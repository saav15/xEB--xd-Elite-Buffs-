package org.xeb.xeb.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Sparkle — homing energy-bolt projectile.
 *
 * Launched by Kinetic Spikes (on damage received) and Charged Fist (on damage dealt).
 *
 * Visual:
 *  - Rendered as a small 3D arrowhead model (GeckoLib, sparkle_arrow.geo.json),
 *    always oriented along its current velocity vector.
 *  - Keeps a rolling history of the last TRAIL_LEN world positions sampled once
 *    per tick; the SparkleRenderer reads this to draw a fading particle trail.
 *
 * Homing:
 *  - 70 % target direction + 30 % current velocity → extremely sharp homing.
 *  - Within 2 blocks: pure lock-on at full speed.
 *  - No target: falls straight down.
 *  - Max lifetime: 100 ticks.
 */
public class SparkleEntity extends ThrowableProjectile implements GeoEntity {

    // ── Trail ──────────────────────────────────────────────────────────────────
    /** How many past positions to keep for trail rendering. */
    public static final int TRAIL_LEN = 14;
    /** Rolling buffer of past positions, oldest first. Client-side only. */
    private final Deque<Vec3> trailPositions = new ArrayDeque<>(TRAIL_LEN + 1);

    // ── GeckoLib ───────────────────────────────────────────────────────────────
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    // ── State ──────────────────────────────────────────────────────────────────
    private double damage = 1.0D;
    private LivingEntity target = null;
    private int targetId = -1;

    // ── Constructors ───────────────────────────────────────────────────────────

    public SparkleEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public SparkleEntity(Level level, LivingEntity owner, double damage, LivingEntity target) {
        super(ModEntities.SPARKLE.get(), owner, level);
        this.damage = damage;
        this.target = target;
        if (target != null) this.targetId = target.getId();
        this.setNoGravity(true);

        // Random outward burst so multiple sparkles fan out slightly
        double rx = (this.random.nextDouble() - 0.5D) * 0.5D;
        double ry = 0.25D + this.random.nextDouble() * 0.35D;
        double rz = (this.random.nextDouble() - 0.5D) * 0.5D;
        this.setDeltaMovement(new Vec3(rx, ry, rz).normalize().scale(0.4D));
    }

    @Override
    protected void defineSynchedData() {}

    // ── Tick ───────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();

