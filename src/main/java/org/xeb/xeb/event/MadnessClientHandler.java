package org.xeb.xeb.event;

import org.xeb.xeb.Xeb;
import org.xeb.xeb.effect.ModEffects;
import org.xeb.xeb.bot.PlayerBotStateMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Xeb.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MadnessClientHandler {

    private static boolean wasActive = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !player.isAlive()) {
            if (wasActive) {
                reset();
            }
            return;
        }

        if (!player.hasEffect(ModEffects.MADNESS.get())) {
            if (wasActive) {
                reset();
            }
            return;
        }

        wasActive = true;
        PlayerBotStateMachine.tick(mc, player);
    }

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        Player player = event.getEntity();
        if (player == null || !player.level().isClientSide() || !(player instanceof LocalPlayer localPlayer)) return;
        if (!player.hasEffect(ModEffects.MADNESS.get())) return;
        if (PlayerBotStateMachine.getCurrentTarget() == null) return;

        PlayerBotStateMachine.applyMovementInput(event.getInput(), localPlayer);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.VIGNETTE.id())) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.hasEffect(ModEffects.MADNESS.get())) {
                renderRedBorder(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
            }
        }
    }

    private static void renderRedBorder(net.minecraft.client.gui.GuiGraphics guiGraphics, int width, int height) {
        com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
        com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();

        float time = (Minecraft.getInstance().level.getGameTime() + Minecraft.getInstance().getFrameTime());
        float alpha = 0.45F + 0.15F * (float) Math.sin(time / 6.0F);
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 0.0F, 0.0F, alpha);

        guiGraphics.blit(new ResourceLocation("textures/misc/vignette.png"), 0, 0, 0, 0.0F, 0.0F, width, height, width, height);

        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
        com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
    }

    @SubscribeEvent
    public static void onComputeFovModifier(net.minecraftforge.client.event.ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        if (player.isUsingItem() && player.getUseItem().is(org.xeb.xeb.item.ModItems.DOOMFIST.get())) {
            int ticksCharged = player.getTicksUsingItem();
            float chargeRatio = Math.min(50.0F, ticksCharged) / 50.0F;
            if (chargeRatio > 0.0F) {
                // Enlarge FOV (zoom out effect) by up to 25% based on charge ratio
                event.setNewFovModifier(event.getNewFovModifier() + (chargeRatio * 0.25F));
            }
        }
    }

    private static void reset() {
        wasActive = false;
        PlayerBotStateMachine.reset();
    }
}
