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

public class IronSpellsAdapter implements ModCompatAdapter {
    private static final String MOD_ID = "irons_spellbooks";
    private final boolean loaded;

    public IronSpellsAdapter() {
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
            if (path.contains("spellbook") || path.contains("staff") || 
                path.contains("wand") || path.contains("tome") || path.contains("scroll")) {
                return WeaponClass.MAGIC;
            }
        }
        String className = stack.getItem().getClass().getName().toLowerCase();
        if (className.contains("spellbook") || className.contains("wand") || className.contains("staff")) {
            return WeaponClass.MAGIC;
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
