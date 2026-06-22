package org.xeb.xeb.compat.hooks;

import org.xeb.xeb.compat.CompatHook;
import org.xeb.xeb.compat.ModCompatManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class MutantMonstersHook implements CompatHook {
    @Override
    public void registerTypes() {
        // Bosses
        register("mutant_zombie", true);
        register("mutant_skeleton", true);
        register("mutant_creeper", true);
        register("mutant_enderman", true);
        register("mutant_snow_golem", true);
    }

    private void register(String name, boolean isBoss) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("mutantmonsters", name));
        if (type != null) {
            if (isBoss) {
                ModCompatManager.registerBoss(type);
            } else {
                ModCompatManager.registerEligible(type);
            }
        }
    }
}
