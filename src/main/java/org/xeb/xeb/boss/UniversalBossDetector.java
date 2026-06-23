package org.xeb.xeb.boss;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.ForgeRegistries;
import org.xeb.xeb.Config;
import org.xeb.xeb.compat.ModCompatManager;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Universal detector to classify if an entity type is a boss using metadata heuristics,
 * health thresholds, namespaces, and reflective checks.
 */
public class UniversalBossDetector {
    private static final Map<EntityType<?>, CachedBossResult> CACHE = new WeakHashMap<>();

    /**
     * Checks if the entity is blacklisted from Madness effects or targeting.
     */
    public static boolean isBlacklisted(LivingEntity entity) {
        if (entity == null) return true;
        ResourceLocation rl = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (rl == null) return false;
        String regName = rl.toString();
        String blacklist = Config.madnessBlacklistedEntities;
        if (blacklist != null && !blacklist.isEmpty()) {
            for (String entry : blacklist.split(",")) {
                if (entry.trim().equalsIgnoreCase(regName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Resolves whether a LivingEntity should be classified as a Boss under Madness logic.
     */
    public static boolean isBoss(LivingEntity entity) {
        if (entity == null) return false;
        if (isBlacklisted(entity)) return false;

        EntityType<?> type = entity.getType();
        long now = System.currentTimeMillis();
        long cacheDurationMs = Config.bossDetectionCacheTicks * 50L; // 600 ticks = 30000ms

        synchronized (CACHE) {
            CachedBossResult cached = CACHE.get(type);
            if (cached != null && (now - cached.timestamp) < cacheDurationMs) {
                return cached.isBoss;
            }
        }

        boolean isBossResult = evaluateBoss(entity);

        synchronized (CACHE) {
            CACHE.put(type, new CachedBossResult(isBossResult, now));
        }

        return isBossResult;
    }

    private static boolean evaluateBoss(LivingEntity entity) {
        // CHECK 1: ModCompatManager JSON tags or registered boss list
        if (ModCompatManager.isBossLegacy(entity)) {
            return true;
        }

        // CHECK 2: Health threshold
        boolean highHealth = entity.getMaxHealth() >= Config.bossHealthThreshold;

        // CHECK 3: Reflective boss bar field presence
        boolean hasBossBar = hasBossInfoField(entity);

        // CHECK 4: MISC category with high health
        boolean isMisc = entity.getType().getCategory() == MobCategory.MISC;

        // CHECK 5: Mod namespace known for bosses
        ResourceLocation rl = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        boolean isBossModNamespace = false;
        if (rl != null) {
            String ns = rl.getNamespace().toLowerCase();
            if (ns.equals("cataclysm") || ns.equals("alexsmobs") || ns.equals("mutagen") || 
                ns.equals("mowziesmobs") || ns.equals("twilightforest")) {
                isBossModNamespace = true;
            }
        }

        // CHECK 6: Class name check
        boolean classNameMatch = entity.getClass().getSimpleName().toLowerCase().contains("boss");

        // Aggregated logic: CHECK 1 OR (CHECK 2 AND (CHECK 3 OR CHECK 4 OR CHECK 5 OR CHECK 6))
        return highHealth && (hasBossBar || isMisc || isBossModNamespace || classNameMatch);
    }

    private static boolean hasBossInfoField(LivingEntity entity) {
        Class<?> clazz = entity.getClass();
        while (clazz != null && clazz != LivingEntity.class) {
            try {
                for (Field field : clazz.getDeclaredFields()) {
                    if (net.minecraft.world.BossEvent.class.isAssignableFrom(field.getType())) {
                        return true;
                    }
                }
            } catch (NoClassDefFoundError | Exception ignored) {}
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    private static class CachedBossResult {
        final boolean isBoss;
        final long timestamp;

        CachedBossResult(boolean isBoss, long timestamp) {
            this.isBoss = isBoss;
            this.timestamp = timestamp;
        }
    }
}
