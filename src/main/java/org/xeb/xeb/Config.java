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

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean enabled;
    public static boolean medallionRenderEnabled;
    public static double medallionSizeScale;
    public static boolean colorOverlayEnabled;
    public static boolean glowEyesEnabled;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        enabled = ENABLED.get();
        medallionRenderEnabled = MEDALLION_RENDER_ENABLED.get();
        medallionSizeScale = MEDALLION_SIZE_SCALE.get();
        colorOverlayEnabled = COLOR_OVERLAY_ENABLED.get();
        glowEyesEnabled = GLOW_EYES_ENABLED.get();
    }
}
