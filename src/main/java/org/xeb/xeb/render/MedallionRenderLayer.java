package org.xeb.xeb.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.xeb.xeb.Xeb;
import org.xeb.xeb.medallion.MedallionData;
import org.xeb.xeb.medallion.MedallionManager;
import org.xeb.xeb.medallion.MedallionType;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class MedallionRenderLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation BRONZE_TEXTURE = new ResourceLocation(Xeb.MODID, "textures/entity/medallion_bronze.png");
    private static final ResourceLocation SILVER_TEXTURE = new ResourceLocation(Xeb.MODID, "textures/entity/medallion_silver.png");
    private static final ResourceLocation GOLD_TEXTURE = new ResourceLocation(Xeb.MODID, "textures/entity/medallion_gold.png");

    private final MedallionModel model = new MedallionModel();

    public MedallionRenderLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    public static float getMedallionWorldHeight(LivingEntity entity) {
        float height = entity.getBbHeight();
        String typeId = net.minecraft.world.entity.EntityType.getKey(entity.getType()).toString();
        
        switch (typeId) {
            case "minecraft:ghast":
                return 4.2F;
            case "minecraft:wither":
                return 2.4F;
            case "minecraft:ender_dragon":
                return 8.0F;
            case "minecraft:giant":
                return 12.2F;
            case "xeb:elite_fly":
                return height + 0.03F;
            case "minecraft:spider":
                return 0.75F;
            case "minecraft:cave_spider":
                return 0.45F;
        }
        
        return height + 0.12F;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        List<MedallionData> medallions = MedallionManager.getMedallions(entity);
        if (medallions.isEmpty()) return;

        int count = medallions.size();
        float time = (entity.tickCount + partialTick);
        double bob = Math.sin(time / 10.0D) * 0.08D;
        float rotation = (time * 4.5F) % 360.0F;

        // Calculate the current scale of the PoseStack to undo any parent scaling (like for baby zombies or giants)
        org.joml.Vector3f dir = new org.joml.Vector3f(0.0F, 1.0F, 0.0F);
        poseStack.last().pose().transformDirection(dir);
        float scale = dir.length();
        if (scale < 0.001F) scale = 1.0F;

        // Base medallion scale in world coordinates
        float entityWidth = entity.getBbWidth();
        float targetWorldScale = 0.2F + 0.8F * entityWidth;
        targetWorldScale = Math.max(0.25F, Math.min(2.5F, targetWorldScale));
        
        // Convert to PoseStack local scale
        float baseScale = targetWorldScale / scale;
        
        // Spacing between multiple medallions in PoseStack coordinates
        float targetWorldSpacing = 0.55F * targetWorldScale;
        float spacing = targetWorldSpacing / scale;

        float worldHeight = getMedallionWorldHeight(entity);
        float heightOffset = worldHeight / scale;

        for (int i = 0; i < count; i++) {
            MedallionData m = medallions.get(i);
            poseStack.pushPose();

            // Position above the head (bounding box height + offset) in PoseStack coordinates
            poseStack.translate(0.0F, -heightOffset, 0.0F);

            // Translate horizontally to form a row
            float xOffset = (i - (count - 1) / 2.0F) * spacing;
            poseStack.translate(xOffset, bob, 0.0D);

            // Rotate
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

            // Scale
            poseStack.scale(baseScale, baseScale, baseScale);

            // Medallion tier base color (Bronze, Silver, Gold)
            int tierColor = m.getTier().getColor();
            float tR = ((tierColor >> 16) & 0xFF) / 255.0F;
            float tG = ((tierColor >> 8) & 0xFF) / 255.0F;
            float tB = (tierColor & 0xFF) / 255.0F;

            // Buff color (representing debuff)
            int buffColor = m.getBuff().getColor();
            float bR = ((buffColor >> 16) & 0xFF) / 255.0F;
            float bG = ((buffColor >> 8) & 0xFF) / 255.0F;
            float bB = (buffColor & 0xFF) / 255.0F;

            ResourceLocation texture = switch (m.getTier()) {
                case COMMON -> BRONZE_TEXTURE;
                case RARE -> SILVER_TEXTURE;
                case LEGENDARY -> GOLD_TEXTURE;
            };

            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
            
            // Render main coin shape in base medallion color
            model.renderCoin(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, tR, tG, tB, 1.0F);

            // Render center gem in buff color
            model.renderGem(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, bR, bG, bB, 1.0F);

            poseStack.popPose();
        }
    }

    public static class MedallionModel extends Model {
        private final ModelPart coin;
        private final ModelPart gem;

        public MedallionModel() {
            super(RenderType::entityCutoutNoCull);

            MeshDefinition mesh = new MeshDefinition();
            PartDefinition rootPart = mesh.getRoot();

            // A thin 8x8x1 coin box
            rootPart.addOrReplaceChild("coin", CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(-4.0F, -4.0F, -0.5F, 8.0F, 8.0F, 1.0F),
                    PartPose.ZERO);

            // A central 4x4x1.2 gem box representing the specific buff
            rootPart.addOrReplaceChild("gem", CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(-2.0F, -2.0F, -0.6F, 4.0F, 4.0F, 1.2F),
                    PartPose.ZERO);

            ModelPart root = LayerDefinition.create(mesh, 32, 32).bakeRoot();
            this.coin = root.getChild("coin");
            this.gem = root.getChild("gem");
        }

        public void renderCoin(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
            this.coin.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }

        public void renderGem(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
            this.gem.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
            this.coin.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
            this.gem.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}
