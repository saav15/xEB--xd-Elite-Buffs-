package org.xeb.xeb.compat.hooks;

import org.xeb.xeb.compat.CompatHook;
import org.xeb.xeb.compat.ModCompatManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class AetherHook implements CompatHook {
    @Override
    public void registerTypes() {
        // Bosses
        register("slider", true);
        register("valkyrie_queen", true);
        register("sun_spirit", true);

        // Mobs
        register("cockatrice", false);
        register("sentry", false);
        register("mimic", false);
        register("zephyr", false);
        register("valkyrie", false);
    }

    private void register(String name, boolean isBoss) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("aether", name));
        if (type != null) {
            if (isBoss) {
                ModCompatManager.registerBoss(type);
            } else {
                ModCompatManager.registerEligible(type);
            }
        }
    }
}
