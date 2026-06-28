package org.xeb.xeb.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xeb.xeb.Xeb;

@Mod.EventBusSubscriber(modid = Xeb.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DoomfistHUDOverlay {

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null || mc.options.hideGui) return;

            Player player = mc.player;
            
            // Render the bottom-left ability cooldown icons if player is holding the Doomfist v1
            boolean holdsDoomfist = player.getMainHandItem().is(org.xeb.xeb.item.ModItems.DOOMFIST.get()) 
                    || player.getOffhandItem().is(org.xeb.xeb.item.ModItems.DOOMFIST.get());
            if (holdsDoomfist) {
                renderAbilityCooldowns(event, mc, player);
            }

            if (player.isUsingItem() && player.getUseItem().is(org.xeb.xeb.item.ModItems.DOOMFIST.get())) {
                int ticksUsing = player.getTicksUsingItem();
                float progress = Math.min(50.0F, ticksUsing) / 50.0F; // 2.5s = 50 ticks

                int width = event.getWindow().getGuiScaledWidth();
                int height = event.getWindow().getGuiScaledHeight();

                // Center position below crosshair
                int barW = 49; // 4 segments * 10 width + 3 spacing * 3
                int barH = 4;
                int x = width / 2 - barW / 2;
                int y = height / 2 + 15;

                GuiGraphics g = event.getGuiGraphics();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                int segmentCount = 4;
                int segmentW = 10;
                int segmentSpacing = 3;

                // Color definition: if fully charged, pulse bright electric blue; otherwise clean white/light blue
                int fillColor;
                if (progress >= 1.0F) {
                    float pulse = 0.6F + 0.4F * (float) Math.sin((mc.level.getGameTime() + event.getPartialTick()) * 0.4D);
                    int r = (int) (100 * pulse);
                    int gVal = (int) (200 * pulse + 55);
                    int b = 255;
                    fillColor = (0xFF << 24) | (r << 16) | (gVal << 8) | b;
                } else {
                    fillColor = 0xFFE5F5FF; // Soft clean white/ice-blue (Overwatch default)
                }

                // Draw each segment depending on progress
                for (int i = 0; i < segmentCount; i++) {
                    float segmentThreshold = (i + 1) / (float) segmentCount;
                    int segX = x + i * (segmentW + segmentSpacing);

                    // 1. Draw outer black border slanted bar (width + 2, height + 2)
                    drawSlantedBar(g, segX - 1, y - 1, segmentW + 2, barH + 2, 0xFF000000);

                    // 2. Draw inner dark transparent background slanted bar
                    drawSlantedBar(g, segX, y, segmentW, barH, 0x88222222);

                    // 3. Draw filled slanted bar based on progress
                    if (progress >= segmentThreshold) {
                        drawSlantedBar(g, segX, y, segmentW, barH, fillColor);
                    } else if (progress > i / (float) segmentCount) {
                        float segmentProgress = (progress - (i / (float) segmentCount)) * segmentCount;
                        int partialW = (int) (segmentW * segmentProgress);
                        drawSlantedBar(g, segX, y, partialW, barH, fillColor);
                    }
                }

                RenderSystem.disableBlend();
            }
        }
    }

    /**
     * Renders a slanted parallelogram block.
     */
    private static void drawSlantedBar(GuiGraphics g, int x, int y, int width, int height, int color) {
        for (int dy = 0; dy < height; dy++) {
            int offset = (height - 1 - dy); // Slants to the right (Overwatch style)
            g.fill(x + offset, y + dy, x + width + offset, y + dy + 1, color);
        }
    }

    private static void renderAbilityCooldowns(RenderGuiOverlayEvent.Post event, Minecraft mc, Player player) {
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        
        // Bottom left corner placement
        int xStart = 10;
        int yStart = height - 42;
        
        GuiGraphics g = event.getGuiGraphics();
        net.minecraft.nbt.CompoundTag tag = player.getPersistentData();
        
        int uppercutCD = tag.contains("xebUppercutCooldownTicks") ? tag.getInt("xebUppercutCooldownTicks") : 0;
        int slamCD = tag.contains("xebSlamCooldownTicks") ? tag.getInt("xebSlamCooldownTicks") : 0;
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        // Render Uppercut Icon Box (G)
        renderAbilityIconBox(g, mc, xStart, yStart, "G", "UPPER", uppercutCD, 100);
        
        // Render Seismic Slam Icon Box (H)
        renderAbilityIconBox(g, mc, xStart + 30, yStart, "H", "SLAM", slamCD, 120);
        
        RenderSystem.disableBlend();
    }
    
    private static void renderAbilityIconBox(GuiGraphics g, Minecraft mc, int x, int y, String key, String label, int cd, int maxCd) {
        int boxW = 24;
        int boxH = 24;
        
        // Outer black border
        g.fill(x - 1, y - 1, x + boxW + 1, y + boxH + 1, 0xFF000000);
        
        // Background
        g.fill(x, y, x + boxW, y + boxH, 0x44000000);
        
        // Cooldown Progress Overlay (fills from bottom to top)
        if (cd > 0) {
            float ratio = cd / (float) maxCd;
            int overlayH = (int) (boxH * ratio);
            g.fill(x, y + boxH - overlayH, x + boxW, y + boxH, 0x99555555); // Solid gray semi-transparent overlay
        }
        
        // Inner frame border (white transparent if ready, darker transparent if on cooldown)
        int borderColor = (cd > 0) ? 0x44FFFFFF : 0xBBFFFFFF;
        g.fill(x, y, x + boxW, y + 1, borderColor); // Top
        g.fill(x, y + boxH - 1, x + boxW, y + boxH, borderColor); // Bottom
        g.fill(x, y, x + 1, y + boxH, borderColor); // Left
        g.fill(x + boxW - 1, y, x + boxW, y + boxH, borderColor); // Right
        
        // Key label (centered, e.g. "G" or "H")
        int keyColor = (cd > 0) ? 0x88AAAAAA : 0xFFFFFFFF;
        int keyX = x + (boxW - mc.font.width(key)) / 2;
        int keyY = y + (boxH - mc.font.lineHeight) / 2;
        g.drawString(mc.font, key, keyX, keyY, keyColor, false);
        
        // Ability tiny label above the box
        int labelColor = (cd > 0) ? 0x44FFFFFF : 0x88FFFFFF;
        g.drawString(mc.font, label, x, y - 9, labelColor, false);
        
        // Cooldown time remaining (centered below the box)
        if (cd > 0) {
            String timeText = String.format("%.1fs", cd / 20.0F);
            int timeX = x + (boxW - mc.font.width(timeText)) / 2;
            g.drawString(mc.font, timeText, timeX, y + boxH + 2, 0xFFFFAA00, true);
        }
    }
}
