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

public class DamagingBuff extends EliteBuff {
    private static final UUID ATTACK_MODIFIER_UUID = UUID.fromString("4907a974-9f44-469b-8ee3-a551de7e7166");

    public DamagingBuff() {
        super("damaging", "Damaging", BuffType.UNIVERSAL, 0xDC143C, 10.0D);
    }

    @Override
    public void onAttach(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance != null) {
            double amount = MedallionManager.isBoss(entity) ? 1.0D : 2.0D;
            AttributeModifier modifier = new AttributeModifier(ATTACK_MODIFIER_UUID, "Damaging Buff Modifier", amount, AttributeModifier.Operation.ADDITION);
            instance.addTransientModifier(modifier);
        }
    }

    @Override
    public void onDetach(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance != null) {
            instance.removeModifier(ATTACK_MODIFIER_UUID);
        }
    }

    @Override
    public void onServerTick(LivingEntity entity, ServerLevel level) {}
}
