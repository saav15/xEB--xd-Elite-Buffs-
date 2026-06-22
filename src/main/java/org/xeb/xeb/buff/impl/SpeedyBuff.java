package org.xeb.xeb.buff.impl;

import org.xeb.xeb.buff.BuffType;
import org.xeb.xeb.buff.EliteBuff;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class SpeedyBuff extends EliteBuff {
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("6f0cd17e-976d-4952-ba61-8f55e09f5fbb");

    public SpeedyBuff() {
        super("speedy", "Speedy", BuffType.UNIVERSAL, 0x00CED1, 10.0D);
    }

    @Override
    public void onAttach(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (instance != null) {
            AttributeModifier modifier = new AttributeModifier(SPEED_MODIFIER_UUID, "Speedy Buff Modifier", 0.2D, AttributeModifier.Operation.ADDITION);
            instance.addTransientModifier(modifier);
        }
    }

    @Override
    public void onDetach(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (instance != null) {
            instance.removeModifier(SPEED_MODIFIER_UUID);
        }
    }

    @Override
    public void onServerTick(LivingEntity entity, ServerLevel level) {}
}
