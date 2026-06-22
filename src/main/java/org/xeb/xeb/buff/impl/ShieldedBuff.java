package org.xeb.xeb.buff.impl;

import org.xeb.xeb.buff.BuffType;
import org.xeb.xeb.buff.EliteBuff;
import org.xeb.xeb.medallion.MedallionManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.UUID;

public class ShieldedBuff extends EliteBuff {
    private static final String SHIELD_KEY = "xebShield";
    private static final UUID HEALTH_PENALTY_UUID = UUID.fromString("fb41b716-e41c-4b68-b80c-7833de089999");

    public ShieldedBuff() {
        super("shielded", "Shielded", BuffType.UNIVERSAL, 0x4169E1, 2.0D);
    }

    @Override
    public void onAttach(LivingEntity entity) {
        Difficulty difficulty = entity.level().getDifficulty();
        boolean isBoss = MedallionManager.isBoss(entity);
        int shield = 5;

        if (isBoss) {
            shield = switch (difficulty) {
                case EASY -> 4;
                case NORMAL -> 8;
                case HARD -> 16;
                default -> 8;
            };
        } else {
            shield = switch (difficulty) {
                case EASY -> 3;
                case NORMAL -> 5;
                case HARD -> 7;
                default -> 5;
            };
        }

        entity.getPersistentData().putInt(SHIELD_KEY, shield);

        // Max health penalty of -20%
        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            AttributeModifier modifier = new AttributeModifier(HEALTH_PENALTY_UUID, "Shielded Health Penalty", -0.20D, AttributeModifier.Operation.MULTIPLY_BASE);
            maxHealth.addTransientModifier(modifier);
            // Clamp current health
            if (entity.getHealth() > entity.getMaxHealth()) {
                entity.setHealth(entity.getMaxHealth());
            }
        }
    }

    @Override
    public void onDetach(LivingEntity entity) {
        entity.getPersistentData().remove(SHIELD_KEY);
        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.removeModifier(HEALTH_PENALTY_UUID);
        }
    }

    @Override
    public void onServerTick(LivingEntity entity, ServerLevel level) {}

    @Override
    public void onDamageTaken(LivingEntity entity, LivingHurtEvent event) {
        CompoundTag tag = entity.getPersistentData();
        if (tag.contains(SHIELD_KEY)) {
            int shield = tag.getInt(SHIELD_KEY);
            if (shield > 0) {
                float damage = event.getAmount();
                if (shield >= damage) {
                    shield -= damage;
                    tag.putInt(SHIELD_KEY, shield);
                    event.setAmount(0.0F);
                } else {
                    float remaining = damage - shield;
                    tag.putInt(SHIELD_KEY, 0);
                    event.setAmount(remaining);
                }
            }
        }
    }
}
