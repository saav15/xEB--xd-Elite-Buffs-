package org.xeb.xeb.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AllStatsUpEffect extends MobEffect {
    public AllStatsUpEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFD700); // Gold color
        
        // Add attribute modifiers: +10% per level (amplifier)
        // UUIDs must be constant and distinct from AllStatsDown
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "fb41b716-e41c-4b68-b80c-7833de08ab33", 0.10D, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(Attributes.MAX_HEALTH, "fb41b716-e41c-4b68-b80c-7833de08ab34", 0.10D, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, "fb41b716-e41c-4b68-b80c-7833de08ab35", 0.10D, AttributeModifier.Operation.MULTIPLY_BASE);
    }
}
