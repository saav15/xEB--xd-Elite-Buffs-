package org.xeb.xeb.weapon.classifier;

import net.minecraft.world.item.*;
import org.xeb.xeb.weapon.WeaponClass;
import org.xeb.xeb.weapon.WeaponClassifier;

public class ClassBasedClassifier implements WeaponClassifier {
    @Override
    public WeaponClass classify(ItemStack stack) {
        if (stack.isEmpty()) return WeaponClass.NON_WEAPON;
        Item item = stack.getItem();
        
        // Vanilla checks
        if (item instanceof SwordItem || item instanceof AxeItem) {
            return WeaponClass.MELEE;
        }
        if (item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem) {
            return WeaponClass.RANGED;
        }
        if (item instanceof DiggerItem) {
            return WeaponClass.TOOL;
        }
        if (item instanceof ProjectileWeaponItem) {
            return WeaponClass.RANGED;
        }

        // Mod-specific dynamic class checks
        if (isInstanceOf(item, "net.titanium.item.SwordItem")) {
            return WeaponClass.MELEE;
        }
        if (isInstanceOf(item, "dev.xkmc.l2weaponry.content.item.MeleeWeapon")) {
            return WeaponClass.MELEE;
        }
        if (isInstanceOf(item, "yesman.epicfight.world.item.WeaponItem")) {
            return WeaponClass.MELEE;
        }
        
        return WeaponClass.NON_WEAPON;
    }

    @Override
    public double confidence(ItemStack stack) {
        return classify(stack) != WeaponClass.NON_WEAPON ? 0.85 : 0.0;
    }

    private boolean isInstanceOf(Object obj, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return clazz.isInstance(obj);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
