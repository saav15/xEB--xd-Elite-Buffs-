package org.xeb.xeb.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.xeb.xeb.entity.EliteFlyEntity;
import org.xeb.xeb.entity.EliteFlyModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EliteFlyRenderer extends GeoEntityRenderer<EliteFlyEntity> {
    public EliteFlyRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new EliteFlyModel());
    }

    @Override
    public void render(EliteFlyEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        // Translate model down by 0.5 blocks to align visual presentation with physical hitbox
        poseStack.translate(0.0D, -0.5D, 0.0D);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
