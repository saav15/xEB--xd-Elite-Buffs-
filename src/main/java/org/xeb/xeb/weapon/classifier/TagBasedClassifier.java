package org.xeb.xeb.weapon.classifier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.xeb.xeb.weapon.WeaponClass;
import org.xeb.xeb.weapon.WeaponClassifier;

public class TagBasedClassifier implements WeaponClassifier {
    @Override
    public WeaponClass classify(ItemStack stack) {
        if (stack.isEmpty()) return WeaponClass.NON_WEAPON;
        
        // Get tags of the item
        for (TagKey<Item> tag : stack.getTags().toList()) {
            ResourceLocation loc = tag.location();
            String path = loc.getPath().toLowerCase();
            String namespace = loc.getNamespace().toLowerCase();
            
            // Check Better Combat tags
            if (namespace.equals("bettercombat")) {
                if (path.contains("swords") || path.contains("axes") || path.contains("daggers") || path.contains("spears") || path.contains("melee")) {
                    return WeaponClass.MELEE;
                }
            }
            
            // Vanilla/Forge common tags
            if (path.contains("swords") || path.contains("melee_weapons") || path.contains("greatswords") || path.contains("daggers") || path.contains("spears")) {
                return WeaponClass.MELEE;
            }
            if (path.contains("bows") || path.contains("crossbows") || path.contains("ranged_weapons")) {
                return WeaponClass.RANGED;
            }
            if (path.contains("axes")) {
                return WeaponClass.MELEE; // Axes are melee weapons
            }
            
            // Epic fight
            if (namespace.equals("epicfight")) {
                if (path.contains("weapon") || path.contains("sword") || path.contains("axe") || path.contains("spear") || path.contains("melee")) {
                    return WeaponClass.MELEE;
                }
            }
            
            // Iron's Spells
            if (namespace.equals("irons_spells")) {
                if (path.contains("spellbook") || path.contains("staves") || path.contains("wands")) {
                    return WeaponClass.MAGIC;
                }
            }
        }
        
        // Secondary tool check
        for (TagKey<Item> tag : stack.getTags().toList()) {
            String path = tag.location().getPath().toLowerCase();
            if (path.contains("pickaxes") || path.contains("shovels") || path.contains("tools") || path.contains("hoes")) {
                return WeaponClass.TOOL;
            }
        }
        
        return WeaponClass.NON_WEAPON;
    }

    @Override
    public double confidence(ItemStack stack) {
        return classify(stack) != WeaponClass.NON_WEAPON ? 0.95 : 0.0;
    }
}
