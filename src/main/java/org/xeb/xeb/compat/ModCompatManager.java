package org.xeb.xeb.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;
import org.xeb.xeb.Xeb;
import org.xeb.xeb.compat.hooks.*;

import java.util.HashSet;
import java.util.Set;

public class ModCompatManager {
    private static final Set<EntityType<?>> ELIGIBLE_TYPES = new HashSet<>();
    private static final Set<EntityType<?>> BOSS_TYPES = new HashSet<>();

    private static final TagKey<EntityType<?>> ELITE_ELIGIBLE_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(Xeb.MODID, "elite_eligible"));
    private static final TagKey<EntityType<?>> BOSSES_TAG = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(Xeb.MODID, "bosses"));

    public static void registerEligible(EntityType<?> type) {
        if (type != null) ELIGIBLE_TYPES.add(type);
    }

    public static void registerBoss(EntityType<?> type) {
        if (type != null) {
            BOSS_TYPES.add(type);
            ELIGIBLE_TYPES.add(type); // bosses are also eligible
        }
    }

    public static boolean isEligible(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        // Check dynamic set
        if (ELIGIBLE_TYPES.contains(type)) return true;
        // Check JSON tags
        return type.is(ELITE_ELIGIBLE_TAG);
    }

    public static boolean isBoss(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        // Check dynamic set
        if (BOSS_TYPES.contains(type)) return true;
        // Check JSON tags
        if (type.is(BOSSES_TAG)) return true;
        // Fallback: Wither/Dragon or max health >= 100
        return entity.getMaxHealth() >= 100.0F;
    }

    public static void init() {
        // Vanilla is always loaded
        new VanillaMobHook().registerTypes();

        if (ModList.get().isLoaded("twilightforest")) {
            new TwilightForestHook().registerTypes();
        }
        if (ModList.get().isLoaded("mowziesmobs")) {
            new MowziesMobsHook().registerTypes();
        }
        if (ModList.get().isLoaded("alexsmobs")) {
            new AlexMobsHook().registerTypes();
        }
        if (ModList.get().isLoaded("alexscaves")) {
            new AlexCavesHook().registerTypes();
        }
        if (ModList.get().isLoaded("mutantmonsters")) {
            new MutantMonstersHook().registerTypes();
        }
        if (ModList.get().isLoaded("cataclysm")) {
            new CataclysmHook().registerTypes();
        }
        if (ModList.get().isLoaded("born_in_chaos_v1")) {
            new BornInChaosHook().registerTypes();
        }
        if (ModList.get().isLoaded("aquamirae")) {
            new AquamiraeHook().registerTypes();
        }
        if (ModList.get().isLoaded("aether")) {
            new AetherHook().registerTypes();
        }
        if (ModList.get().isLoaded("bettercombat")) {
            new BetterCombatHook().registerTypes();
        }
    }
}
