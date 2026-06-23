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

public class ArtifactsAdapter implements ModCompatAdapter {
    private static final String MOD_ID = "artifacts";
    private final boolean loaded;

    public ArtifactsAdapter() {
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
            if (path.contains("glove") || path.contains("gauntlet") || 
                path.contains("claw") || path.contains("dagger")) {
                return WeaponClass.MELEE;
            }
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
