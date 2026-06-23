package org.xeb.xeb.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.xeb.xeb.medallion.MedallionData;
import org.xeb.xeb.medallion.MedallionManager;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class MobColorOverlay<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public MobColorOverlay(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        List<MedallionData> medallions = MedallionManager.getMedallions(entity);
        if (medallions.isEmpty()) return;

        if (!org.xeb.xeb.Config.colorOverlayEnabled) return;

        // Dynamic pulsing and color cycling
        float time = (entity.tickCount + partialTick) / 20.0F;
        int colorCount = medallions.size();
        float red, green, blue;
        
        if (colorCount == 1) {
            int color = medallions.get(0).getBuff().getColor();
            red = ((color >> 16) & 0xFF) / 255.0F;
            green = ((color >> 8) & 0xFF) / 255.0F;
            blue = (color & 0xFF) / 255.0F;
        } else {
            // Cycle colors continuously over time to prevent muddy mixes and create a premium effect
            float indexFloat = (time * 0.7F) % colorCount; // Transition speed
            int index1 = (int) Math.floor(indexFloat);
            int index2 = (index1 + 1) % colorCount;
            float progress = indexFloat - index1;

            int color1 = medallions.get(index1).getBuff().getColor();
            int color2 = medallions.get(index2).getBuff().getColor();

            float r1 = ((color1 >> 16) & 0xFF) / 255.0F;
            float g1 = ((color1 >> 8) & 0xFF) / 255.0F;
            float b1 = (color1 & 0xFF) / 255.0F;

            float r2 = ((color2 >> 16) & 0xFF) / 255.0F;
            float g2 = ((color2 >> 8) & 0xFF) / 255.0F;
            float b2 = (color2 & 0xFF) / 255.0F;

            red = r1 + (r2 - r1) * progress;
            green = g1 + (g2 - g1) * progress;
            blue = b1 + (b2 - b1) * progress;
        }

        // Opacity pulsing: color overlay intensity increased by 20%
        float alpha = 0.86F + (float) Math.sin((entity.tickCount + partialTick) / 3.5F) * 0.12F;

        ResourceLocation texture = this.getTextureLocation(entity);
        
        poseStack.pushPose();
        
        // Scale slightly outward from base to prevent z-fighting without translating
        poseStack.scale(1.002F, 1.002F, 1.002F);
        
        // Render base model again as a translucent overlay
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
        
        poseStack.popPose();
    }
}
