package org.xeb.xeb.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.xeb.xeb.weapon.WeaponClass;
import org.xeb.xeb.weapon.WeaponStyleData;

import java.util.Optional;

public interface ModCompatAdapter {
    String modId();
    boolean isLoaded();
    WeaponClass classifyWeapon(ItemStack stack);
    boolean isBoss(LivingEntity entity);
    Optional<WeaponStyleData> getWeaponStyle(ItemStack stack);
    boolean isItemNonWeapon(ItemStack stack);
}
