package org.xeb.xeb.mixin;

import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intentionally empty mixin for CreeperRenderer.
 * Layers (MedallionRenderLayer, MobColorOverlay, GlowEyeOverlay) are added
 * via the EntityRenderersEvent.AddLayers loop in Xeb.java which covers all
 * LivingEntityRenderer types including CreeperRenderer without duplication.
 */
@Mixin(CreeperRenderer.class)
public class CreeperRendererMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(EntityRendererProvider.Context context, CallbackInfo ci) {
        // No-op: layers are registered via EntityRenderersEvent.AddLayers in Xeb.java
    }
}
