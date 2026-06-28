package org.xeb.xeb.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CharredBurnEffect extends MobEffect {
    public CharredBurnEffect() {
        super(MobEffectCategory.HARMFUL, 0xE65C00);
        // Each level of Charred Burn reduces attack damage by 15%
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, "7107DE5E-7CE8-4030-940E-514C1F160892", -0.15D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            entity.hurt(entity.damageSources().inFire(), 1.0F + amplifier);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
