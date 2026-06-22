package org.xeb.xeb.buff.impl;

import org.xeb.xeb.buff.BuffType;
import org.xeb.xeb.buff.EliteBuff;
import org.xeb.xeb.medallion.MedallionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class ToughBuff extends EliteBuff {
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("6f0cd17e-976d-4952-ba61-8f55e09f5fa4");

    public ToughBuff() {
        super("tough", "Tough", BuffType.UNIVERSAL, 0x696969, 10.0D);
    }

    @Override
    public void onAttach(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attributes.ARMOR);
        if (instance != null) {
            double amount = MedallionManager.isBoss(entity) ? 1.0D : 2.0D;
            AttributeModifier modifier = new AttributeModifier(ARMOR_MODIFIER_UUID, "Tough Buff Modifier", amount, AttributeModifier.Operation.ADDITION);
            instance.addTransientModifier(modifier);
        }
    }

    @Override
    public void onDetach(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attributes.ARMOR);
        if (instance != null) {
            instance.removeModifier(ARMOR_MODIFIER_UUID);
        }
    }

    @Override
    public void onServerTick(LivingEntity entity, ServerLevel level) {}
}
