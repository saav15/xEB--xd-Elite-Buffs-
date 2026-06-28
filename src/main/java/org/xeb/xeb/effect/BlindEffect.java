package org.xeb.xeb.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Blind — Chance to "miss" your target.
 * The attacker's hit is fully negated (damage + secondary effects) depending on amplifier.
 * Base miss chance: 10%  (+10% per amplifier level)
 *
 * The actual miss logic lives in BuffDamageHandler (attacker-side check).
 */
public class BlindEffect extends MobEffect {
    public BlindEffect() {
        // Purple-black: loss of accuracy
        super(MobEffectCategory.HARMFUL, 0x1A0030);
    }

    /** Returns the miss probability for this effect instance. */
    public static float getMissChance(int amplifier) {
        return 0.10F + 0.10F * amplifier; // lvl 1 = 10%, lvl 2 = 20%, …
    }
}
