package org.xeb.xeb;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Xeb.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Whether the xEB Elite Buffs system is enabled.")
            .define("enabled", true);

    public static final ForgeConfigSpec.BooleanValue MEDALLION_RENDER_ENABLED = BUILDER
            .comment("Whether to render the medallion models above elite mob heads.")
            .define("render.medallionEnabled", true);

    public static final ForgeConfigSpec.DoubleValue MEDALLION_SIZE_SCALE = BUILDER
            .comment("Size scale for the medallion models.")
            .defineInRange("render.medallionSizeScale", 1.0D, 0.5D, 2.0D);

    public static final ForgeConfigSpec.BooleanValue COLOR_OVERLAY_ENABLED = BUILDER
            .comment("Whether to overlay colors corresponding to active buffs on elite mobs.")
            .define("render.colorOverlayEnabled", true);

    public static final ForgeConfigSpec.BooleanValue GLOW_EYES_ENABLED = BUILDER
            .comment("Whether to render glowing eyes matching the elite buff color.")
            .define("render.glowEyesEnabled", true);

    public static final ForgeConfigSpec.DoubleValue MEDALLION_RENDER_DISTANCE = BUILDER
            .comment("Maximum distance in blocks to render the medallion models above elite mob heads (in blocks). 3 chunks = 48 blocks.")
            .defineInRange("render.medallionRenderDistance", 48.0D, 1.0D, 256.0D);

    // Weapon Detection Group
    public static final ForgeConfigSpec.DoubleValue WEAPON_ATTRIBUTE_CONFIDENCE_THRESHOLD = BUILDER
            .comment("Minimum confidence score (0.0-1.0) for attribute-based weapon classification.")
            .defineInRange("weaponDetection.attributeConfidenceThreshold", 0.6, 0.0, 1.0);

    public static final ForgeConfigSpec.ConfigValue<String> MOD_MATERIAL_TIER_MAPPING = BUILDER
            .comment("Maps mod material names to tier values (0-10). Format: 'material=tier,...'")
            .define("weaponDetection.modMaterialTierMapping", "adamantine=5,cobalt=3,manyullyn=4,queens_slime=4,fiery=4,nebular=5");

    public static final ForgeConfigSpec.IntValue HOTBAR_SCAN_INTERVAL_TICKS = BUILDER
            .comment("How often (in ticks) the hotbar is rescanned for weapon selection.")
            .defineInRange("weaponDetection.hotbarScanIntervalTicks", 10, 1, 100);

    public static final ForgeConfigSpec.DoubleValue LOW_DURABILITY_THRESHOLD = BUILDER
            .comment("Weapons below this durability percentage get a score penalty.")
            .defineInRange("weaponDetection.lowDurabilityThreshold", 0.1, 0.0, 1.0);

    public static final ForgeConfigSpec.DoubleValue NO_AMMO_RANGED_PENALTY = BUILDER
            .comment("Score penalty for ranged weapons when no ammo is available.")
            .defineInRange("weaponDetection.noAmmoRangedPenalty", -50.0, -100.0, 0.0);

    // Boss AI Group
    public static final ForgeConfigSpec.DoubleValue BOSS_HEALTH_THRESHOLD = BUILDER
            .comment("Entities with max health >= this value are potential bosses.")
            .defineInRange("bossAI.healthThreshold", 300.0, 50.0, 10000.0);

    public static final ForgeConfigSpec.BooleanValue FORCE_ATTACK_ON_FROZEN_BOSS = BUILDER
            .comment("If true, frozen bosses deal damage directly to targets as a last resort.")
            .define("bossAI.forceAttackOnFrozenBoss", false);

    public static final ForgeConfigSpec.BooleanValue BOSS_ATTACK_ALL_MOBS = BUILDER
            .comment("If true, bosses under Madness attack any living entity (not just players/bosses/elites).")
            .define("bossAI.bossAttackAllMobs", false);

    public static final ForgeConfigSpec.IntValue FROZEN_DETECTION_TIMEOUT_TICKS = BUILDER
            .comment("How long (ticks) a boss must be idle before being marked as 'frozen'.")
            .defineInRange("bossAI.frozenDetectionTimeoutTicks", 60, 20, 600);

    public static final ForgeConfigSpec.IntValue FORCED_MOVEMENT_DURATION_TICKS = BUILDER
            .comment("How long (ticks) forced goal injection lasts before restoring original AI.")
            .defineInRange("bossAI.forcedMovementDurationTicks", 30, 10, 100);

    public static final ForgeConfigSpec.IntValue TARGET_EXPANSION_DURATION_TICKS = BUILDER
            .comment("How long (ticks) to expand boss target candidates to all mobs when no valid targets found.")
            .defineInRange("bossAI.targetExpansionDurationTicks", 100, 20, 600);

    public static final ForgeConfigSpec.IntValue BOSS_DETECTION_CACHE_TICKS = BUILDER
            .comment("How long (ticks) to cache boss detection results per entity type.")
            .defineInRange("bossAI.bossDetectionCacheTicks", 600, 100, 6000);

    public static final ForgeConfigSpec.ConfigValue<String> MADNESS_BLACKLISTED_ENTITIES = BUILDER
            .comment("Registry names of entities blacklisted from Madness targeting, comma-separated.")
            .define("bossAI.madnessBlacklistedEntities", "");

    // Better Combat Group
    public static final ForgeConfigSpec.BooleanValue ENABLE_BETTER_COMBAT_INTEGRATION = BUILDER
            .comment("Enable Better Combat-aware weapon styles, combo timing, and special abilities.")
            .define("betterCombat.enableIntegration", true);

    public static final ForgeConfigSpec.BooleanValue AUTO_TRIGGER_LEAP = BUILDER
            .comment("Automatically jump before attacking with leap weapons.")
            .define("betterCombat.autoTriggerLeap", true);

    public static final ForgeConfigSpec.BooleanValue AUTO_TRIGGER_THRUST = BUILDER
            .comment("Automatically sprint toward target before attacking with thrust weapons.")
            .define("betterCombat.autoTriggerThrust", true);

    public static final ForgeConfigSpec.DoubleValue EMERGENCY_SWITCH_SCORE_THRESHOLD = BUILDER
            .comment("If new weapon's score exceeds current by this much, switch immediately even mid-combo.")
            .defineInRange("betterCombat.emergencySwitchScoreThreshold", 20.0, 5.0, 100.0);

    public static final ForgeConfigSpec.DoubleValue MELEE_RANGE_BUFFER_BLOCKS = BUILDER
            .comment("Extra blocks added to weapon reach for the melee/ranged decision buffer zone.")
            .defineInRange("combat.meleeRangeBufferBlocks", 1.5, 0.0, 5.0);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean enabled = true;
    public static boolean medallionRenderEnabled = true;
    public static double medallionSizeScale = 1.0D;
    public static boolean colorOverlayEnabled = true;
    public static boolean glowEyesEnabled = true;
    public static double medallionRenderDistance = 48.0D;

    // Static variables for configuration
    public static double weaponAttributeConfidenceThreshold = 0.6;
    public static String modMaterialTierMapping = "adamantine=5,cobalt=3,manyullyn=4,queens_slime=4,fiery=4,nebular=5";
    public static int hotbarScanIntervalTicks = 10;
    public static double lowDurabilityThreshold = 0.1;
    public static double noAmmoRangedPenalty = -50.0;

    public static double bossHealthThreshold = 300.0;
    public static boolean forceAttackOnFrozenBoss = false;
    public static boolean bossAttackAllMobs = false;
    public static int frozenDetectionTimeoutTicks = 60;
    public static int forcedMovementDurationTicks = 30;
    public static int targetExpansionDurationTicks = 100;
    public static int bossDetectionCacheTicks = 600;
    public static String madnessBlacklistedEntities = "";

    public static boolean enableBetterCombatIntegration = true;
    public static boolean autoTriggerLeap = true;
    public static boolean autoTriggerThrust = true;
    public static double emergencySwitchScoreThreshold = 20.0;
    public static double meleeRangeBufferBlocks = 1.5;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        enabled = ENABLED.get();
        medallionRenderEnabled = MEDALLION_RENDER_ENABLED.get();
        medallionSizeScale = MEDALLION_SIZE_SCALE.get();
        colorOverlayEnabled = COLOR_OVERLAY_ENABLED.get();
        glowEyesEnabled = GLOW_EYES_ENABLED.get();
        medallionRenderDistance = MEDALLION_RENDER_DISTANCE.get();

        // Load new configurations
        weaponAttributeConfidenceThreshold = WEAPON_ATTRIBUTE_CONFIDENCE_THRESHOLD.get();
        modMaterialTierMapping = MOD_MATERIAL_TIER_MAPPING.get();
        hotbarScanIntervalTicks = HOTBAR_SCAN_INTERVAL_TICKS.get();
        lowDurabilityThreshold = LOW_DURABILITY_THRESHOLD.get();
        noAmmoRangedPenalty = NO_AMMO_RANGED_PENALTY.get();

        bossHealthThreshold = BOSS_HEALTH_THRESHOLD.get();
        forceAttackOnFrozenBoss = FORCE_ATTACK_ON_FROZEN_BOSS.get();
        bossAttackAllMobs = BOSS_ATTACK_ALL_MOBS.get();
        frozenDetectionTimeoutTicks = FROZEN_DETECTION_TIMEOUT_TICKS.get();
        forcedMovementDurationTicks = FORCED_MOVEMENT_DURATION_TICKS.get();
        targetExpansionDurationTicks = TARGET_EXPANSION_DURATION_TICKS.get();
        bossDetectionCacheTicks = BOSS_DETECTION_CACHE_TICKS.get();
        madnessBlacklistedEntities = MADNESS_BLACKLISTED_ENTITIES.get();

        enableBetterCombatIntegration = ENABLE_BETTER_COMBAT_INTEGRATION.get();
        autoTriggerLeap = AUTO_TRIGGER_LEAP.get();
        autoTriggerThrust = AUTO_TRIGGER_THRUST.get();
        emergencySwitchScoreThreshold = EMERGENCY_SWITCH_SCORE_THRESHOLD.get();
        meleeRangeBufferBlocks = MELEE_RANGE_BUFFER_BLOCKS.get();
    }
}
