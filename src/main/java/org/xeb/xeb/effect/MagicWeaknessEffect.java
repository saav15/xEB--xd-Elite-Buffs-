package org.xeb.xeb.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Magic Weakness — All magic-sourced damage received is multiplied.
 * Base: ×2  (+1× per amplifier level → lvl 2 = ×3, lvl 3 = ×4, …)
 *
 * Actual damage multiplication is applied in BuffDamageHandler (target-side check).
 */
public class MagicWeaknessEffect extends MobEffect {
    public MagicWeaknessEffect() {
        // Dark blue — arcane vulnerability
        super(MobEffectCategory.HARMFUL, 0x00008B);
    }

    /** Returns the magic damage multiplier for this effect instance. */
    public static float getDamageMultiplier(int amplifier) {
        return 2.0F + amplifier; // lvl 0 = ×2, lvl 1 = ×3, …
    }
}
