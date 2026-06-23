package org.xeb.xeb.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xeb.xeb.Config;
import org.xeb.xeb.Xeb;
import org.xeb.xeb.medallion.MedallionData;
import org.xeb.xeb.medallion.MedallionManager;
import org.xeb.xeb.render.MedallionRenderLayer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central CLIENT-ONLY event handler that ensures medallion rendering, color overlay,
 * and Mega scaling works for ALL LivingEntity types — including Creepers — using
 * RenderLivingEvent which fires reliably for every entity rendered by LivingEntityRenderer.
 *
 * Uses RenderLivingEvent.Pre/Post which fire for ALL mobs that use LivingEntityRenderer,
 * including CreeperRenderer, without depending on layer injection.
 */
@Mod.EventBusSubscriber(modid = Xeb.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientMedallionEventHandler {

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (entity == null) return;

        List<MedallionData> medallions = MedallionManager.getMedallions(entity);
        if (medallions.isEmpty()) return;

        int megaCount = 0;
        for (MedallionData m : medallions) {
            if (m.getBuff().getId().equals("mega")) megaCount++;
        }
        if (megaCount > 0) {
            float scaleFactor = 1.0F + 0.30F * megaCount;
            event.getPoseStack().pushPose();
            event.getPoseStack().scale(scaleFactor, scaleFactor, scaleFactor);
        }
    }

    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (entity == null) return;

        List<MedallionData> medallions = MedallionManager.getMedallions(entity);
        if (medallions.isEmpty()) return;

        int megaCount = 0;
        for (MedallionData m : medallions) {
            if (m.getBuff().getId().equals("mega")) megaCount++;
        }

        // Pop the Mega scale pose that was pushed in Pre
        if (megaCount > 0) {
            event.getPoseStack().popPose();
        }

        // If this renderer already has MedallionRenderLayer, it handles its own rendering.
        // Skip fallback to avoid double rendering.
        LivingEntityRenderer renderer = (LivingEntityRenderer) event.getRenderer();
        if (rendererHasMedallionLayer(renderer)) return;

        // ---- FALLBACK: render for mobs whose renderer has no injected MedallionRenderLayer ----
        if (!Config.medallionRenderEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        double distSq = entity.distanceToSqr(mc.player);
        if (distSq > Config.medallionRenderDistance * Config.medallionRenderDistance) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();
        float partialTick = event.getPartialTick();

        // Color overlay using the entity's own texture + model
        if (Config.colorOverlayEnabled) {
            renderColorOverlayRaw(poseStack, bufferSource, packedLight, entity, partialTick, medallions, renderer);
        }

        // Medallion 3D models above the entity's head
        org.joml.Vector3f dir = new org.joml.Vector3f(0.0F, 1.0F, 0.0F);
        poseStack.last().pose().transformDirection(dir);
        float scale = dir.length();
        if (scale < 0.001F) scale = 1.0F;

        float worldHeight = MedallionRenderLayer.getMedallionWorldHeight(entity);
        float extraMegaOffset = megaCount * 0.35F * scale;
        float heightOffset = ((worldHeight + extraMegaOffset) / scale) - 1.501F;

        poseStack.pushPose();
        poseStack.translate(0.0F, -heightOffset, 0.0F);
        MedallionRenderLayer.renderMedallionsStatic(poseStack, bufferSource, packedLight, entity, partialTick);
        poseStack.popPose();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void renderColorOverlayRaw(PoseStack poseStack, MultiBufferSource bufferSource,
                                              int packedLight, LivingEntity entity,
                                              float partialTick, List<MedallionData> medallions,
                                              LivingEntityRenderer renderer) {
        try {
            float time = (entity.tickCount + partialTick) / 20.0F;
            int colorCount = medallions.size();
            float red, green, blue;

            if (colorCount == 1) {
                int color = medallions.get(0).getBuff().getColor();
                red = ((color >> 16) & 0xFF) / 255.0F;
                green = ((color >> 8) & 0xFF) / 255.0F;
                blue = (color & 0xFF) / 255.0F;
            } else {
                float indexFloat = (time * 0.7F) % colorCount;
                int index1 = (int) Math.floor(indexFloat);
                int index2 = (index1 + 1) % colorCount;
                float progress = indexFloat - index1;
                int c1 = medallions.get(index1).getBuff().getColor();
                int c2 = medallions.get(index2).getBuff().getColor();
                red = ((c1 >> 16) & 0xFF) / 255.0F + (((c2 >> 16) & 0xFF) / 255.0F - ((c1 >> 16) & 0xFF) / 255.0F) * progress;
                green = ((c1 >> 8) & 0xFF) / 255.0F + (((c2 >> 8) & 0xFF) / 255.0F - ((c1 >> 8) & 0xFF) / 255.0F) * progress;
                blue = (c1 & 0xFF) / 255.0F + ((c2 & 0xFF) / 255.0F - (c1 & 0xFF) / 255.0F) * progress;
            }

            float alpha = 0.86F + (float) Math.sin((entity.tickCount + partialTick) / 3.5F) * 0.12F;
            float height = entity.getBbHeight();

            ResourceLocation texture = renderer.getTextureLocation(entity);
            EntityModel model = renderer.getModel();

            poseStack.pushPose();
            poseStack.translate(0.0D, -height / 2.0D, 0.0D);
            poseStack.scale(1.015F, 1.015F, 1.015F);
            poseStack.translate(0.0D, height / 2.0D, 0.0D);
            VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
            model.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
            poseStack.popPose();
        } catch (Exception ignored) {}
    }

    // ----- Layer detection (cached via reflection) -----

    private static final Map<Class<?>, Field> LAYER_FIELD_CACHE = new HashMap<>();
    private static final Map<Class<?>, Boolean> LAYER_FIELD_ABSENT = new HashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean rendererHasMedallionLayer(LivingEntityRenderer renderer) {
        if (renderer == null) return false;
        try {
            Class<?> cls = renderer.getClass();
            if (LAYER_FIELD_ABSENT.containsKey(cls)) return false;
            Field f = LAYER_FIELD_CACHE.get(cls);
            if (f == null) {
                f = findLayersField(cls);
                if (f == null) {
                    LAYER_FIELD_ABSENT.put(cls, Boolean.TRUE);
                    return false;
                }
                f.setAccessible(true);
                LAYER_FIELD_CACHE.put(cls, f);
            }
            List<RenderLayer> layers = (List<RenderLayer>) f.get(renderer);
            if (layers == null) return false;
            for (RenderLayer layer : layers) {
                if (layer instanceof MedallionRenderLayer) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private static Field findLayersField(Class<?> clazz) {
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                // "layers" in dev environment, "f_115322_" in obfuscated production
                if (f.getName().equals("layers") || f.getName().equals("f_115322_")) {
                    return f;
                }
            }
            c = c.getSuperclass();
        }
        return null;
    }
}
