package org.xeb.xeb.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xeb.xeb.Xeb;
import org.xeb.xeb.event.HolyShieldClientHandler;

@Mod.EventBusSubscriber(modid = Xeb.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class HolyShieldOverlay {
    private static final ResourceLocation CROSS_TEXTURE = new ResourceLocation(Xeb.MODID, "textures/gui/holy_shield_hud.png");

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        // Render right after player health bar is drawn
        if (event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_HEALTH.id())) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null || mc.options.hideGui) return;

            Player player = mc.player;
            if (HolyShieldClientHandler.isHolyShieldActive(player)) {
                int width = event.getWindow().getGuiScaledWidth();
                int height = event.getWindow().getGuiScaledHeight();

                // 12 pixels to the left of the health bar (hearts start at width / 2 - 91)
                int x = width / 2 - 91 - 12;
                int y = height - 39; // Vertically aligned with the hearts line

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                // Render the 9x9 blue-and-white cross icon
                event.getGuiGraphics().blit(CROSS_TEXTURE, x, y, 0, 0.0F, 0.0F, 9, 9, 9, 9);

                RenderSystem.disableBlend();
            }
        }
    }
}
