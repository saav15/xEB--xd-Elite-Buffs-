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

public class AlexsMobsAdapter implements ModCompatAdapter {
    private static final String MOD_ID = "alexsmobs";
    private final boolean loaded;

    public AlexsMobsAdapter() {
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
        return WeaponClass.NON_WEAPON;
    }

    @Override
    public boolean isBoss(LivingEntity entity) {
        if (!loaded || entity == null) return false;
        ResourceLocation rl = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (rl != null && rl.getNamespace().equals(MOD_ID)) {
            String path = rl.getPath().toLowerCase();
            return path.equals("void_worm") || path.equals("warped_mosco") || path.equals("centipede");
        }
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
