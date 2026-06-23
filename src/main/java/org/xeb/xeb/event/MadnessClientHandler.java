package org.xeb.xeb.event;

import org.xeb.xeb.Xeb;
import org.xeb.xeb.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.List;

@Mod.EventBusSubscriber(modid = Xeb.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MadnessClientHandler {

    private static LivingEntity currentClientTarget = null;
    // Offset ticks for erratic look direction
    private static int franticTimer = 0;
    private static float yawNoise = 0.0F;
    private static float pitchNoise = 0.0F;
    // Track ranged draw state
    private static int rangedDrawTicks = 0;
    // Random right-click cooldown
    private static int rightClickCooldown = 0;
    private static boolean wasActive = false;

    public static boolean isRangedWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem) {
            return true;
        }
        UseAnim anim = stack.getUseAnimation();
        return anim == UseAnim.BOW || anim == UseAnim.CROSSBOW || anim == UseAnim.SPEAR;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !player.isAlive()) {
            if (wasActive) {
                reset();
            }
            return;
        }

        if (!player.hasEffect(ModEffects.MADNESS.get())) {
            if (wasActive) {
                reset();
            }
            return;
        }

        wasActive = true;

        franticTimer++;
        if (rightClickCooldown > 0) rightClickCooldown--;

        // ── 1. Find best target ──────────────────────────────────────────────
        double range = 16.0D;
        AABB searchBox = player.getBoundingBox().inflate(range);
        List<LivingEntity> entities = player.level().getEntitiesOfClass(
                LivingEntity.class, searchBox,
                target -> target != player && target.isAlive() && player.hasLineOfSight(target) &&
                          !(target instanceof Player p && (p.isCreative() || p.isSpectator()))
        );

        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (LivingEntity entity : entities) {
            double dist = player.distanceToSqr(entity);
            if (dist < closestDist) {
                closestDist = dist;
                closest = entity;
            }
        }
        currentClientTarget = closest;

        if (currentClientTarget == null) {
            mc.options.keyAttack.setDown(false);
            return;
        }

        double distToTarget = player.distanceTo(currentClientTarget);

        // ── 2. Switch to best weapon in hotbar ───────────────────────────────
        int bestSlot = getBestWeaponSlot(player, distToTarget);
        if (bestSlot != player.getInventory().selected) {
            player.getInventory().selected = bestSlot;
            rangedDrawTicks = 0; // Reset draw ticks when switching items!
            mc.options.keyUse.setDown(false); // Reset key state
        }

        // ── 3. Frantic look direction ─────────────────────────────────────────
        double dx = currentClientTarget.getX() - player.getX();
        double dy = (currentClientTarget.getY() + currentClientTarget.getEyeHeight() * 0.75D)
                - (player.getY() + player.getEyeHeight());
        double dz = currentClientTarget.getZ() - player.getZ();
        double dh = Math.sqrt(dx * dx + dz * dz);
        float baseYaw   = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float basePitch = (float) -(Math.atan2(dy, dh) * 180.0D / Math.PI);

        // Smooth noise oscillation + sudden jerks every ~15 ticks
        yawNoise   = (float) (Math.sin(franticTimer * 0.38) * 8.0 + Math.sin(franticTimer * 1.1) * 3.0);
        pitchNoise = (float) (Math.sin(franticTimer * 0.27) * 4.0);
        if (franticTimer % 15 == 0) {
            yawNoise   += (player.getRandom().nextFloat() - 0.5F) * 30.0F;
            pitchNoise += (player.getRandom().nextFloat() - 0.5F) * 10.0F;
        }

        float finalYaw   = baseYaw + yawNoise;
        float finalPitch = Math.max(-80.0F, Math.min(80.0F, basePitch + pitchNoise));

        player.setYRot(finalYaw);
        player.setXRot(finalPitch);
        player.yRotO = finalYaw;
        player.xRotO = finalPitch;
        player.yHeadRot = finalYaw;
        player.yHeadRotO = finalYaw;

        // ── 4. Combat: ranged vs melee ────────────────────────────────────────
        ItemStack mainHand = player.getMainHandItem();
        boolean hasRanged = isRangedWeapon(mainHand);

        if (hasRanged && distToTarget > 5.0D) {
            // ── 4a. Ranged attack ──────────────────────────────────────────
            mc.options.keyAttack.setDown(false); // Make sure attack key is released in ranged mode
            handleRangedAttack(mc, player, mainHand, distToTarget);
        } else {
            // ── 4b. Melee attack via startAttack/reflection (triggers Better Combat combos if installed) ──
            rangedDrawTicks = 0;
            mc.options.keyUse.setDown(false);

            double reach = player.getAttributeValue(net.minecraftforge.common.ForgeMod.ENTITY_REACH.get());
            boolean isBetterCombat = net.minecraftforge.fml.ModList.get().isLoaded("bettercombat");

            if (distToTarget <= reach) {
                if (isBetterCombat) {
                    mc.options.keyAttack.setDown(true);
                } else {
                    if (player.getAttackStrengthScale(0.0F) >= 0.9F) {
                        if (!triggerClientAttack(mc)) {
                            mc.gameMode.attack(player, currentClientTarget);
                            player.swing(InteractionHand.MAIN_HAND);
                        }
                    }
                }
            } else {
                mc.options.keyAttack.setDown(false);
            }

            // ── 4c. Random right-click ability ────────────────────────────
            if (rightClickCooldown <= 0) {
                ItemStack held = player.getMainHandItem();
                ItemStack offhand = player.getOffhandItem();
                ItemStack toUse = null;
                InteractionHand useHand = null;

                // Prefer offhand ability if it has use duration
                if (!offhand.isEmpty() && offhand.getUseDuration() > 0) {
                    toUse = offhand; useHand = InteractionHand.OFF_HAND;
                } else if (!held.isEmpty() && held.getUseDuration() > 0
                        && !isRangedWeapon(held)) {
                    toUse = held; useHand = InteractionHand.MAIN_HAND;
                }

                if (toUse != null) {
                    mc.options.keyUse.setDown(true);
                    // Release next tick — just a pulse
                    rightClickCooldown = 20 + player.getRandom().nextInt(60); // 1–4 seconds
                } else {
                    rightClickCooldown = 5;
                }
            } else if (rightClickCooldown == 1) {
                mc.options.keyUse.setDown(false);
            }
        }
    }

    // ── Ranged weapon draw/release ─────────────────────────────────────────────
    private static void handleRangedAttack(Minecraft mc, LocalPlayer player, ItemStack mainHand, double dist) {
        if (mainHand.getItem() instanceof CrossbowItem || mainHand.getUseAnimation() == UseAnim.CROSSBOW) {
            boolean charged = CrossbowItem.isCharged(mainHand);
            if (charged) {
                // If it's charged, we want to fire it!
                // To fire, we need a click sequence: release then press
                if (rangedDrawTicks >= 0) {
                    // Release the draw key mapping first (so it registers release after charging)
                    mc.options.keyUse.setDown(false);
                    rangedDrawTicks = -5; // Cooldown / release phase
                } else if (rangedDrawTicks == -1) {
                    // Last tick of cooldown: pulse use button to fire
                    mc.options.keyUse.setDown(true);
                    rangedDrawTicks = 0; // Reset
                } else {
                    // Cooldown countdown
                    mc.options.keyUse.setDown(false);
                    rangedDrawTicks++;
                }
            } else {
                // Not charged: hold use to charge it
                mc.options.keyUse.setDown(true);
                rangedDrawTicks = 1;
            }
        } else {
            // Bow or Trident or any modded charge weapon
            int drawDuration = 20;

            if (rangedDrawTicks < 0) {
                // Cooldown/release phase (explicitly set keyUse to false)
                mc.options.keyUse.setDown(false);
                rangedDrawTicks++;
            } else if (rangedDrawTicks < drawDuration) {
                // Draw/charge phase
                mc.options.keyUse.setDown(true);
                rangedDrawTicks++;
            } else {
                // Release — fire the projectile!
                mc.options.keyUse.setDown(false);
                if (player.isUsingItem() && mc.gameMode != null) {
                    mc.gameMode.releaseUsingItem(player);
                }
                rangedDrawTicks = -5; // Wait 5 ticks before starting next draw
            }
        }
    }

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        Player player = event.getEntity();
        if (player == null || !player.level().isClientSide()) return;
        if (!player.hasEffect(ModEffects.MADNESS.get())) return;
        if (currentClientTarget == null) return;

        net.minecraft.client.player.Input input = event.getInput();

        // Frantic movement: mostly forward but with random strafe and speed variation
        // Use franticTimer for deterministic-but-chaotic variation
        double fwd  = 0.6 + 0.4 * Math.abs(Math.sin(franticTimer * 0.13));
        double side = Math.sin(franticTimer * 0.31) * 0.5; // oscillating strafe

        // Sudden direction flip every 20–40 ticks
        int flip = (franticTimer / 25) % 2 == 0 ? 1 : -1;
        side *= flip;

        input.forwardImpulse = (float) fwd;
        input.leftImpulse    = (float) side;
        input.up    = fwd > 0.1;
        input.down  = false;
        input.left  = side < -0.1;
        input.right = side > 0.1;

        // Auto-jump on collision or randomly (~5% chance per tick)
        if (player.horizontalCollision || (franticTimer % 40 == 0 && player.onGround())) {
            input.jumping = true;
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.VIGNETTE.id())) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.hasEffect(ModEffects.MADNESS.get())) {
                renderRedBorder(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight());
            }
        }
    }

    private static void renderRedBorder(net.minecraft.client.gui.GuiGraphics guiGraphics, int width, int height) {
        com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
        com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();

        float time = (Minecraft.getInstance().level.getGameTime() + Minecraft.getInstance().getFrameTime());
        float alpha = 0.45F + 0.15F * (float) Math.sin(time / 6.0F);
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 0.0F, 0.0F, alpha);

        guiGraphics.blit(new ResourceLocation("textures/misc/vignette.png"), 0, 0, 0, 0.0F, 0.0F, width, height, width, height);

        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
        com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
    }

    private static boolean triggerClientAttack(Minecraft mc) {
        try {
            // Mojang mappings: startAttack
            java.lang.reflect.Method method = Minecraft.class.getDeclaredMethod("startAttack");
            method.setAccessible(true);
            method.invoke(mc);
            return true;
        } catch (NoSuchMethodException e) {
            try {
                // SRG mappings: m_91244_ or m_91243_ depending on exact mappings version
                java.lang.reflect.Method method = Minecraft.class.getDeclaredMethod("m_91244_");
                method.setAccessible(true);
                method.invoke(mc);
                return true;
            } catch (NoSuchMethodException ex) {
                try {
                    java.lang.reflect.Method method = Minecraft.class.getDeclaredMethod("m_91243_");
                    method.setAccessible(true);
                    method.invoke(mc);
                    return true;
                } catch (Exception ex2) {
                    // Fallback
                }
            } catch (Exception ex) {
                // Fallback
            }
        } catch (Exception e) {
            // Fallback
        }
        return false;
    }

    private static void reset() {
        wasActive = false;
        currentClientTarget = null;
        franticTimer = 0;
        yawNoise = 0.0F;
        pitchNoise = 0.0F;
        rangedDrawTicks = 0;
        rightClickCooldown = 0;
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            if (mc.options != null) {
                mc.options.keyUse.setDown(false);
                mc.options.keyAttack.setDown(false);
            }
            if (mc.player != null && mc.player.isUsingItem() && mc.gameMode != null) {
                mc.gameMode.releaseUsingItem(mc.player);
            }
        }
    }

    private static int getBestWeaponSlot(Player player, double distToTarget) {
        int bestSlot = player.getInventory().selected;
        double maxDmg = Double.NEGATIVE_INFINITY;
        boolean preferRanged = distToTarget > 5.0D;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            double score = 0.0;
            Collection<net.minecraft.world.entity.ai.attributes.AttributeModifier> modifiers =
                    stack.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE);
            for (net.minecraft.world.entity.ai.attributes.AttributeModifier mod : modifiers) {
                score += mod.getAmount();
            }

            if (stack.getItem() instanceof SwordItem)   score += 4.0;
            else if (stack.getItem() instanceof TridentItem) score += 5.0;
            else if (stack.getItem() instanceof DiggerItem)  score += 2.0;

            boolean isRangedItem = isRangedWeapon(stack);

            if (preferRanged) {
                if (isRangedItem) {
                    score += 20.0; // Heavily prioritize ranged items if target is far
                }
            } else {
                if (isRangedItem) {
                    score -= 20.0; // Heavily penalize ranged items if target is close
                }
            }

            if (score > maxDmg) {
                maxDmg = score;
                bestSlot = i;
            }
        }
        return bestSlot;
    }
}
