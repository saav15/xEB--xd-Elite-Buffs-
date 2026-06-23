package org.xeb.xeb.weapon;

import net.minecraft.world.item.ItemStack;

public interface WeaponClassifier {
    WeaponClass classify(ItemStack stack);
    double confidence(ItemStack stack);
}
