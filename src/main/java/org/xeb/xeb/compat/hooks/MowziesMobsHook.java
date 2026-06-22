package org.xeb.xeb.compat.hooks;

import org.xeb.xeb.compat.CompatHook;
import org.xeb.xeb.compat.ModCompatManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class MowziesMobsHook implements CompatHook {
    @Override
    public void registerTypes() {
        // Bosses
        register("ferrous_wroughtnaut", true);
        register("barako", true);
        register("frostmaw", true);

        // Mobs
        register("foliaath", false);
        register("barakoan_rider", false);
        register("barakoana", false);
        register("barakoan_sentinel", false);
        register("grottol", false);
        register("naga", false);
    }

    private void register(String name, boolean isBoss) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("mowziesmobs", name));
        if (type != null) {
            if (isBoss) {
                ModCompatManager.registerBoss(type);
            } else {
                ModCompatManager.registerEligible(type);
            }
        }
    }
}
