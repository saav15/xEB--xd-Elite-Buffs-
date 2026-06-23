package org.xeb.xeb.entity;

import net.minecraft.core.particles.ParticleTypes;
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

import java.util.List;

public class SparkleEntity extends ThrowableProjectile {
    private double damage = 1.0D;
    private LivingEntity target = null;
    private int targetId = -1;

    public SparkleEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public SparkleEntity(Level level, LivingEntity owner, double damage, LivingEntity target) {
        super(ModEntities.SPARKLE.get(), owner, level);
        this.damage = damage;
        this.target = target;
        if (target != null) {
            this.targetId = target.getId();
        }
        this.setNoGravity(true);
        // Initial random upward burst velocity to simulate natural spawning spikes
        double rx = (this.random.nextDouble() - 0.5D) * 0.4D;
        double ry = 0.3D + this.random.nextDouble() * 0.4D;
        double rz = (this.random.nextDouble() - 0.5D) * 0.4D;
        this.setDeltaMovement(new Vec3(rx, ry, rz).normalize().scale(0.35D));
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            // Trail particles
            for (int i = 0; i < 2; i++) {
                double px = this.getX() + (this.random.nextDouble() - 0.5D) * 0.1D;
                double py = this.getY() + (this.random.nextDouble() - 0.5D) * 0.1D;
                double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.1D;
                this.level().addParticle(ParticleTypes.END_ROD, px, py, pz, 0, 0, 0);
            }
            return;
        }

        // Server-side
        if (this.tickCount > 100) {
            this.discard();
            return;
        }

        // Target validation
        if (this.target == null && this.targetId != -1) {
            net.minecraft.world.entity.Entity entity = this.level().getEntity(this.targetId);
            if (entity instanceof LivingEntity living) {
                this.target = living;
            }
        }

        findTarget();

        if (this.target != null && this.target.isAlive()) {
            Vec3 targetPos = this.target.getBoundingBox().getCenter();
            Vec3 direction = targetPos.subtract(this.position());
            double distSq = direction.lengthSqr();
            double speed = 0.6D;
            
            if (distSq < 4.0D) {
                // Direct lock-on when within 2 blocks to guarantee hit
                this.setDeltaMovement(direction.normalize().scale(speed));
            } else {
                // Extremely sharp homing curve interpolation: 70% target direction, 30% current velocity
                Vec3 currentVelocity = this.getDeltaMovement();
                Vec3 newVelocity = currentVelocity.scale(0.3D).add(direction.normalize().scale(0.7D)).normalize().scale(speed);
                this.setDeltaMovement(newVelocity);
            }
        } else {
            // No target: steer directly down to hit the ground/floor
            this.setDeltaMovement(new Vec3(0, -0.3D, 0));
        }
    }

    private void findTarget() {
        if (this.target == null || !this.target.isAlive()) {
            double range = 16.0D;
            net.minecraft.world.phys.AABB box = this.getBoundingBox().inflate(range);
            List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity != this.getOwner() && entity.isAlive() &&
                           !(entity instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator()))
            );
            
            LivingEntity nearest = null;
            double minDist = Double.MAX_VALUE;
            for (LivingEntity e : list) {
                double dist = this.distanceToSqr(e);
                if (dist < minDist) {
                    minDist = dist;
                    nearest = e;
                }
            }
            this.target = nearest;
            if (nearest != null) {
                this.targetId = nearest.getId();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        net.minecraft.world.entity.Entity hitEntity = result.getEntity();
        if (hitEntity == this.getOwner()) {
            return;
        }

        if (!this.level().isClientSide()) {
            net.minecraft.world.entity.Entity owner = this.getOwner();
            net.minecraft.world.damagesource.DamageSource source;
            if (owner instanceof LivingEntity livingOwner) {
                source = this.damageSources().indirectMagic(this, livingOwner);
            } else {
                source = this.damageSources().magic();
            }
            
            hitEntity.hurt(source, (float) this.damage);
            sendHitParticles();
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide()) {
            sendHitParticles();
            this.discard();
        }
    }



    private void sendHitParticles() {
        this.level().broadcastEntityEvent(this, (byte) 3);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 12; i++) {
                double px = this.getX() + (this.random.nextDouble() - 0.5D) * 0.3D;
                double py = this.getY() + (this.random.nextDouble() - 0.5D) * 0.3D;
                double pz = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.3D;
                this.level().addParticle(ParticleTypes.END_ROD, px, py, pz,
                        (this.random.nextDouble() - 0.5D) * 0.2D,
                        (this.random.nextDouble() - 0.5D) * 0.2D,
                        (this.random.nextDouble() - 0.5D) * 0.2D);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

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
        if (tag.contains("xebSparkleDamage")) {
            this.damage = tag.getDouble("xebSparkleDamage");
        }
        if (tag.contains("xebSparkleTargetId")) {
            this.targetId = tag.getInt("xebSparkleTargetId");
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
