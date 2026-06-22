package org.xeb.xeb.effect;

import org.xeb.xeb.mana.ManaManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class ManaDrainEffect extends MobEffect {
    public ManaDrainEffect() {
        super(MobEffectCategory.HARMFUL, 0x483D8B);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Drain 1 mana (plus amplifier) per tick
        ManaManager.drainMana(entity, 1.0 + amplifier);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
