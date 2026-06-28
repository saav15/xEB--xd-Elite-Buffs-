package org.xeb.xeb.bettercombat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.xeb.xeb.Config;
import org.xeb.xeb.weapon.WeaponStyleData;

import java.util.List;

/**
 * Handles client-side simulated inputs to trigger special abilities of Better Combat weapons,
 * modded weapon right-click specials (Cataclysm, etc.), and per-combo-step abilities.
 */
@OnlyIn(Dist.CLIENT)
public class SpecialAbilityHandler {

    // --- Movement specials state (leap/thrust/parry) ---
    private static int parryTicksRemaining = 0;

    // --- Right-click special state ---
    private static int rightClickChargeTicks = 0;
    private static int rightClickCooldownTicks = 0;
    private static boolean rightClickHeld = false;

    /**
     * Movement / stance specials (leap, thrust, parry) — now per-combo-step aware.
     * When BetterCombat exposes per-step ability data the handler activates the ability
     * that belongs to the current combo step; otherwise it falls back to the aggregate set.
     *
     * @param comboStep the 0-indexed step the player is about to swing
     */
    public static void executeSpecialAbilities(Minecraft mc, LocalPlayer player, LivingEntity target,
                                               WeaponStyleData style, int comboStep) {
        if (style == null || target == null) return;

        double dist = player.distanceTo(target);

        // Determine the active ability set for this step.
        String stepAbility = style.hasPerStepData() ? style.getAbilityForStep(comboStep) : null;

        // 1. Leap — only active on the step that declares "leap", or (legacy) if the global set has it.
        boolean leapActive = "leap".equals(stepAbility) || (!style.hasPerStepData()
                && Config.autoTriggerLeap && style.getSpecialAbilities().contains("leap"));
        if (leapActive) {
            if (dist >= 3.0D && dist <= 6.0D && player.onGround() && player.getRandom().nextFloat() < 0.2F) {
                mc.options.keyJump.setDown(true);
            } else {
                mc.options.keyJump.setDown(false);
            }
        } else {
            mc.options.keyJump.setDown(false);
        }

        // 2. Thrust — sprint before attack on the correct step.
        boolean thrustActive = "thrust".equals(stepAbility) || (!style.hasPerStepData()
                && Config.autoTriggerThrust && style.getSpecialAbilities().contains("thrust"));
        if (thrustActive && dist >= 2.0D && dist <= 5.0D) {
            player.setSprinting(true);
        }

        // 3. Parry stance — briefly hold right-click when target is swinging.
        boolean parryActive = "parry".equals(stepAbility) || (!style.hasPerStepData()
                && style.getSpecialAbilities().contains("parry"));
        if (parryActive) {
            boolean targetAttacking = target.swinging && isFacing(target, player) && dist <= 4.0D;
            if (targetAttacking && parryTicksRemaining <= 0) {
                parryTicksRemaining = 6; // Hold parry for 6 ticks (~0.3 s)
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

    /**
     * Right-click special-ability execution for modded weapons (Cataclysm charge-swords,
     * elemental staves, etc.). Two modes:
     * <ul>
     *   <li><b>PERIODIC</b> — charge-and-release on a fixed cooldown timer.</li>
     *   <li><b>TACTICAL</b> — only activate when there is a tactical advantage
     *       (target low HP, multi-target, player hurt, etc.).</li>
     *   <li><b>DISABLED</b> — do nothing.</li>
     * </ul>
     *
     * @param mode one of "PERIODIC", "TACTICAL", "DISABLED" (from config)
     */
    public static void executeRightClickAbilities(Minecraft mc, LocalPlayer player, LivingEntity target, String mode) {
        if ("DISABLED".equals(mode)) return;
        if (mc == null || player == null || target == null || !player.isAlive() || !target.isAlive()) return;

        if (rightClickCooldownTicks > 0) {
            rightClickCooldownTicks--;
            return;
        }

        boolean shouldActivate = false;

        if ("PERIODIC".equals(mode)) {
            // Activate on a fixed cooldown cycle.
            shouldActivate = rightClickChargeTicks == 0 && rightClickCooldownTicks <= 0;
        } else if ("TACTICAL".equals(mode)) {
            // Only activate when there is a clear advantage.
            shouldActivate = evaluateTacticalAdvantage(player, target);
        }

        if (!shouldActivate) return;

        // Begin the charge phase (hold right-click).
        if (rightClickChargeTicks == 0) {
            rightClickChargeTicks = 1; // Start charging
            rightClickHeld = true;
            mc.options.keyUse.setDown(true);
            return;
        }

        // Charging in progress — compute draw duration from the held item.
        if (rightClickChargeTicks > 0 && rightClickHeld) {
            net.minecraft.world.item.ItemStack held = player.getMainHandItem();
            int rawUse = held.getItem().getUseDuration(held);
            int drawDuration = (rawUse > 0 && rawUse < 200) ? Math.max(10, rawUse) : 20;

            if (rightClickChargeTicks >= drawDuration) {
                // Release!
                mc.options.keyUse.setDown(false);
                if (player.isUsingItem() && mc.gameMode != null) {
                    mc.gameMode.releaseUsingItem(player);
                }
                rightClickChargeTicks = 0;
                rightClickHeld = false;

                // Cooldown based on mode.
                if ("PERIODIC".equals(mode)) {
                    rightClickCooldownTicks = Config.rightClickPeriodicCooldownTicks;
                } else {
                    // TACTICAL: longer cooldown to avoid spamming specials
                    rightClickCooldownTicks = Config.rightClickPeriodicCooldownTicks + 40;
                }
            } else {
                rightClickChargeTicks++;
            }
        }
    }

    /**
     * Evaluates whether using the weapon's right-click special is tactically advantageous.
     * Returns true when ANY of the following conditions are met:
     * <ul>
     *   <li>Target HP is below the configured threshold (low-hp finisher).</li>
     *   <li>Multiple enemies nearby (AoE special is worth it).</li>
     *   <li>Player HP is below 50% (desperate measure).</li>
     *   <li>Target is currently attacking the player (counter window).</li>
     * </ul>
     */
    private static boolean evaluateTacticalAdvantage(LocalPlayer player, LivingEntity target) {
        // Target is low on health — finish them off.
        float targetHealthPct = target.getHealth() / target.getMaxHealth();
        if (targetHealthPct <= Config.rightClickTacticalLowHpThreshold) {
            return true;
        }

        // Player is hurt — desperate times.
        float playerHealthPct = player.getHealth() / player.getMaxHealth();
        if (playerHealthPct < 0.5F) {
            return true;
        }

        // Target is actively swinging at us — counter window.
        if (target.swinging && isFacing(target, player) && player.distanceTo(target) <= 5.0D) {
            return true;
        }

        // Multiple enemies nearby — AoE special is worth using.
        double radius = Config.tacticalMultiTargetRadius;
        AABB searchBox = player.getBoundingBox().inflate(radius);
        List<LivingEntity> nearby = player.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                e -> e != player && e.isAlive() && e != target);
        if (nearby.size() >= 2) {
            return true;
        }

        return false;
    }

    /**
     * Resets all handler state. Called from {@code PlayerBotStateMachine.reset()} when
     * Madness wears off.
     */
    public static void reset() {
        parryTicksRemaining = 0;
        rightClickChargeTicks = 0;
        rightClickCooldownTicks = 0;
        rightClickHeld = false;
    }

    private static boolean isFacing(LivingEntity attacker, LivingEntity target) {
        Vec3 attackerLook = attacker.getViewVector(1.0F).normalize();
        Vec3 toTarget = target.position().subtract(attacker.position()).normalize();
        double dot = attackerLook.dot(toTarget);
        return dot > 0.7D;
    }
}
