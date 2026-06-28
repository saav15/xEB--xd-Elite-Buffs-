package org.xeb.xeb.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class DelayedPainEffect extends MobEffect {
    public DelayedPainEffect() {
        super(MobEffectCategory.HARMFUL, 0x550000);
    }
}
