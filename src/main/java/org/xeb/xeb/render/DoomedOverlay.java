package org.xeb.xeb.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xeb.xeb.Xeb;
import org.xeb.xeb.effect.ModEffects;

/**
 * Client-side overlay for the Doomed effect.
 *
 * Draws a smooth circular vignette that closes in on the center of the screen
 * ("pinholes") as the timer counts down, ending in a pitch black screen at death.
 */
@Mod.EventBusSubscriber(modid = Xeb.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DoomedOverlay {

    private static final ResourceLocation VIGNETTE_TEXTURE = new ResourceLocation("textures/misc/vignette.png");
    private static final float SHRINK_DURATION_TICKS = 200.0F; // Shrink overlay over the last 10 seconds

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) return;

        Player player = mc.player;
        if (!player.hasEffect(ModEffects.DOOMED.get())) return;

        MobEffectInstance effect = player.getEffect(ModEffects.DOOMED.get());
        if (effect == null) return;

        int remainingTicks = effect.getDuration();
        GuiGraphics g = event.getGuiGraphics();
        int W = mc.getWindow().getGuiScaledWidth();
        int H = mc.getWindow().getGuiScaledHeight();

        // ── Compute smooth vignette scale & alpha ────────────────────────────
        // t goes from 1.0 (start of shrink window) down to 0.0 (death / 0 ticks)
        float t = Math.min(SHRINK_DURATION_TICKS, remainingTicks) / SHRINK_DURATION_TICKS;
        
        // Quadratic ease-in starting at 1.3F to push the dark vignette edges off-screen at the start
        float S = t * t * 1.3F; 
        
        // Vignette opacity goes from 0.0F (completely clear) to 1.0F (pitch black)
        float alpha = 1.0F - t;

        // Dimensions of the scaled vignette circle
        int w = (int) (W * S);
        int h = (int) (H * S);
        int x1 = W / 2 - w / 2;
        int y1 = H / 2 - h / 2;
        int x2 = W / 2 + w / 2;
        int y2 = H / 2 + h / 2;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 1. Draw the black scaled vignette in the center
        if (alpha > 0.01F) {
            RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, alpha);
            g.blit(VIGNETTE_TEXTURE, x1, y1, 0, 0.0F, 0.0F, w, h, w, h);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        // 2. Fill the outer margins with solid black to block out the rest of the screen
        int blackColor = ((int) (alpha * 255) << 24); // Fade the black margins in line with the alpha
        
        // Top margin
        if (y1 > 0) {
            g.fill(0, 0, W, y1, blackColor);
        }
        // Bottom margin
        if (y2 < H) {
            g.fill(0, y2, W, H, blackColor);
        }
        // Left margin (between top and bottom margins)
        if (x1 > 0 && y1 < y2) {
            g.fill(0, y1, x1, y2, blackColor);
        }
        // Right margin (between top and bottom margins)
        if (x2 < W && y1 < y2) {
            g.fill(x2, y1, W, y2, blackColor);
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
