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

public class GlowEyeOverlay<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation GLOW_EYES_TEX = new ResourceLocation("textures/entity/enderman/enderman_eyes.png"); // base vanilla glowing eyes texture as fallback

    public GlowEyeOverlay(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        List<MedallionData> medallions = MedallionManager.getMedallions(entity);
        if (medallions.isEmpty()) return;

        // Pulse alpha: 0.7 to 1.0 over 2 seconds (40 ticks)
        float time = entity.tickCount + partialTick;
        float alpha = 0.7F + (float) Math.sin(time * 0.157F) * 0.15F; // sine wave pulsing

        // Get primary buff color
        int color = medallions.get(0).getBuff().getColor();
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        // Render Enderman-like glowing eyes overlay using RenderType.eyes
        // RenderType.eyes makes the texture full bright in the dark!
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.eyes(GLOW_EYES_TEX));
        
        // This renders the eyes in full brightness
        this.getParentModel().renderToBuffer(poseStack, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
    }
}
