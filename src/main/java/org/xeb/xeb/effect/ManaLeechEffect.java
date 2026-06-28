package org.xeb.xeb.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Mana Leeches — Drains a percentage of the victim's mana each second,
 * transferring it to whoever applied this effect.
 *
 * Drain rate: 5% per second base  (+5% per amplifier level)
 * Applicator UUID is stored in the victim's PersistentData under "xebManaLeechApplicator"
 * (written when the effect is added via MobEffectEvent.Added).
 *
 * Drain tick logic lives in BuffTickHandler.
 */
public class ManaLeechEffect extends MobEffect {
    public static final String APPLICATOR_KEY = "xebManaLeechApplicator";

    public ManaLeechEffect() {
        // Deep purple — mana drain
        super(MobEffectCategory.HARMFUL, 0x4B0082);
    }

    /** Fraction of max-mana drained per second. */
    public static float getDrainPercent(int amplifier) {
        return 0.05F + 0.05F * amplifier; // lvl 0 = 5%, lvl 1 = 10%, …
    }
}
