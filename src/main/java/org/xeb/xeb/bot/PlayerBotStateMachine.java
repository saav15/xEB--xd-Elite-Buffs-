package org.xeb.xeb.bot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.xeb.xeb.Config;
import org.xeb.xeb.bettercombat.BetterCombatWeaponAnalyzer;
import org.xeb.xeb.bettercombat.SpecialAbilityHandler;
import org.xeb.xeb.boss.UniversalBossDetector;
import org.xeb.xeb.effect.ModEffects;
import org.xeb.xeb.weapon.*;

import java.util.List;

/**
 * formalizes player bot controls under Madness using state transitions.
 */
@OnlyIn(Dist.CLIENT)
public class PlayerBotStateMachine {
    private static BotState currentState = BotState.IDLE;
    private static int stateTicks = 0;
    private static final HotbarScanner HOTBAR_SCANNER = new HotbarScanner();
    private static LivingEntity currentTarget = null;
    private static int hybridChargeTicks = 0;
    private static int hybridSpecialCooldown = 0;
    private static int rangedDrawTicks = 0;
    
    private static int currentComboStep = 0;
    private static int comboTicksRemaining = 0;

    public static BotState getCurrentState() {
        return currentState;
    }

    public static LivingEntity getCurrentTarget() {
        return currentTarget;
    }

