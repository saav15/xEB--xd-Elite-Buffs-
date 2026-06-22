package org.xeb.xeb.compat.hooks;

import org.xeb.xeb.compat.CompatHook;
import org.xeb.xeb.compat.ModCompatManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class TwilightForestHook implements CompatHook {
    @Override
    public void registerTypes() {
        // Bosses
        register("naga", true);
        register("lich", true);
        register("hydra", true);
        register("ur_ghast", true);
        register("minoshroom", true);
        register("alpha_yeti", true);
        register("snow_queen", true);

        // Mobs
        register("minotaur", false);
        register("redcap", false);
        register("redcap_sapper", false);
        register("kobold", false);
        register("death_tome", false);
        register("fire_beetle", false);
        register("slime_beetle", false);
        register("pinch_beetle", false);
        register("maze_slime", false);
        register("carminite_ghastguard", false);
        register("carminite_golem", false);
        register("yeti", false);
        register("giant_miner", false);
    }

    private void register(String name, boolean isBoss) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("twilightforest", name));
        if (type != null) {
            if (isBoss) {
                ModCompatManager.registerBoss(type);
            } else {
                ModCompatManager.registerEligible(type);
            }
        }
    }
}
