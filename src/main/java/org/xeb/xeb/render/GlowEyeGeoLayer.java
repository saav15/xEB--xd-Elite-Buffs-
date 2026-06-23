package org.xeb.xeb.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.xeb.xeb.Xeb;
import org.xeb.xeb.medallion.MedallionData;
import org.xeb.xeb.medallion.MedallionManager;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.util.RenderUtils;

import java.util.List;
import java.util.Optional;

public class GlowEyeGeoLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    private static final ResourceLocation WHITE_TEX = new ResourceLocation(Xeb.MODID, "textures/entity/white.png");
    private final GlowEyeOverlay.GlowEyeModel eyesModel = new GlowEyeOverlay.GlowEyeModel();

    public GlowEyeGeoLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        LivingEntity entity = null;
        if (animatable instanceof LivingEntity living) {
            entity = living;
        } else if (this.getRenderer() instanceof software.bernie.geckolib.renderer.GeoReplacedEntityRenderer<?, ?> replacedRenderer) {
            entity = org.xeb.xeb.Xeb.getEntityFromReplacedRenderer(replacedRenderer);
        }
        if (entity == null) return;

        List<MedallionData> medallions = MedallionManager.getMedallions(entity);
        if (medallions.isEmpty()) return;

        if (!org.xeb.xeb.Config.glowEyesEnabled) return;

        float time = entity.tickCount + partialTick;
        float alpha = 0.7F + (float) Math.sin(time * 0.157F) * 0.15F;

        int color = medallions.get(0).getBuff().getColor();
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.eyes(WHITE_TEX));

        // Detect current PoseStack scale
        org.joml.Vector3f dir = new org.joml.Vector3f(0.0F, 1.0F, 0.0F);
        poseStack.last().pose().transformDirection(dir);
        float scale = dir.length();
        if (scale < 0.001F) scale = 1.0F;

        GeoBone headBone = bakedModel.getBone("head").orElseGet(() -> bakedModel.getBone("Head").orElse(null));

        if (headBone != null) {
            poseStack.pushPose();
            // Position PoseStack at the bone using GeckoLib's helper
            RenderUtils.prepMatrixForBone(poseStack, headBone);
            // Apply scale factor to match the pixel coordinates (scaleFactor is 1.0 if already scaled by 1/16)
            float scaleFactor = 1.0F / (scale * 16.0F);
            poseStack.scale(scaleFactor, scaleFactor, scaleFactor);
            // Render the eye model
            eyesModel.renderToBuffer(poseStack, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
            poseStack.popPose();
        } else {
            // Fallback rendering
            poseStack.pushPose();
            float width = entity.getBbWidth();
            float height = entity.getBbHeight();
            
            float localY = (-height * 0.8F) / scale;
            float localZ = (-width * 0.51F) / scale;
            poseStack.translate(0.0F, localY, localZ);
            
            float scaleFactor = 1.0F / (scale * 16.0F);
            poseStack.scale(scaleFactor, scaleFactor, scaleFactor);
            poseStack.translate(0.0F, 4.2F, 0.0F);
            
            eyesModel.renderToBuffer(poseStack, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
            poseStack.popPose();
        }
    }
}
