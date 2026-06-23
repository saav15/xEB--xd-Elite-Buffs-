package org.xeb.xeb.compat.adapter;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.xeb.xeb.compat.ModCompatAdapter;
import org.xeb.xeb.weapon.WeaponClass;
import org.xeb.xeb.weapon.WeaponStyleData;

import java.util.Optional;

public class TConstructAdapter implements ModCompatAdapter {
    private static final String MOD_ID = "tconstruct";
    private final boolean loaded;

    public TConstructAdapter() {
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
        
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (rl != null && rl.getNamespace().equals(MOD_ID)) {
            String path = rl.getPath().toLowerCase();
            if (path.contains("cleaver") || path.contains("broadsword") || path.contains("scythe") || 
                path.contains("dagger") || path.contains("hammer") || path.contains("sword")) {
                return WeaponClass.MELEE;
            } else if (path.contains("bow") || path.contains("crossbow") || path.contains("shuriken")) {
                return WeaponClass.RANGED;
            }
        }
        
        String className = stack.getItem().getClass().getName().toLowerCase();
        if (className.contains("slimeknights.tconstruct") || className.contains("tconstruct")) {
            if (className.contains("bow") || className.contains("crossbow") || className.contains("ranged")) {
                return WeaponClass.RANGED;
            }
            return WeaponClass.MELEE;
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
