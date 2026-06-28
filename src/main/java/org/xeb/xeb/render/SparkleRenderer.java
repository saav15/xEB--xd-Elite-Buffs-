package org.xeb.xeb.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.xeb.xeb.Xeb;
import org.xeb.xeb.entity.SparkleEntity;
import org.xeb.xeb.entity.SparkleModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Renderer for SparkleEntity.
 *
 * Extends GeoEntityRenderer to get full GeckoLib model rendering for free,
 * then adds:
 *
 *  1. Velocity-aligned orientation — rotates the model so the arrowhead tip
 *     always points along the projectile's current movement direction.
 *
 *  2. Energy trail — draws fading line segments between the stored past
 *     positions (SparkleEntity.trailPositions), cycling from bright white/cyan
 *     near the head to dark blue at the tail with quadratic alpha falloff.
 *     Uses RenderType.lines() for crisp sub-pixel rendering.
 */
@OnlyIn(Dist.CLIENT)
public class SparkleRenderer extends GeoEntityRenderer<SparkleEntity> {

    public SparkleRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new SparkleModel());
        this.shadowRadius = 0.0F; // projectile — no shadow
    }

    @Override
    public ResourceLocation getTextureLocation(SparkleEntity entity) {
        return new ResourceLocation(Xeb.MODID, "textures/entity/sparkle_arrow.png");
    }

    // ── Main render entry ──────────────────────────────────────────────────────

    @Override
    public void render(SparkleEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffers, int packedLight) {

        // ── Trail (rendered first, behind the model) ───────────────────────────
        renderTrail(entity, poseStack, buffers);

        // ── Align pose to velocity before GeckoLib renders the model ──────────
        poseStack.pushPose();

        Vec3 vel = entity.getDeltaMovement();
        if (vel.lengthSqr() > 1e-6) {
            float yaw   = (float) Math.toDegrees(Math.atan2(-vel.x, vel.z));
            float hLen  = (float) Math.sqrt(vel.x * vel.x + vel.z * vel.z);
            float pitch = (float) Math.toDegrees(Math.atan2(-vel.y, hLen));
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        }

        // Scale the model down — the geo cubes are defined in Bedrock pixel units
        // (~16 pu per block), so a 5-pu arrowhead needs to be ~0.31 blocks total
        float scale = 0.065F;
        poseStack.scale(scale, scale, scale);

        // Let GeckoLib handle the actual geometry rendering
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);

        poseStack.popPose();
    }

    // ── Trail rendering ────────────────────────────────────────────────────────

    /**
     * Draws a fading energy tail behind the arrowhead.
     *
     * Each pair of consecutive trail positions forms a line segment.
     * Alpha and color are interpolated from the head (bright white/cyan, alpha=1)
     * to the tail (dark blue, alpha=0) using a quadratic curve so the fade
     * looks natural rather than linear.
     *
     * The PoseStack is at the entity's render origin (entity position).
     * Trail positions are in world space, so we subtract the entity position
     * to get them in local render space.
     */
    private void renderTrail(SparkleEntity entity, PoseStack poseStack,
                              MultiBufferSource buffers) {
        Vec3[] trail = entity.getTrailSnapshot();
        if (trail.length < 2) return;

        Vec3 origin = entity.position();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());

        for (int i = trail.length - 1; i >= 1; i--) {
            // t: 1.0 = segment nearest head, 0.0 = oldest tail segment
            float t     = (float) i / trail.length;
            float alpha = t * t; // quadratic — sharp fade at tail
            if (alpha < 0.015F) continue;

            // Colour: electric blue (0.4, 0.8, 1.0) at head → red (1.0, 0.0, 0.0) at tail/end
            float r = 1.0F - t * 0.6F;
            float g = t * 0.8F;
            float b = t;

            // Convert world positions to local render space
            Vec3 p0 = trail[i - 1].subtract(origin);
            Vec3 p1 = trail[i].subtract(origin);

            // Line normal (required by RenderType.lines — use velocity direction approximation)
            float nx = 0F, ny = 1F, nz = 0F;

            lines.vertex(poseStack.last().pose(),
                            (float) p0.x, (float) p0.y, (float) p0.z)
                    .color(r, g, b, alpha)
                    .normal(poseStack.last().normal(), nx, ny, nz)
                    .endVertex();

            lines.vertex(poseStack.last().pose(),
                            (float) p1.x, (float) p1.y, (float) p1.z)
                    .color(r, g, b, alpha)
                    .normal(poseStack.last().normal(), nx, ny, nz)
                    .endVertex();
        }
    }
}
