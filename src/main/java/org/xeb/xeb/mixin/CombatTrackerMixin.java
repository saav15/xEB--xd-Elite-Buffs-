package org.xeb.xeb.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.xeb.xeb.event.DeathMessageHelper;

@Mixin(CombatTracker.class)
public class CombatTrackerMixin {
    @Shadow @Final private LivingEntity mob;

    @Inject(method = "getDeathMessage", at = @At("RETURN"), cancellable = true)
    private void onGetDeathMessage(CallbackInfoReturnable<Component> cir) {
        Component original = cir.getReturnValue();
        Component modified = DeathMessageHelper.modifyDeathMessage(original, this.mob);
        cir.setReturnValue(modified);
    }
}
