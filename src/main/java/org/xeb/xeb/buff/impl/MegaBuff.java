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
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;

public class MegaBuff extends EliteBuff {
    private static final UUID MEGA_HEALTH_UUID = UUID.fromString("fb41b716-e41c-4b68-b80c-7833de08ab30");
    private static final UUID MEGA_DAMAGE_UUID = UUID.fromString("fb41b716-e41c-4b68-b80c-7833de08ab31");
    private static final UUID MEGA_SPEED_UUID = UUID.fromString("fb41b716-e41c-4b68-b80c-7833de08ab32");

    public MegaBuff() {
        super("mega", "Mega", BuffType.ENEMY_ONLY, 0xFF1493, 1.0D);
    }

    @Override
    public void onAttach(LivingEntity entity) {
        // +50% Max Health
        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.addTransientModifier(new AttributeModifier(MEGA_HEALTH_UUID, "Mega Health modifier", 0.5D, AttributeModifier.Operation.MULTIPLY_BASE));
            entity.heal((float) (entity.getMaxHealth() * 0.5));
        }

        // +50% Damage
        AttributeInstance attackDmg = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDmg != null) {
            attackDmg.addTransientModifier(new AttributeModifier(MEGA_DAMAGE_UUID, "Mega Damage modifier", 0.5D, AttributeModifier.Operation.MULTIPLY_BASE));
        }

        // +50% Attack Speed
        AttributeInstance attackSpeed = entity.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.addTransientModifier(new AttributeModifier(MEGA_SPEED_UUID, "Mega Attack Speed modifier", 0.5D, AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    @Override
    public void onDetach(LivingEntity entity) {
        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) maxHealth.removeModifier(MEGA_HEALTH_UUID);

        AttributeInstance attackDmg = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDmg != null) attackDmg.removeModifier(MEGA_DAMAGE_UUID);

        AttributeInstance attackSpeed = entity.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) attackSpeed.removeModifier(MEGA_SPEED_UUID);
    }

    @Override
    public void onServerTick(LivingEntity entity, ServerLevel level) {
        if (entity.tickCount % 8 == 0) {
            BuffParticlePacket packet = new BuffParticlePacket(entity.getX(), entity.getY(), entity.getZ(), "mega", 1);
            XEBNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
        }
    }
}
