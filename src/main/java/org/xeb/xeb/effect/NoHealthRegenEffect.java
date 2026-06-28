package org.xeb.xeb.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * NHR (No Health Regen) — Unique level.
 * Completely prevents any form of health regeneration while active.
 * Cancels LivingHealEvent in BuffDamageHandler.
 */
public class NoHealthRegenEffect extends MobEffect {
    public NoHealthRegenEffect() {
        // Dull grey-red — vital suppression
        super(MobEffectCategory.HARMFUL, 0x5C1A1A);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // No tick-based logic needed; heal cancellation handled via event
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
