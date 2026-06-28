package org.xeb.xeb.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * NMR (No Mana Regen) — Unique level.
 * Prevents mana regeneration from any magic mod while active.
 * The ManaManager.regenMana call is short-circuited in BuffTickHandler
 * when the entity has this effect.
 */
public class NoManaRegenEffect extends MobEffect {
    public NoManaRegenEffect() {
        // Dark teal — mana suppression
        super(MobEffectCategory.HARMFUL, 0x003333);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Tick-less; logic is handled in BuffTickHandler mana regen gate
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
