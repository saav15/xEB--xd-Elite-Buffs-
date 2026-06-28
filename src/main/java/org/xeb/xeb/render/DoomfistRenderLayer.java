package org.xeb.xeb.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xeb.xeb.Xeb;

@Mod.EventBusSubscriber(modid = Xeb.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DoomfistRenderLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Xeb.MODID, "textures/entity/doomfist_gauntlet.png");
    private final ModelPart gauntletPart;

    public DoomfistRenderLayer(RenderLayerParent<T, M> parent) {
        super(parent);
        
        // Build 3D gauntlet part (bulkier sleeve and massive fist for third-person)
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("main", CubeListBuilder.create()
                // Bulkier forearm sleeve: width/depth 5.2, height 9
                .addBox(-2.6F, 3.0F, -2.6F, 5.2F, 9.0F, 5.2F)
                // Massive fist head: width/depth 6.8, height 5.5, extends past hand
                .addBox(-3.4F, 11.0F, -3.4F, 6.8F, 5.5F, 6.8F),
                PartPose.ZERO);
        
        this.gauntletPart = LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack mainHand = entity.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = entity.getItemInHand(InteractionHand.OFF_HAND);

        boolean hasMain = mainHand.is(org.xeb.xeb.item.ModItems.DOOMFIST.get());
        boolean hasOff = offHand.is(org.xeb.xeb.item.ModItems.DOOMFIST.get());

        if (!hasMain && !hasOff) return;

        M model = this.getParentModel();
        if (model instanceof ArmedModel armedModel) {
            if (hasMain) {
                HumanoidArm arm = entity.getMainArm();
                renderGauntletForArm(poseStack, buffer, packedLight, armedModel, arm, entity);
            }
            if (hasOff) {
                HumanoidArm arm = entity.getMainArm().getOpposite();
                renderGauntletForArm(poseStack, buffer, packedLight, armedModel, arm, entity);
            }
        }
    }

    private void renderGauntletForArm(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ArmedModel armedModel, HumanoidArm arm, T entity) {
        poseStack.pushPose();
        
        // Translate and rotate based on the arm's pose
        armedModel.translateToHand(arm, poseStack);
        
        // 1. Render gauntlet
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.gauntletPart.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        
        // 2. Render blue energy aura if charging or dashing
        float chargeRatio = 0.0F;
        boolean isDashing = entity.getPersistentData().getBoolean("xebDoomfistDashing");
        if (entity.isUsingItem() && entity.getUseItem().is(org.xeb.xeb.item.ModItems.DOOMFIST.get())) {
            int ticksCharged = entity.getTicksUsingItem();
            chargeRatio = Math.min(50.0F, ticksCharged) / 50.0F;
        } else if (isDashing) {
            chargeRatio = entity.getPersistentData().getFloat("xebDoomfistChargeRatio");
        }

        if (chargeRatio > 0.0F || isDashing) {
            poseStack.pushPose();
            // Scale slightly up around its center
            poseStack.scale(1.06F, 1.06F, 1.06F);
            
            // Calculate pulse or energy scrolling
            float time = (entity.tickCount + Minecraft.getInstance().getFrameTime()) * 0.05F;
            float alpha = isDashing ? 0.75F : (chargeRatio * 0.45F + 0.1F * (float)Math.sin(time * 10.0F));
            alpha = Math.max(0.1F, Math.min(0.95F, alpha));
            
            float r = 0.0F;
            float g = 0.4F + chargeRatio * 0.4F;
            float b = 1.0F;
            
            ResourceLocation energyTexture = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
            float u = time * 0.5F;
            float v = time * 0.5F;
            
            VertexConsumer auraConsumer = buffer.getBuffer(RenderType.energySwirl(energyTexture, u, v));
            // Render at full-bright coordinates (15728880) so it glows emissively in the dark!
            this.gauntletPart.render(poseStack, auraConsumer, 15728880, OverlayTexture.NO_OVERLAY, r, g, b, alpha);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    // --- First Person Rendering in RenderHandEvent ---
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (event.getItemStack().is(org.xeb.xeb.item.ModItems.DOOMFIST.get())) {
            // Cancel item rendering so we can render our custom 3D model instead of the flat 2D texture
            event.setCanceled(true);

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();

            // Position model in first-person based on hand (right/left)
            boolean isRightHand = event.getHand() == InteractionHand.MAIN_HAND == (Minecraft.getInstance().options.mainHand().get() == HumanoidArm.RIGHT);
            
            // Translate and position to match first-person viewport (moved to the side and down)
            if (isRightHand) {
                poseStack.translate(0.55D, -0.65D, -0.7D);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-45.0F));
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(15.0F));
            } else {
                poseStack.translate(-0.55D, -0.65D, -0.7D);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(45.0F));
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(15.0F));
            }

            // Scale down first-person model by 35% so it doesn't obstruct view
            poseStack.scale(0.65F, 0.65F, 0.65F);

            // Build/render the first person gauntlet
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("main", CubeListBuilder.create()
                    // Forearm sleeve
                    .addBox(-2.5F, -6.0F, -2.5F, 5.0F, 10.0F, 5.0F)
                    // Golden fist head at the front
                    .addBox(-3.0F, 3.5F, -3.0F, 6.0F, 5.0F, 6.0F),
                    PartPose.ZERO);
            ModelPart firstPersonPart = LayerDefinition.create(mesh, 64, 64).bakeRoot();

            VertexConsumer consumer = event.getMultiBufferSource().getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
            firstPersonPart.render(poseStack, consumer, event.getPackedLight(), OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

            // Aura logic in first person
            net.minecraft.world.entity.player.Player player = Minecraft.getInstance().player;
            if (player != null) {
                float chargeRatio = 0.0F;
                boolean isDashing = player.getPersistentData().getBoolean("xebDoomfistDashing");
                if (player.isUsingItem() && player.getUseItem().is(org.xeb.xeb.item.ModItems.DOOMFIST.get())) {
                    int ticksCharged = player.getTicksUsingItem();
                    chargeRatio = Math.min(50.0F, ticksCharged) / 50.0F;
                } else if (isDashing) {
                    chargeRatio = player.getPersistentData().getFloat("xebDoomfistChargeRatio");
                }

                if (chargeRatio > 0.0F || isDashing) {
                    poseStack.pushPose();
                    poseStack.scale(1.06F, 1.06F, 1.06F);

                    float time = (player.tickCount + event.getPartialTick()) * 0.05F;
                    float alpha = isDashing ? 0.75F : (chargeRatio * 0.45F + 0.1F * (float)Math.sin(time * 10.0F));
                    alpha = Math.max(0.1F, Math.min(0.95F, alpha));

                    float r = 0.0F;
                    float g = 0.4F + chargeRatio * 0.4F;
                    float b = 1.0F;

                    ResourceLocation energyTexture = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
                    float u = time * 0.5F;
                    float v = time * 0.5F;

                    VertexConsumer auraConsumer = event.getMultiBufferSource().getBuffer(RenderType.energySwirl(energyTexture, u, v));
                    firstPersonPart.render(poseStack, auraConsumer, 15728880, OverlayTexture.NO_OVERLAY, r, g, b, alpha);
                    poseStack.popPose();
                }
            }

            poseStack.popPose();
        }
    }
}
