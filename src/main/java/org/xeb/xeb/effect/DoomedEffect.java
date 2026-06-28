package org.xeb.xeb.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Doomed — Unique level.
 * Uses the vanilla effect duration as a death-countdown:
 *  - The HUD darkens progressively client-side (DoomedOverlay.java handles rendering).
 *  - When duration reaches 0 the entity is killed instantly server-side (BuffTickHandler).
 *
 * Duration is whatever the applicator set when calling addEffect().
 * Client reads remaining duration from the standard MobEffectInstance API.
 */
public class DoomedEffect extends MobEffect {
    public DoomedEffect() {
        // Black — inevitable doom
        super(MobEffectCategory.HARMFUL, 0x0D0D0D);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return;
        // Death is issued in BuffTickHandler when duration == 0
        // (applyEffectTick is not called at tick 0, so we guard in the handler)
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Check every tick so the handler can catch the exact final tick
        return true;
    }
}
