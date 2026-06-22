package org.xeb.xeb.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class SandstormEffect extends MobEffect {
    public SandstormEffect() {
        super(MobEffectCategory.HARMFUL, 0xF4A460);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Deal 1 magic damage (+ amplifier for scaling)
        entity.hurt(entity.damageSources().magic(), 1.0F + amplifier);
        
        // Spawn sand particles
        if (entity.level().isClientSide()) {
            double x = entity.getX();
            double y = entity.getY() + entity.getBbHeight() / 2.0;
            double z = entity.getZ();
            entity.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 0, 0.02, 0);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Trigger once every second (20 ticks)
        return duration % 20 == 0;
    }
}
