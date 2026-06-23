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

public class MedallionGeoLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {
    private final MedallionRenderLayer<LivingEntity, ?> standardLayer;

    public MedallionGeoLayer(GeoRenderer<T> renderer) {
        super(renderer);
        this.standardLayer = new MedallionRenderLayer<>(null);
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

        standardLayer.render(poseStack, bufferSource, packedLight, entity, 0.0F, 0.0F, partialTick, entity.tickCount + partialTick, 0.0F, 0.0F);
    }
}
