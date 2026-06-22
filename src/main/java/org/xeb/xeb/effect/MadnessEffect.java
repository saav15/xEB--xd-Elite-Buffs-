package org.xeb.xeb.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class MadnessEffect extends MobEffect {
    public MadnessEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B0000); // Dark red color representing rage/madness
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide() && entity instanceof Mob mob) {
            LivingEntity currentTarget = mob.getTarget();
            // If there's no target, or target is dead, or randomly switch targets (15% chance per second) to create chaos
            if (currentTarget == null || !currentTarget.isAlive() || mob.getRandom().nextFloat() < 0.15F) {
                double range = 16.0D + amplifier * 4.0D;
                AABB searchBox = mob.getBoundingBox().inflate(range);
                List<LivingEntity> potentialTargets = mob.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                        target -> target != mob && target.isAlive() && mob.hasLineOfSight(target) &&
                                  !(target instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator())));
                if (!potentialTargets.isEmpty()) {
                    LivingEntity target = potentialTargets.get(mob.getRandom().nextInt(potentialTargets.size()));
                    mob.setTarget(target);
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Run target selection check every 20 ticks (1s)
        return duration % 20 == 0;
    }
}
