package org.xeb.xeb.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MadnessEffect extends MobEffect {
    public MadnessEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B0000);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return;
        if (!(entity instanceof Mob mob)) return;

        double range = 16.0D + amplifier * 4.0D;
        AABB searchBox = mob.getBoundingBox().inflate(range);
        List<LivingEntity> potentialTargets = mob.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                target -> target != mob && target.isAlive() && mob.hasLineOfSight(target) &&
                          !(target instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator())));

        LivingEntity currentTarget = mob.getTarget();
        boolean shouldSwitch = currentTarget == null || !currentTarget.isAlive()
                || mob.getRandom().nextFloat() < 0.15F;

        if (shouldSwitch && !potentialTargets.isEmpty()) {
            LivingEntity newTarget = potentialTargets.get(mob.getRandom().nextInt(potentialTargets.size()));
            // Store target ID in persistent NBT for event interception
            mob.getPersistentData().putInt("xebMadnessTargetId", newTarget.getId());
            // setTarget alone is sometimes ignored by custom boss AI (e.g. Ender Cataclysm bosses).
            // Also set last-hurt references to trigger their internal aggro systems.
            mob.setTarget(newTarget);
            mob.setLastHurtByMob(newTarget);
            mob.setLastHurtMob(newTarget);
        }

        // Frantic movement — random yaw jitter and lateral impulses
        applyFranticMovement(mob, amplifier);
    }

    private void applyFranticMovement(Mob mob, int amplifier) {
        float rand = mob.getRandom().nextFloat();

        // 45% chance: sudden yaw jerk
        if (rand < 0.45F) {
            float jitter = (mob.getRandom().nextFloat() - 0.5F) * (30.0F + amplifier * 10.0F);
            mob.setYRot(mob.getYRot() + jitter);
            mob.yRotO = mob.getYRot();
            mob.yHeadRot = mob.getYRot();
        }

        // 20% chance: lateral impulse (strafe)
        if (rand < 0.20F && mob.onGround()) {
            float strafeX = (mob.getRandom().nextFloat() - 0.5F) * 0.3F;
            float strafeZ = (mob.getRandom().nextFloat() - 0.5F) * 0.3F;
            Vec3 current = mob.getDeltaMovement();
            mob.setDeltaMovement(current.x + strafeX, current.y, current.z + strafeZ);
            mob.hurtMarked = true;
        }

        // 10% chance: random jump lunge toward or away from target
        if (rand < 0.10F && mob.onGround()) {
            LivingEntity target = mob.getTarget();
            if (target != null) {
                Vec3 dir = target.position().subtract(mob.position()).normalize();
                // 50/50: lunge toward or away
                double sign = mob.getRandom().nextBoolean() ? 1.0 : -1.0;
                mob.setDeltaMovement(
                        mob.getDeltaMovement().x + dir.x * sign * 0.4,
                        0.42,
                        mob.getDeltaMovement().z + dir.z * sign * 0.4
                );
                mob.hurtMarked = true;
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Run every 10 ticks (2× per second) instead of 20 — more reactive
        return duration % 10 == 0;
    }
}
