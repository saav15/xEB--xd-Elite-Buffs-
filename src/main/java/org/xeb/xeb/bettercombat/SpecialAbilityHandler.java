package org.xeb.xeb.bettercombat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.xeb.xeb.Config;
import org.xeb.xeb.weapon.WeaponStyleData;

/**
 * Handles client-side simulated inputs to trigger special abilities of Better Combat weapons.
 */
@OnlyIn(Dist.CLIENT)
public class SpecialAbilityHandler {

    private static int parryTicksRemaining = 0;

    /**
     * Updates and triggers simulated movement or action states for the client bot.
     */
    public static void executeSpecialAbilities(Minecraft mc, LocalPlayer player, LivingEntity target, WeaponStyleData style) {
        if (style == null || target == null) return;

        double dist = player.distanceTo(target);

        // 1. Leap ability triggering
        if (Config.autoTriggerLeap && style.getSpecialAbilities().contains("leap")) {
            if (dist >= 3.0D && dist <= 6.0D && player.onGround() && player.getRandom().nextFloat() < 0.2F) {
                mc.options.keyJump.setDown(true);
            } else {
                mc.options.keyJump.setDown(false);
            }
        }

        // 2. Thrust ability triggering (Sprint before attack)
        if (Config.autoTriggerThrust && style.getSpecialAbilities().contains("thrust")) {
            if (dist >= 2.0D && dist <= 5.0D) {
                player.setSprinting(true);
            }
        }

        // 3. Parry stance triggering (Briefly hold right click when target swings)
        if (style.getSpecialAbilities().contains("parry")) {
            boolean targetAttacking = target.swinging && isFacing(target, player) && dist <= 4.0D;
            if (targetAttacking && parryTicksRemaining <= 0) {
                parryTicksRemaining = 6; // Hold parry stance for 6 ticks (0.3s)
            }
        }

        if (parryTicksRemaining > 0) {
            mc.options.keyUse.setDown(true);
            parryTicksRemaining--;
        } else if (parryTicksRemaining == 0) {
            mc.options.keyUse.setDown(false);
            parryTicksRemaining = -1; // Reset complete
        }
    }

    private static boolean isFacing(LivingEntity attacker, LivingEntity target) {
        Vec3 attackerLook = attacker.getViewVector(1.0F).normalize();
        Vec3 toTarget = target.position().subtract(attacker.position()).normalize();
        double dot = attackerLook.dot(toTarget);
        return dot > 0.7D; // Attacker is looking general direction of target
    }
}
