package org.xeb.xeb.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.xeb.xeb.medallion.MedallionData;
import org.xeb.xeb.medallion.MedallionManager;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.cache.object.BakedGeoModel;

import java.util.List;

public class GlowEyeGeoLayer<T extends LivingEntity & GeoAnimatable> extends GeoRenderLayer<T> {
    private static final ResourceLocation GLOW_EYES_TEX = new ResourceLocation("textures/entity/enderman/enderman_eyes.png");

    public GlowEyeGeoLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        List<MedallionData> medallions = MedallionManager.getMedallions(animatable);
        if (medallions.isEmpty()) return;

        float time = animatable.tickCount + partialTick;
        float alpha = 0.7F + (float) Math.sin(time * 0.157F) * 0.15F;

        int color = medallions.get(0).getBuff().getColor();
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        RenderType eyesType = RenderType.eyes(GLOW_EYES_TEX);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(eyesType);

        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, eyesType, vertexConsumer, partialTick, 15728880, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
    }
}
