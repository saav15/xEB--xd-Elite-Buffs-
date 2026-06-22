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

public class DepressingBuff extends EliteBuff {
    private static final UUID DEPRESSING_DAMAGE_UUID = UUID.fromString("fb41b716-e41c-4b68-b80c-7833de08ab10");
    private static final UUID DEPRESSING_ARMOR_UUID = UUID.fromString("fb41b716-e41c-4b68-b80c-7833de08ab11");
    private static final UUID DEPRESSING_HEALTH_UUID = UUID.fromString("fb41b716-e41c-4b68-b80c-7833de08ab12");
    private static final UUID DEPRESSING_SPEED_UUID = UUID.fromString("fb41b716-e41c-4b68-b80c-7833de08ab13");
    private static final UUID DEPRESSING_LUCK_UUID = UUID.fromString("fb41b716-e41c-4b68-b80c-7833de08ab14");

    private static final Map<UUID, Set<UUID>> DEPRESSED_ENTITIES = new HashMap<>(); // Entity UUID -> Set of Depressing Mob UUIDs affecting it

    public DepressingBuff() {
        super("depressing", "Depressing", BuffType.UNIVERSAL, 0x2F4F4F, 2.0D);
    }

    @Override
    public void onAttach(LivingEntity entity) {}

    @Override
    public void onDetach(LivingEntity entity) {
        // Cleanup all entities affected by this mob
        UUID mobId = entity.getUUID();
        for (Map.Entry<UUID, Set<UUID>> entry : DEPRESSED_ENTITIES.entrySet()) {
            if (entry.getValue().remove(mobId) && entry.getValue().isEmpty()) {
                // If no other depressing mob affects it, remove modifiers
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
        AABB aabb = entity.getBoundingBox().inflate(8.0D);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, aabb);

        // Find players and hostile entities to target
        List<LivingEntity> toAffect = new ArrayList<>();
        for (LivingEntity target : targets) {
            if (target instanceof Player || target.isControlledByLocalInstance() || target.getType() != entity.getType()) {
                if (target != entity) {
                    toAffect.add(target);
                }
            }
        }

        // Apply debuffs to targets (limit to 2 depressing sources per target fight)
        for (LivingEntity target : toAffect) {
            UUID targetId = target.getUUID();
            Set<UUID> sources = DEPRESSED_ENTITIES.computeIfAbsent(targetId, k -> new HashSet<>());

            if (sources.size() < 2 || sources.contains(mobId)) {
                sources.add(mobId);
                applyModifiers(target);
            }
        }

        // Cleanup entities that moved out of range
        for (Map.Entry<UUID, Set<UUID>> entry : DEPRESSED_ENTITIES.entrySet()) {
            if (entry.getValue().contains(mobId)) {
                net.minecraft.world.entity.Entity affected = level.getEntity(entry.getKey());
                if (affected instanceof LivingEntity living) {
                    if (living.distanceToSqr(entity) > 64.0D) { // Out of 8 blocks
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
        if (dmg != null && dmg.getModifier(DEPRESSING_DAMAGE_UUID) == null) {
            dmg.addTransientModifier(new AttributeModifier(DEPRESSING_DAMAGE_UUID, "Depressing Damage Debuff", -1.0D, AttributeModifier.Operation.ADDITION));
        }

        AttributeInstance armor = target.getAttribute(Attributes.ARMOR);
        if (armor != null && armor.getModifier(DEPRESSING_ARMOR_UUID) == null) {
            armor.addTransientModifier(new AttributeModifier(DEPRESSING_ARMOR_UUID, "Depressing Armor Debuff", -1.0D, AttributeModifier.Operation.ADDITION));
        }

        AttributeInstance hp = target.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null && hp.getModifier(DEPRESSING_HEALTH_UUID) == null) {
            hp.addTransientModifier(new AttributeModifier(DEPRESSING_HEALTH_UUID, "Depressing Health Debuff", -1.0D, AttributeModifier.Operation.ADDITION));
            if (target.getHealth() > target.getMaxHealth()) {
                target.setHealth(target.getMaxHealth());
            }
        }

        AttributeInstance speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && speed.getModifier(DEPRESSING_SPEED_UUID) == null) {
            speed.addTransientModifier(new AttributeModifier(DEPRESSING_SPEED_UUID, "Depressing Speed Debuff", -0.05D, AttributeModifier.Operation.ADDITION));
        }

        AttributeInstance luck = target.getAttribute(Attributes.LUCK);
        if (luck != null && luck.getModifier(DEPRESSING_LUCK_UUID) == null) {
            luck.addTransientModifier(new AttributeModifier(DEPRESSING_LUCK_UUID, "Depressing Luck Debuff", -1.0D, AttributeModifier.Operation.ADDITION));
        }
    }

    private static void removeModifiers(LivingEntity target) {
        AttributeInstance dmg = target.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null) dmg.removeModifier(DEPRESSING_DAMAGE_UUID);

        AttributeInstance armor = target.getAttribute(Attributes.ARMOR);
        if (armor != null) armor.removeModifier(DEPRESSING_ARMOR_UUID);

        AttributeInstance hp = target.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) hp.removeModifier(DEPRESSING_HEALTH_UUID);

        AttributeInstance speed = target.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.removeModifier(DEPRESSING_SPEED_UUID);

        AttributeInstance luck = target.getAttribute(Attributes.LUCK);
        if (luck != null) luck.removeModifier(DEPRESSING_LUCK_UUID);
    }
}
