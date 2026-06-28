package org.xeb.xeb.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Adrenaline — Unique level, ONLY obtainable while the entity has Exhausted.
 * While active:
 *  • 75% critical hit chance on attacks
 *  • ×2 bonus damage on every hit (stacks with crits)
 *  • 40% chance to dodge incoming attacks (cancels damage + secondary effects)
 *
 * Applied automatically when Exhausted is added (same duration).
 * Removed automatically when Exhausted expires.
 *
 * The crit/damage/dodge logic is in BuffDamageHandler.
 * The linkage check is in BuffTickHandler.
 */
public class AdrenalineEffect extends MobEffect {
    public static final float CRIT_CHANCE    = 0.75F;
    public static final float DAMAGE_BONUS   = 2.0F; // multiplier
    public static final float DODGE_CHANCE   = 0.40F;

    public AdrenalineEffect() {
        // Bright red — surge of survival energy
        super(MobEffectCategory.BENEFICIAL, 0xFF2200);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Tick-less — all logic handled via events in BuffDamageHandler
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
