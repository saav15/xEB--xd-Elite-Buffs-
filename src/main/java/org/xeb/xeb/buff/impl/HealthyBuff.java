package org.xeb.xeb.buff.impl;

import org.xeb.xeb.buff.BuffType;
import org.xeb.xeb.buff.EliteBuff;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class HealthyBuff extends EliteBuff {
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("fb41b716-e41c-4b68-b80c-7833de0899ac");

    public HealthyBuff() {
        super("healthy", "Healthy", BuffType.ENEMY_ONLY, 0x228B22, 5.0D);
    }

    @Override
    public void onAttach(LivingEntity entity) {
        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            AttributeModifier modifier = new AttributeModifier(HEALTH_MODIFIER_UUID, "Healthy Buff Max Health", 4.0D, AttributeModifier.Operation.ADDITION);
            maxHealth.addTransientModifier(modifier);
            // Heal the entity by the amount added
            entity.heal(4.0F);
        }
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, -1, 2, false, false, false));
    }

    @Override
    public void onDetach(LivingEntity entity) {
        entity.removeEffect(MobEffects.REGENERATION);
        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.removeModifier(HEALTH_MODIFIER_UUID);
        }
    }

    @Override
    public void onServerTick(LivingEntity entity, ServerLevel level) {
        if (!entity.hasEffect(MobEffects.REGENERATION)) {
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, -1, 2, false, false, false));
        }
    }
}