    /**
     * Resets the entire state machine state and releases all key bindings.
     */
    public static void reset() {
        currentState = BotState.IDLE;
        stateTicks = 0;
        currentTarget = null;
        hybridChargeTicks = 0;
        hybridSpecialCooldown = 0;
        rangedDrawTicks = 0;
        currentComboStep = 0;
        comboTicksRemaining = 0;
        HOTBAR_SCANNER.reset();

        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.options != null) {
                mc.options.keyUse.setDown(false);
                mc.options.keyAttack.setDown(false);
                mc.options.keyJump.setDown(false);
            }
            if (mc != null && mc.player != null && mc.player.isUsingItem() && mc.gameMode != null) {
                mc.gameMode.releaseUsingItem(mc.player);
            }
        } catch (Exception | LinkageError ignored) {
            // Safe in unit test environments
        }
    }

    /**
     * Ticks the bot state machine, executing actions and performing transitions.
     */
    public static void tick(Minecraft mc, LocalPlayer player) {
        if (player == null || !player.isAlive() || !player.hasEffect(ModEffects.MADNESS.get())) {
            if (currentState != BotState.IDLE) {
                reset();
            }
            return;
        }

        stateTicks++;
        if (hybridSpecialCooldown > 0) hybridSpecialCooldown--;
        if (comboTicksRemaining > 0) comboTicksRemaining--;

        // State Machine transitions
        switch (currentState) {
            case IDLE:
                currentState = BotState.SCANNING;
                stateTicks = 0;
                break;

            case SCANNING:
                findTarget(player);
                if (currentTarget != null) {
                    currentState = BotState.APPROACHING;
                    stateTicks = 0;
                }
                break;

            case APPROACHING:
                validateTarget(player);
                if (currentTarget == null) {
                    currentState = BotState.SCANNING;
                    stateTicks = 0;
                    break;
                }

                double dist = player.distanceTo(currentTarget);
                double reach = getWeaponReach(player);
                boolean isRanged = WeaponClassificationEngine.classify(player.getMainHandItem()) == WeaponClass.RANGED;

                // Scan hotbar slots and adjust selected slot
                boolean inMeleeRange = dist <= reach;
                int bestSlot = HOTBAR_SCANNER.getBestWeaponSlot(player, dist, inMeleeRange);
                if (bestSlot != player.getInventory().selected) {
                    switchToSlot(mc, player, bestSlot);
                }

                if (inMeleeRange) {
                    currentState = BotState.ATTACKING_MELEE;
                    stateTicks = 0;
                } else if (isRanged && dist <= 24.0D) {
                    currentState = BotState.ATTACKING_RANGED;
                    stateTicks = 0;
                }
                break;

            case ATTACKING_MELEE:
                validateTarget(player);
                if (currentTarget == null) {
                    currentState = BotState.SCANNING;
                    stateTicks = 0;
                    break;
                }

                double distMelee = player.distanceTo(currentTarget);
                double reachMelee = getWeaponReach(player);

                int bestSlotMelee = HOTBAR_SCANNER.getBestWeaponSlot(player, distMelee, distMelee <= reachMelee);
                if (bestSlotMelee != player.getInventory().selected) {
                    switchToSlot(mc, player, bestSlotMelee);
                }

                if (distMelee > reachMelee) {
                    boolean isRangedMelee = WeaponClassificationEngine.classify(player.getMainHandItem()) == WeaponClass.RANGED;
                    if (isRangedMelee) {
                        currentState = BotState.ATTACKING_RANGED;
                    } else {
                        currentState = BotState.APPROACHING;
                    }
                    stateTicks = 0;
                    break;
                }

                executeMeleeCombat(mc, player);
                break;

            case ATTACKING_RANGED:
                validateTarget(player);
                if (currentTarget == null) {
                    currentState = BotState.SCANNING;
                    stateTicks = 0;
                    break;
                }

                double distRanged = player.distanceTo(currentTarget);
                double reachRanged = getWeaponReach(player);

                int bestSlotRanged = HOTBAR_SCANNER.getBestWeaponSlot(player, distRanged, distRanged <= reachRanged);
                if (bestSlotRanged != player.getInventory().selected) {
                    switchToSlot(mc, player, bestSlotRanged);
                }

                if (distRanged <= reachRanged) {
                    currentState = BotState.ATTACKING_MELEE;
                    stateTicks = 0;
                    break;
                } else if (WeaponClassificationEngine.classify(player.getMainHandItem()) != WeaponClass.RANGED) {
                    currentState = BotState.APPROACHING;
                    stateTicks = 0;
                    break;
                }

                executeRangedCombat(mc, player);
                break;
        }

        // Apply chaotic view orientation
        if (currentTarget != null) {
            lookAtTarget(player, currentTarget);
        }
    }

    private static void findTarget(LocalPlayer player) {
        double range = 16.0D;
        AABB searchBox = player.getBoundingBox().inflate(range);
        List<LivingEntity> entities = player.level().getEntitiesOfClass(
                LivingEntity.class, searchBox,
                t -> t != player && t.isAlive() && player.hasLineOfSight(t) &&
                     !UniversalBossDetector.isBlacklisted(t) &&
                     !(t instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator()))
        );

        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (LivingEntity e : entities) {
            double dist = player.distanceToSqr(e);
            if (dist < closestDist) {
                closestDist = dist;
                closest = e;
            }
        }
        currentTarget = closest;
    }

    private static void validateTarget(LocalPlayer player) {
        if (currentTarget == null || !currentTarget.isAlive() || player.distanceToSqr(currentTarget) > 360.0D) {
            currentTarget = null;
        }
    }

    private static void switchToSlot(Minecraft mc, LocalPlayer player, int slot) {
        ItemStack prevStack = player.getMainHandItem();
        ItemStack newStack = player.getInventory().getItem(slot);

        if (isBCLoaded() && comboTicksRemaining > 0) {
            double prevScore = WeaponScoringEngine.calculateScore(prevStack, player.distanceTo(currentTarget) <= getWeaponReach(player), player);
            double newScore = WeaponScoringEngine.calculateScore(newStack, player.distanceTo(currentTarget) <= getWeaponReach(player), player);
            if (newScore - prevScore < Config.emergencySwitchScoreThreshold) {
                return; // Wait for current combo to end
            }
        }

        player.getInventory().selected = slot;
        rangedDrawTicks = 0;
        hybridChargeTicks = 0;
        currentComboStep = 0;
        comboTicksRemaining = 0;
        mc.options.keyUse.setDown(false);
        if (player.isUsingItem() && mc.gameMode != null) {
            mc.gameMode.releaseUsingItem(player);
        }

        // Two-handed offhand swap logic
        if (isBCLoaded()) {
            var styleOpt = BetterCombatWeaponAnalyzer.getWeaponStyle(newStack);
            if (styleOpt.isPresent() && styleOpt.get().isTwoHanded()) {
                ItemStack offhand = player.getOffhandItem();
                if (WeaponClassificationEngine.classify(offhand) != WeaponClass.NON_WEAPON) {
                    for (int i = 0; i < 9; i++) {
                        if (i != slot && player.getInventory().getItem(i).isEmpty()) {
                            swapHotbarToOffhand(mc, player, i);
                            break;
                        }
                    }
                }
            }
        }
    }

    private static void swapHotbarToOffhand(Minecraft mc, Player player, int hotbarSlot) {
        int prevSelected = player.getInventory().selected;
        player.getInventory().selected = hotbarSlot;
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                net.minecraft.core.BlockPos.ZERO,
                net.minecraft.core.Direction.DOWN
            ));
        }
        player.getInventory().selected = prevSelected;
    }

    private static double getWeaponReach(LocalPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        double reach = player.getAttributeValue(net.minecraftforge.common.ForgeMod.ENTITY_REACH.get());
        if (reach <= 0) reach = 3.0;

        WeaponClass wc = WeaponClassificationEngine.classify(mainHand);
        if (wc == WeaponClass.MELEE) {
            reach += 0.5D;
        }

        if (isBCLoaded() && Config.enableBetterCombatIntegration) {
            var styleOpt = BetterCombatWeaponAnalyzer.getWeaponStyle(mainHand);
            if (styleOpt.isPresent()) {
                reach = styleOpt.get().getAttackRange();
            }
        }

        return reach + Config.meleeRangeBufferBlocks;
    }

    private static void lookAtTarget(LocalPlayer player, LivingEntity target) {
        double dx = target.getX() - player.getX();
        double dy = (target.getY() + target.getEyeHeight() * 0.75D) - (player.getY() + player.getEyeHeight());
        double dz = target.getZ() - player.getZ();
        double dh = Math.sqrt(dx * dx + dz * dz);
        float baseYaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float basePitch = (float) -(Math.atan2(dy, dh) * 180.0D / Math.PI);

        float yawNoise = (float) (Math.sin(stateTicks * 0.38) * 8.0);
        float pitchNoise = (float) (Math.sin(stateTicks * 0.27) * 4.0);

        float finalYaw = baseYaw + yawNoise;
        float finalPitch = Math.max(-80.0F, Math.min(80.0F, basePitch + pitchNoise));

        player.setYRot(finalYaw);
        player.setXRot(finalPitch);
        player.yRotO = finalYaw;
        player.xRotO = finalPitch;
        player.yHeadRot = finalYaw;
        player.yHeadRotO = finalYaw;
    }

    private static void executeMeleeCombat(Minecraft mc, LocalPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        WeaponClass wc = WeaponClassificationEngine.classify(mainHand);

        if (mc.options.keyUse.isDown() || player.isUsingItem()) {
            ItemStack using = player.getUseItem();
            if (using.getItem().isEdible() || using.getItem() instanceof ShieldItem) {
                mc.options.keyUse.setDown(false);
                if (mc.gameMode != null) {
                    mc.gameMode.releaseUsingItem(player);
                }
            }
        }

        if (wc == WeaponClass.HYBRID) {
            handleHybridMeleeLogic(mc, player);
            return;
        }

        boolean isBetterCombat = isBCLoaded() && Config.enableBetterCombatIntegration;
        if (isBetterCombat) {
            var styleOpt = BetterCombatWeaponAnalyzer.getWeaponStyle(mainHand);
            if (styleOpt.isPresent()) {
                var style = styleOpt.get();
                SpecialAbilityHandler.executeSpecialAbilities(mc, player, currentTarget, style);

                if (comboTicksRemaining <= 0) {
                    mc.options.keyAttack.setDown(true);
                    triggerClientAttack(mc);
                    currentComboStep = (currentComboStep + 1) % style.getComboLength();
                    double speed = style.getAttackSpeed();
                    comboTicksRemaining = (int) Math.max(5.0, (20.0 / speed));
                } else {
                    mc.options.keyAttack.setDown(false);
                }
                return;
            }
        }

        mc.options.keyAttack.setDown(false);
        if (player.getAttackStrengthScale(0.0F) >= 0.9F) {
            if (!triggerClientAttack(mc)) {
                mc.gameMode.attack(player, currentTarget);
                player.swing(InteractionHand.MAIN_HAND);
            }
        }
    }

    private static void handleHybridMeleeLogic(Minecraft mc, LocalPlayer player) {
        if (player.getAttackStrengthScale(0.0F) >= 0.9F) {
            if (!triggerClientAttack(mc)) {
                mc.gameMode.attack(player, currentTarget);
                player.swing(InteractionHand.MAIN_HAND);
            }
        }

        if (hybridSpecialCooldown <= 0 && hybridChargeTicks == 0) {
            if (player.getRandom().nextFloat() < 0.05F) {
                hybridChargeTicks = 1;
            }
        }

        if (hybridChargeTicks > 0) {
            int drawDuration = player.getMainHandItem().getItem().toString().toLowerCase().contains("incinerator") ? 45 : 20;
            if (hybridChargeTicks < drawDuration) {
                mc.options.keyUse.setDown(true);
                hybridChargeTicks++;
            } else {
                mc.options.keyUse.setDown(false);
                if (player.isUsingItem() && mc.gameMode != null) {
                    mc.gameMode.releaseUsingItem(player);
                }
                hybridChargeTicks = 0;
                hybridSpecialCooldown = 100 + player.getRandom().nextInt(80);
            }
        }
    }

    private static void executeRangedCombat(Minecraft mc, LocalPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        mc.options.keyAttack.setDown(false);

        if (mainHand.getItem() instanceof CrossbowItem) {
            boolean charged = CrossbowItem.isCharged(mainHand);
            if (charged) {
                if (rangedDrawTicks >= 0) {
                    mc.options.keyUse.setDown(false);
                    rangedDrawTicks = -5;
                } else if (rangedDrawTicks == -1) {
                    mc.options.keyUse.setDown(true);
                    rangedDrawTicks = 0;
                } else {
                    mc.options.keyUse.setDown(false);
                    rangedDrawTicks++;
                }
            } else {
                mc.options.keyUse.setDown(true);
                rangedDrawTicks = 1;
            }
        } else {
            int drawDuration = mainHand.getItem().toString().toLowerCase().contains("incinerator") ? 45 : 20;
            if (rangedDrawTicks < 0) {
                mc.options.keyUse.setDown(false);
                rangedDrawTicks++;
            } else if (rangedDrawTicks < drawDuration) {
                mc.options.keyUse.setDown(true);
                rangedDrawTicks++;
            } else {
                mc.options.keyUse.setDown(false);
                if (player.isUsingItem() && mc.gameMode != null) {
                    mc.gameMode.releaseUsingItem(player);
                }
                rangedDrawTicks = -5;
            }
        }
    }

    /**
     * Updates simulated input vectors based on the current state.
     */
    public static void applyMovementInput(Input input, LocalPlayer player) {
        if (currentTarget == null) {
            input.forwardImpulse = 0.0F;
            input.leftImpulse = 0.0F;
            input.up = false;
            input.down = false;
            input.left = false;
            input.right = false;
            return;
        }

        double fwd = 0.6 + 0.4 * Math.abs(Math.sin(stateTicks * 0.13));
        double side = Math.sin(stateTicks * 0.31) * 0.5;

        int flip = (stateTicks / 25) % 2 == 0 ? 1 : -1;
        side *= flip;

        input.forwardImpulse = (float) fwd;
        input.leftImpulse = (float) side;
        input.up = fwd > 0.1;
        input.down = false;
        input.left = side < -0.1;
        input.right = side > 0.1;

        if (player.horizontalCollision || (stateTicks % 40 == 0 && player.onGround())) {
            input.jumping = true;
        }
    }

    private static boolean triggerClientAttack(Minecraft mc) {
        try {
            java.lang.reflect.Method method = Minecraft.class.getDeclaredMethod("startAttack");
            method.setAccessible(true);
            method.invoke(mc);
            return true;
        } catch (Exception e) {
            try {
                java.lang.reflect.Method method = Minecraft.class.getDeclaredMethod("m_91244_");
                method.setAccessible(true);
                method.invoke(mc);
                return true;
            } catch (Exception ex) {
                try {
                    java.lang.reflect.Method method = Minecraft.class.getDeclaredMethod("m_91243_");
                    method.setAccessible(true);
                    method.invoke(mc);
                    return true;
                } catch (Exception ex2) {
                    return false;
                }
            }
        }
    }
    private static boolean isBCLoaded() {
        try {
            return net.minecraftforge.fml.ModList.get() != null &&
                   net.minecraftforge.fml.ModList.get().isLoaded("bettercombat");
        } catch (Exception | LinkageError e) {
            return false;
        }
    }
}
