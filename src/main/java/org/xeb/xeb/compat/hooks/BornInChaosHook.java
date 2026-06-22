package org.xeb.xeb.compat.hooks;

import org.xeb.xeb.compat.CompatHook;
import org.xeb.xeb.compat.ModCompatManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class BornInChaosHook implements CompatHook {
    @Override
    public void registerTypes() {
        // Bosses
        register("sir_pumpkinhead", true);
        register("pumpkin_spirit", true);
        register("decayed_collector", true);

        // Mobs
        register("nightmare_stalker", false);
        register("corrupted_baron", false);
        register("bone_knight", false);
        register("withered_sir", false);
        register("baby_skeleton", false);
        register("dark_vort", false);
        register("flesh_scavenger", false);
        register("skeleton_demoman", false);
    }

    private void register(String name, boolean isBoss) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("born_in_chaos_v1", name));
        if (type != null) {
            if (isBoss) {
                ModCompatManager.registerBoss(type);
            } else {
                ModCompatManager.registerEligible(type);
            }
        }
    }
}
