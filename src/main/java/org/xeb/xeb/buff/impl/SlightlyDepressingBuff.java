package org.xeb.xeb.buff.impl;

import org.xeb.xeb.buff.BuffType;
import org.xeb.xeb.buff.EliteBuff;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class SlightlyDepressingBuff extends EliteBuff {
    private static final UUID SLIGHTLY_DAMAGE_UUID = UUID.fromString("fb41b716-e41c-4b68-b80c-7833de08ab20");
    private static final UUID SLIGHTLY_ARMOR_UUID = UUID.fromString("fb41b716-e41c-4b68-b80c-7833de08ab21");
    private static final UUID SLIGHTLY_HEALTH_UUID = UUID.fromString("fb41b716-e41c-4b68-b80c-7833de08ab22");
    private static final UUID SLIGHTLY_SPEED_UUID = UUID.fromString("fb41b716-e41c-4b68-b80c-7833de08ab23");
    private static final UUID SLIGHTLY_LUCK_UUID = UUID.fromString("fb41b716-e41c-4b68-b80c-7833de08ab24");

    private static final Map<UUID, Set<UUID>> SLIGHTLY_DEPRESSED_ENTITIES = new HashMap<>();

    public SlightlyDepressingBuff() {
        super("slightly_depressing", "Slightly Depressing", BuffType.UNIVERSAL, 0x708090, 5.0D);
    }

    @Override
    public void onAttach(LivingEntity entity) {}

    @Override
    public void onDetach(LivingEntity entity) {
        UUID mobId = entity.getUUID();
        for (Map.Entry<UUID, Set<UUID>> entry : SLIGHTLY_DEPRESSED_ENTITIES.entrySet()) {
            if (entry.getValue().remove(mobId) && entry.getValue().isEmpty()) {
                if (entity.level() instanceof ServerLevel serverLevel) {
                    net.minecraft.world.entity.Entity affected = serverLevel.getEntity(entry.getKey());
                    if (affected instanceof LivingEntity living) {
                        removeModifiers(living);
                    }
                }
            }
        }
    }

    @Override
    public void onServerTick(LivingEntity entity, ServerLevel level) {
        if (entity.tickCount % 10 != 0) return;

        UUID mobId = entity.getUUID();
        AABB aabb = entity.getBoundingBox().inflate(2.0D);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, aabb);

        List<LivingEntity> toAffect = new ArrayList<>();
        for (LivingEntity target : targets) {
            if (target instanceof Player || target.isControlledByLocalInstance() || target.getType() != entity.getType()) {
                if (target != entity) {
                    toAffect.add(target);
                }
            }
        }

        // Apply debuffs to targets (no limit on stacking)
        for (LivingEntity target : toAffect) {
            UUID targetId = target.getUUID();
            Set<UUID> sources = SLIGHTLY_DEPRESSED_ENTITIES.computeIfAbsent(targetId, k -> new HashSet<>());
            sources.add(mobId);
            applyModifiers(target);
        }

        // Cleanup entities that moved out of range
        for (Map.Entry<UUID, Set<UUID>> entry : SLIGHTLY_DEPRESSED_ENTITIES.entrySet()) {
            if (entry.getValue().contains(mobId)) {
                net.minecraft.world.entity.Entity affected = level.getEntity(entry.getKey());
                if (affected instanceof LivingEntity living) {
                    if (living.distanceToSqr(entity) > 4.0D) { // Out of 2 blocks (2^2 = 4)
                        entry.getValue().remove(mobId);
                        if (entry.getValue().isEmpty()) {
                            removeModifiers(living);
                        }
                    }
                }
            }
        }
    }

    private static void applyModifiers(LivingEntity target) {
        AttributeInstance dmg = target.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null && dmg.getModifier(SLIGHTLY_DAMAGE_UUID) == null) {
            dmg.addTransientModifier(new AttributeModifier(SLIGHTLY_DAMAGE_UUID, "Slightly Depressing Damage Debuff", -1.0D, AttributeModifier.Operation.ADDITION));
        }

        AttributeInstance armor = target.getAttribute(Attributes.ARMOR);
        if (armor != null && armor.getModifier(SLIGHTLY_ARMOR_UUID) == null) {
            armor.addTransientModifier(new AttributeModifier(SLIGHTLY_ARMOR_UUID, "Slightly Depressing Armor Debuff", -1.0D, AttributeModifier.Operation.ADDITION));
        }

        AttributeInstance hp = target.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null && hp.getModifier(SLIGHTLY_HEALTH_UUID) == null) {
            hp.addTransientModifier(new AttributeModifier(SLIGHTLY_HEALTH_UUID, "Slightly Depressing Health Debuff", -1.0D, AttributeModifier.Operation.ADDITION));
            if (target.getHealth() > target.getMaxHealth()) {
                target.setHealth(target.getMaxHealth());
            }
        }

        AttributeInstance speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && speed.getModifier(SLIGHTLY_SPEED_UUID) == null) {
            speed.addTransientModifier(new AttributeModifier(SLIGHTLY_SPEED_UUID, "Slightly Depressing Speed Debuff", -0.05D, AttributeModifier.Operation.ADDITION));
        }

        AttributeInstance luck = target.getAttribute(Attributes.LUCK);
        if (luck != null && luck.getModifier(SLIGHTLY_LUCK_UUID) == null) {
            luck.addTransientModifier(new AttributeModifier(SLIGHTLY_LUCK_UUID, "Slightly Depressing Luck Debuff", -1.0D, AttributeModifier.Operation.ADDITION));
        }
    }

    private static void removeModifiers(LivingEntity target) {
        AttributeInstance dmg = target.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null) dmg.removeModifier(SLIGHTLY_DAMAGE_UUID);

        AttributeInstance armor = target.getAttribute(Attributes.ARMOR);
        if (armor != null) armor.removeModifier(SLIGHTLY_ARMOR_UUID);

        AttributeInstance hp = target.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) hp.removeModifier(SLIGHTLY_HEALTH_UUID);

        AttributeInstance speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.removeModifier(SLIGHTLY_SPEED_UUID);

        AttributeInstance luck = target.getAttribute(Attributes.LUCK);
        if (luck != null) luck.removeModifier(SLIGHTLY_LUCK_UUID);
    }
}
