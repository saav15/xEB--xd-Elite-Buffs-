package org.xeb.xeb.weapon;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.xeb.xeb.Config;

/**
 * Manages interval-based hotbar scanning and item switching for the client-side bot.
 */
public class HotbarScanner {
    private int ticksSinceLastScan = 0;
    private int lastSelectedSlot = -1;
    private ItemStack lastHeldStack = ItemStack.EMPTY;
    private int lastHeldDamage = 0;
    private boolean lastInMeleeRange = false;

    public void reset() {
        ticksSinceLastScan = 999; // Force immediate scan next tick
        lastSelectedSlot = -1;
        lastHeldStack = ItemStack.EMPTY;
        lastHeldDamage = 0;
        lastInMeleeRange = false;
    }

    /**
     * Determines the best weapon slot in the hotbar, applying frequency rules and trigger conditions.
     */
    public int getBestWeaponSlot(Player player, double distToTarget, boolean inMeleeRange) {
        ticksSinceLastScan++;

        boolean forceRescan = false;

        // Trigger 1: Held slot or item type changed
        int currentSlot = player.getInventory().selected;
        ItemStack currentStack = player.getMainHandItem();
        if (currentSlot != lastSelectedSlot || currentStack.getItem() != lastHeldStack.getItem()) {
            forceRescan = true;
        }

        // Trigger 2: Current item durability broke
        if (currentStack.isDamageableItem()) {
            int currentDamage = currentStack.getDamageValue();
            if (currentDamage != lastHeldDamage && currentDamage >= currentStack.getMaxDamage()) {
                forceRescan = true;
            }
            lastHeldDamage = currentDamage;
        }

        // Trigger 3: Distance crossed the melee/ranged threshold
        if (inMeleeRange != lastInMeleeRange) {
            forceRescan = true;
        }

        // Trigger 4: Interval reached
        if (ticksSinceLastScan >= Config.hotbarScanIntervalTicks) {
            forceRescan = true;
        }

        if (forceRescan) {
            ticksSinceLastScan = 0;
            lastSelectedSlot = currentSlot;
            lastHeldStack = currentStack.copy();
            lastInMeleeRange = inMeleeRange;

            int bestSlot = currentSlot;
            double maxScore = Double.NEGATIVE_INFINITY;

            for (int i = 0; i < 9; i++) {
                ItemStack slotStack = player.getInventory().getItem(i);
                double score = WeaponScoringEngine.calculateScore(slotStack, inMeleeRange, player);
                if (score > maxScore) {
                    maxScore = score;
                    bestSlot = i;
                }
            }

            // Post-selection check: If selected a bow/crossbow and has no arrow in offhand, check hotbar for ammo
            ItemStack chosenWeapon = player.getInventory().getItem(bestSlot);
            if (!chosenWeapon.isEmpty() && (chosenWeapon.getItem() instanceof BowItem || chosenWeapon.getItem() instanceof CrossbowItem)) {
                ItemStack offhand = player.getOffhandItem();
                if (!(offhand.getItem() instanceof ArrowItem)) {
                    // Try to find arrow in hotbar to swap to offhand
                    for (int i = 0; i < 9; i++) {
                        if (i != bestSlot) {
                            ItemStack arrowStack = player.getInventory().getItem(i);
                            if (arrowStack.getItem() instanceof ArrowItem) {
                                swapHotbarToOffhand(Minecraft.getInstance(), player, i);
                                break;
                            }
                        }
                    }
                }
            }

            return bestSlot;
        }

        return player.getInventory().selected;
    }

    private void swapHotbarToOffhand(Minecraft mc, Player player, int hotbarSlot) {
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
}
