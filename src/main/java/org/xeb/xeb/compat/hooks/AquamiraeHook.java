package org.xeb.xeb.compat.hooks;

import org.xeb.xeb.compat.CompatHook;
import org.xeb.xeb.compat.ModCompatManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class AquamiraeHook implements CompatHook {
    @Override
    public void registerTypes() {
        // Bosses
        register("captain_cornelia", true);

        // Mobs
        register("anglerfish", false);
        register("maze", false);
        register("eel", false);
        register("maw", false);
        register("tortured_soul", false);
    }

    private void register(String name, boolean isBoss) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("aquamirae", name));
        if (type != null) {
            if (isBoss) {
                ModCompatManager.registerBoss(type);
            } else {
                ModCompatManager.registerEligible(type);
            }
        }
    }
}
