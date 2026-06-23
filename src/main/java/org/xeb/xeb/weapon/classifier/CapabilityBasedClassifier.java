package org.xeb.xeb.weapon.classifier;

import net.minecraft.world.item.ItemStack;
import org.xeb.xeb.compat.ModCompatAdapter;
import org.xeb.xeb.compat.ModCompatManager;
import org.xeb.xeb.weapon.WeaponClass;
import org.xeb.xeb.weapon.WeaponClassifier;

public class CapabilityBasedClassifier implements WeaponClassifier {
    @Override
    public WeaponClass classify(ItemStack stack) {
        if (stack.isEmpty()) return WeaponClass.NON_WEAPON;

        // Iterate through all active mod compatibility adapters to inspect capabilities
        for (ModCompatAdapter adapter : ModCompatManager.getAdapters()) {
            if (adapter.isLoaded()) {
                try {
                    WeaponClass result = adapter.classifyWeapon(stack);
                    if (result != null && result != WeaponClass.NON_WEAPON) {
                        return result;
                    }
                } catch (Exception ignored) {}
            }
        }

        return WeaponClass.NON_WEAPON;
    }

    @Override
    public double confidence(ItemStack stack) {
        return classify(stack) != WeaponClass.NON_WEAPON ? 0.9 : 0.0;
    }
}
