package org.xeb.xeb.compat.adapter;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.xeb.xeb.compat.ModCompatAdapter;
import org.xeb.xeb.weapon.WeaponClass;
import org.xeb.xeb.weapon.WeaponStyleData;

import java.util.Optional;

public class TetraAdapter implements ModCompatAdapter {
    private static final String MOD_ID = "tetra";
    private final boolean loaded;

    public TetraAdapter() {
        boolean modLoaded = false;
        try { modLoaded = ModList.get() != null && ModList.get().isLoaded(MOD_ID); } catch (Exception | LinkageError ignored) {}
        this.loaded = modLoaded;
    }

    @Override
    public String modId() {
        return MOD_ID;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public WeaponClass classifyWeapon(ItemStack stack) {
        if (!loaded || stack.isEmpty()) return WeaponClass.NON_WEAPON;
        
        String className = stack.getItem().getClass().getName().toLowerCase();
        if (className.contains("se.mickelus.tetra") || className.contains("modularitem")) {
            if (className.contains("bow") || className.contains("crossbow") || className.contains("ranged")) {
                return WeaponClass.RANGED;
            }
            return WeaponClass.MELEE; // Default modular item to MELEE
        }
        return WeaponClass.NON_WEAPON;
    }

    @Override
    public boolean isBoss(LivingEntity entity) {
        return false;
    }

    @Override
    public Optional<WeaponStyleData> getWeaponStyle(ItemStack stack) {
        return Optional.empty();
    }

    @Override
    public boolean isItemNonWeapon(ItemStack stack) {
        return false;
    }
}
