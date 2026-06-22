package org.xeb.xeb.event;

import org.xeb.xeb.Xeb;
import org.xeb.xeb.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.TridentItem;
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

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !player.isAlive()) {
            currentClientTarget = null;
            return;
        }

        if (!player.hasEffect(ModEffects.MADNESS.get())) {
            currentClientTarget = null;
            return;
        }

        // 1. Scan for the closest valid target
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

        if (currentClientTarget != null) {
            // 2. Switch to the best weapon in hotbar
            int bestSlot = getBestWeaponSlot(player);
            if (bestSlot != player.getInventory().selected) {
                player.getInventory().selected = bestSlot;
            }

            // 3. Force player look direction at target
            double dx = currentClientTarget.getX() - player.getX();
            double dy = (currentClientTarget.getY() + currentClientTarget.getEyeHeight() * 0.75D) - (player.getY() + player.getEyeHeight());
            double dz = currentClientTarget.getZ() - player.getZ();
            double dh = Math.sqrt(dx * dx + dz * dz);
            float yaw = (float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
            float pitch = (float) -(Math.atan2(dy, dh) * 180.0D / Math.PI);

            player.setYRot(yaw);
            player.setXRot(pitch);
            player.yRotO = yaw;
            player.xRotO = pitch;
            player.yHeadRot = yaw;
            player.yHeadRotO = yaw;

            // 4. Attack if in reach
            double reach = player.getAttributeValue(net.minecraftforge.common.ForgeMod.ENTITY_REACH.get());
            if (player.distanceTo(currentClientTarget) <= reach) {
                if (player.getAttackStrengthScale(0.0F) >= 0.9F) {
                    mc.gameMode.attack(player, currentClientTarget);
                    player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        Player player = event.getEntity();
        if (player != null && player.level().isClientSide() && player.hasEffect(ModEffects.MADNESS.get()) && currentClientTarget != null) {
            net.minecraft.client.player.Input input = event.getInput();
            // Force player to sprint/move forward
            input.forwardImpulse = 1.0F;
            input.leftImpulse = 0.0F;
            input.up = true;
            input.down = false;
            input.left = false;
            input.right = false;
            
            // Auto-jump if hitting an obstacle
            if (player.horizontalCollision) {
                input.jumping = true;
            }
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
        
        // Pulsate red vignette opacity for visual feedback
        float time = (Minecraft.getInstance().level.getGameTime() + Minecraft.getInstance().getFrameTime());
        float alpha = 0.45F + 0.15F * (float) Math.sin(time / 6.0F);
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 0.0F, 0.0F, alpha);
        
        guiGraphics.blit(new ResourceLocation("textures/misc/vignette.png"), 0, 0, 0, 0.0F, 0.0F, width, height, width, height);
        
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
        com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
    }

    private static int getBestWeaponSlot(Player player) {
        double maxDmg = -1.0;
        int bestSlot = player.getInventory().selected;
        
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            
            double dmg = 0.0;
            // Get base attribute modifier for attack damage
            Collection<net.minecraft.world.entity.ai.attributes.AttributeModifier> modifiers = 
                    stack.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE);
            for (net.minecraft.world.entity.ai.attributes.AttributeModifier mod : modifiers) {
                dmg += mod.getAmount();
            }
            
            // Prioritize specific weapon types
            if (stack.getItem() instanceof SwordItem) {
                dmg += 4.0;
            } else if (stack.getItem() instanceof TridentItem) {
                dmg += 5.0;
            } else if (stack.getItem() instanceof DiggerItem) {
                dmg += 2.0;
            }
            
            if (dmg > maxDmg) {
                maxDmg = dmg;
                bestSlot = i;
            }
        }
        return bestSlot;
    }
}
