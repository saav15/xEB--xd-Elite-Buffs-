package org.xeb.xeb.compat.hooks;

import org.xeb.xeb.compat.CompatHook;
import org.xeb.xeb.compat.ModCompatManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class CataclysmHook implements CompatHook {
    @Override
    public void registerTypes() {
        // Bosses
        register("ignis", true);
        register("ender_guardian", true);
        register("netherite_monstrosity", true);
        register("the_harbinger", true);
        register("leviathan", true);
        register("ancient_remnant", true);

        // Mobs
        register("ignited_revenant", false);
        register("deepling", false);
        register("deepling_angler", false);
        register("deepling_brute", false);
        register("deepling_warlock", false);
        register("coralssus", false);
        register("amethyst_crab", false);
    }

    private void register(String name, boolean isBoss) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("cataclysm", name));
        if (type != null) {
            if (isBoss) {
                ModCompatManager.registerBoss(type);
            } else {
                ModCompatManager.registerEligible(type);
            }
        }
    }
}
