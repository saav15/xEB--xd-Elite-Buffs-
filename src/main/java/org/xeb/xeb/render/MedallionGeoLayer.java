package org.xeb.xeb.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class MedallionGeoLayer<T extends LivingEntity & GeoAnimatable> extends GeoRenderLayer<T> {
    private final MedallionRenderLayer<T, ?> standardLayer;

    public MedallionGeoLayer(GeoRenderer<T> renderer) {
        super(renderer);
        this.standardLayer = new MedallionRenderLayer<>(null);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        standardLayer.render(poseStack, bufferSource, packedLight, animatable, 0.0F, 0.0F, partialTick, animatable.tickCount + partialTick, 0.0F, 0.0F);
    }
}