        // ── Client: record position for trail rendering ────────────────────────
        if (this.level().isClientSide()) {
            Vec3 pos = this.position();
            trailPositions.addLast(pos);
            while (trailPositions.size() > TRAIL_LEN) {
                trailPositions.pollFirst();
            }
            // Spawn a small static white trail dust particle
            double px = this.getX() + (this.random.nextDouble() - 0.5D) * 0.05D;
            double py = this.getY() + (this.random.nextDouble() - 0.5D) * 0.05D;
            double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.05D;
            this.level().addParticle(
                new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(1.0F, 1.0F, 1.0F), 0.4F),
                px, py, pz, 0, 0, 0
            );
            return; // rendering only — no game logic on client
        }

        // ── Server: lifetime check ─────────────────────────────────────────────
        if (this.tickCount > 100) {
            this.discard();
            return;
        }

        // ── Server: target resolution ──────────────────────────────────────────
        if (this.target == null && this.targetId != -1) {
            net.minecraft.world.entity.Entity e = this.level().getEntity(this.targetId);
            if (e instanceof LivingEntity living) this.target = living;
        }
        findTarget();

        // ── Server: steering ───────────────────────────────────────────────────
        if (this.target != null && this.target.isAlive()) {
            Vec3 targetPos = this.target.getBoundingBox().getCenter();
            Vec3 direction = targetPos.subtract(this.position());
            double distSq = direction.lengthSqr();
            final double speed = 0.65D;

            if (distSq < 4.0D) {
                // Lock-on within 2 blocks — guaranteed hit
                this.setDeltaMovement(direction.normalize().scale(speed));
            } else {
                // Sharp homing: 70% toward target, 30% current inertia
                Vec3 cur = this.getDeltaMovement();
                Vec3 newVel = cur.scale(0.3D)
                        .add(direction.normalize().scale(0.7D))
                        .normalize()
                        .scale(speed);
                this.setDeltaMovement(newVel);
            }
        } else {
            // No target — keep flying in the current direction at speed
            Vec3 cur = this.getDeltaMovement();
            if (cur.lengthSqr() < 1e-6) {
                this.setDeltaMovement(new Vec3(0, -0.3D, 0));
            } else {
                this.setDeltaMovement(cur.normalize().scale(0.65D));
            }
        }
    }

    // ── Trail accessor (read by SparkleRenderer) ───────────────────────────────

    /** Returns a snapshot of the trail position history, oldest first. */
    public Vec3[] getTrailSnapshot() {
        return trailPositions.toArray(new Vec3[0]);
    }

    // ── Target finding ─────────────────────────────────────────────────────────

    private void findTarget() {
        if (this.target != null && this.target.isAlive()) return;

        double range = 16.0D;
        net.minecraft.world.entity.Entity owner = this.getOwner();
        if (owner instanceof LivingEntity livingOwner) {
            this.target = org.xeb.xeb.event.BuffDamageHandler.findNearestEnemy(livingOwner, range);
            if (this.target != null) {
                this.targetId = this.target.getId();
            }
        } else {
            net.minecraft.world.phys.AABB box = this.getBoundingBox().inflate(range);
            List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != this.getOwner() && e.isAlive()
                    && !(e instanceof net.minecraft.world.entity.player.Player p
                         && (p.isCreative() || p.isSpectator()))
            );

            LivingEntity nearest = null;
            double minDist = Double.MAX_VALUE;
            for (LivingEntity e : list) {
                double dist = this.distanceToSqr(e);
                if (dist < minDist) { minDist = dist; nearest = e; }
            }
            this.target = nearest;
            if (nearest != null) this.targetId = nearest.getId();
        }
    }

    public void onReflected(LivingEntity newOwner) {
        this.setOwner(newOwner);
        this.target = null;
        this.targetId = -1;
        findTarget();
    }

    // ── Collision ──────────────────────────────────────────────────────────────

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        net.minecraft.world.entity.Entity hit = result.getEntity();
        if (hit == this.getOwner()) return;

        if (!this.level().isClientSide()) {
            net.minecraft.world.entity.Entity owner = this.getOwner();
            net.minecraft.world.damagesource.DamageSource src =
                (owner instanceof LivingEntity lo)
                    ? this.damageSources().indirectMagic(this, lo)
                    : this.damageSources().magic();
            hit.hurt(src, (float) this.damage);
            broadcastHitEvent();
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide()) {
            broadcastHitEvent();
            this.discard();
        }
    }

    private void broadcastHitEvent() {
        this.level().broadcastEntityEvent(this, (byte) 3);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            // Client: tighter burst of smaller white dust particles on impact
            for (int i = 0; i < 5; i++) {
                double px = this.getX() + (this.random.nextDouble() - 0.5D) * 0.15D;
                double py = this.getY() + (this.random.nextDouble() - 0.5D) * 0.15D;
                double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.15D;
                this.level().addParticle(
                    new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(1.0F, 1.0F, 1.0F), 0.5F),
                    px, py, pz,
                    (this.random.nextDouble() - 0.5D) * 0.1D,
                    (this.random.nextDouble() - 0.5D) * 0.1D,
                    (this.random.nextDouble() - 0.5D) * 0.1D
                );
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    // ── NBT ────────────────────────────────────────────────────────────────────

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("xebSparkleDamage", this.damage);
        if (this.target != null && this.target.isAlive()) {
            tag.putInt("xebSparkleTargetId", this.target.getId());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("xebSparkleDamage")) this.damage   = tag.getDouble("xebSparkleDamage");
        if (tag.contains("xebSparkleTargetId")) this.targetId = tag.getInt("xebSparkleTargetId");
    }

    // ── Network ────────────────────────────────────────────────────────────────

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    // ── GeckoLib ───────────────────────────────────────────────────────────────

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // No animations — the model is static; rotation is done in the renderer
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
