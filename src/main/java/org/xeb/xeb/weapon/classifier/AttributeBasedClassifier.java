package org.xeb.xeb.weapon.classifier;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.xeb.xeb.Config;
import org.xeb.xeb.weapon.WeaponClass;
import org.xeb.xeb.weapon.WeaponClassifier;

import java.util.Collection;
import java.util.Map;

public class AttributeBasedClassifier implements WeaponClassifier {
    @Override
    public WeaponClass classify(ItemStack stack) {
        if (stack.isEmpty()) return WeaponClass.NON_WEAPON;

        double attackDamage = 0.0;
        double attackSpeed = 4.0; // Default base attack speed is 4.0 in Minecraft
        boolean hasAttackDamage = false;
        boolean hasRangedDamage = false;

        // Check ShootableItem
        boolean isShootable = stack.getItem() instanceof ProjectileWeaponItem;

        try {
            var modifiersMap = stack.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            for (Map.Entry<Attribute, Collection<AttributeModifier>> entry : modifiersMap.asMap().entrySet()) {
                Attribute attribute = entry.getKey();
                if (attribute == null) continue;

                double totalMod = 0.0;
                for (AttributeModifier mod : entry.getValue()) {
                    totalMod += mod.getAmount();
                }

                if (attribute == Attributes.ATTACK_DAMAGE) {
                    attackDamage = totalMod;
                    if (attackDamage > 0) {
                        hasAttackDamage = true;
                    }
                } else if (attribute == Attributes.ATTACK_SPEED) {
                    attackSpeed = 4.0 + totalMod;
                } else {
                    String name = attribute.getDescriptionId().toLowerCase();
                    if (name.contains("ranged_attack_damage") || name.contains("ranged_damage") || 
                        name.contains("projectile_damage") || name.contains("bullet_damage")) {
                        hasRangedDamage = true;
                    }
                }
            }
        } catch (Exception ignored) {}

        double conf = confidenceScore(hasAttackDamage, isShootable, hasRangedDamage);
        if (conf >= Config.weaponAttributeConfidenceThreshold) {
            if (hasAttackDamage && (isShootable || hasRangedDamage)) {
                return WeaponClass.HYBRID;
            } else if (hasAttackDamage) {
                return WeaponClass.MELEE;
            } else if (isShootable || hasRangedDamage) {
                return WeaponClass.RANGED;
            }
        }

        return WeaponClass.NON_WEAPON;
    }

    @Override
    public double confidence(ItemStack stack) {
        if (stack.isEmpty()) return 0.0;

        double attackDamage = 0.0;
        boolean hasAttackDamage = false;
        boolean hasRangedDamage = false;
        boolean isShootable = stack.getItem() instanceof ProjectileWeaponItem;

        try {
            var modifiersMap = stack.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            for (Map.Entry<Attribute, Collection<AttributeModifier>> entry : modifiersMap.asMap().entrySet()) {
                Attribute attribute = entry.getKey();
                if (attribute == null) continue;

                double totalMod = 0.0;
                for (AttributeModifier mod : entry.getValue()) {
                    totalMod += mod.getAmount();
                }

                if (attribute == Attributes.ATTACK_DAMAGE) {
                    attackDamage = totalMod;
                    if (attackDamage > 0) {
                        hasAttackDamage = true;
                    }
                } else {
                    String name = attribute.getDescriptionId().toLowerCase();
                    if (name.contains("ranged_attack_damage") || name.contains("ranged_damage") || 
                        name.contains("projectile_damage") || name.contains("bullet_damage")) {
                        hasRangedDamage = true;
                    }
                }
            }
        } catch (Exception ignored) {}

        return confidenceScore(hasAttackDamage, isShootable, hasRangedDamage);
    }

    private double confidenceScore(boolean hasAttackDamage, boolean isShootable, boolean hasRangedDamage) {
        if (hasAttackDamage && (isShootable || hasRangedDamage)) {
            return 0.8;
        } else if (hasAttackDamage) {
            return 0.7;
        } else if (isShootable || hasRangedDamage) {
            return 0.8;
        }
        return 0.0;
    }
}
