package org.xeb.xeb.compat.hooks;

import org.xeb.xeb.compat.CompatHook;
import org.xeb.xeb.compat.ModCompatManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class AlexCavesHook implements CompatHook {
    @Override
    public void registerTypes() {
        // Bosses
        register("tremorzilla", true);
        register("luxtructosaurus", true);
        register("hullbreaker", true);

        // Mobs
        register("subterranodon", false);
        register("vallumraptor", false);
        register("grottol", false);
        register("underperch", false);
        register("corrodent", false);
        register("vesper", false);
        register("deep_one", false);
        register("deep_one_knight", false);
        register("deep_one_mage", false);
        register("mine_guardian", false);
        register("radgill", false);
        register("watcher", false);
        register("brainiac", false);
    }

    private void register(String name, boolean isBoss) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("alexscaves", name));
        if (type != null) {
            if (isBoss) {
                ModCompatManager.registerBoss(type);
            } else {
                ModCompatManager.registerEligible(type);
            }
        }
    }
}
