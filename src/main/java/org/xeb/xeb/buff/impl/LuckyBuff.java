package org.xeb.xeb.buff.impl;

import org.xeb.xeb.buff.BuffType;
import org.xeb.xeb.buff.EliteBuff;
import org.xeb.xeb.network.BuffParticlePacket;
import org.xeb.xeb.network.XEBNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;

public class LuckyBuff extends EliteBuff {
    private static final UUID LUCK_MODIFIER_UUID = UUID.fromString("fb41b716-e41c-4b68-b80c-7833de0899ab");

    public LuckyBuff() {
        super("lucky", "Lucky", BuffType.UNIVERSAL, 0xFFD700, 5.0D);
    }

    @Override
    public void onAttach(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attributes.LUCK);
        if (instance != null) {
            AttributeModifier modifier = new AttributeModifier(LUCK_MODIFIER_UUID, "Lucky Buff Modifier", 3.0D, AttributeModifier.Operation.ADDITION);
            instance.addTransientModifier(modifier);
        }
    }

    @Override
    public void onDetach(LivingEntity entity) {
        AttributeInstance instance = entity.getAttribute(Attributes.LUCK);
        if (instance != null) {
            instance.removeModifier(LUCK_MODIFIER_UUID);
        }
    }

    @Override
    public void onServerTick(LivingEntity entity, ServerLevel level) {
        if (entity.tickCount % 10 == 0) {
            BuffParticlePacket packet = new BuffParticlePacket(entity.getX(), entity.getY(), entity.getZ(), "crit", 1);
            XEBNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
        }
    }

    @Override
    public void onDamageDealt(LivingEntity entity, LivingHurtEvent event) {
        if (entity.getRandom().nextFloat() < 0.10F) {
            event.setAmount(event.getAmount() * 2.0F);
            
            // Spawn crit particles
            LivingEntity target = event.getEntity();
            if (target != null && !entity.level().isClientSide()) {
                BuffParticlePacket packet = new BuffParticlePacket(target.getX(), target.getY(), target.getZ(), "crit", 8);
                XEBNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target), packet);
            }
        }
    }

    @Override
    public void onDamageTaken(LivingEntity entity, LivingHurtEvent event) {
        if (event.getSource().is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD) || event.getAmount() >= 1000.0F) {
            return;
        }
        if (entity.getRandom().nextFloat() < 0.10F) {
            event.setAmount(0.0F);
            event.setCanceled(true);
            
            // Spawn dodge particles
            if (!entity.level().isClientSide()) {
                BuffParticlePacket packet = new BuffParticlePacket(entity.getX(), entity.getY(), entity.getZ(), "dodge", 8);
                XEBNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
            }
        }
    }
}
