package org.xeb.xeb.effect;

import org.xeb.xeb.medallion.MedallionManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class TarredEffect extends MobEffect {
    public TarredEffect() {
        super(MobEffectCategory.HARMFUL, 0x2E8B57);
        // Each stack (amplifier + 1) multiplies MOVEMENT_SPEED by -20%
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890", -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (MedallionManager.hasBuff(entity, "hardy")) {
            entity.removeEffect(this);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
