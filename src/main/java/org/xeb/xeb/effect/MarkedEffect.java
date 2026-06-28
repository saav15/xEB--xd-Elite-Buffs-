package org.xeb.xeb.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Marked — Unique level debuff.
 * While active:
 *  • All damage received is ×2
 *  • Armor effectiveness is reduced by 50%  (damage treated pre-armor is higher)
 *  • Toughness effectiveness is reduced by 30%
 *  • Any dodge/miss chance that the attacker would normally have is suppressed
 *
 * Logic is applied in BuffDamageHandler (target-side check on LivingHurtEvent.Pre).
 */
public class MarkedEffect extends MobEffect {
    public MarkedEffect() {
        // Dark crimson — marked for death
        super(MobEffectCategory.HARMFUL, 0x8B0000);
    }

    /** Effective armor fraction kept (0.5 = 50% armor penetration). */
    public static final float ARMOR_PENETRATION = 0.50F;
    /** Effective toughness fraction kept (0.70 = 30% toughness penetration). */
    public static final float TOUGHNESS_PENETRATION = 0.30F;
    /** Damage multiplier. */
    public static final float DAMAGE_MULTIPLIER = 2.0F;
}
