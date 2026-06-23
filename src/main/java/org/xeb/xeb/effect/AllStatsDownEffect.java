package org.xeb.xeb.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AllStatsDownEffect extends MobEffect {
    public AllStatsDownEffect() {
        super(MobEffectCategory.HARMFUL, 0x556B2F); // Olive green/dark color
        
        // Add attribute modifiers: -10% per level (amplifier)
        // UUIDs must be constant.
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "fb41b716-e41c-4b68-b80c-7833de08ab30", -0.10D, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(Attributes.MAX_HEALTH, "fb41b716-e41c-4b68-b80c-7833de08ab31", -0.10D, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, "fb41b716-e41c-4b68-b80c-7833de08ab32", -0.10D, AttributeModifier.Operation.MULTIPLY_BASE);
    }
}
