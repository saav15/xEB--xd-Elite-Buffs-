package org.xeb.xeb.boss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import org.xeb.xeb.Config;

/**
 * Manages target candidate expansion for bosses when restricted pools are empty.
 */
public class BossTargetCandidateExpander {
    private static final String EXPANDED_TIMER_KEY = "xebTargetExpansionTimer";

    /**
     * Determines whether the boss is currently configured or dynamically expanded to attack all mobs.
     */
    public static boolean shouldAttackAllMobs(LivingEntity boss) {
        if (Config.bossAttackAllMobs) {
            return true;
        }
        return boss.getPersistentData().contains(EXPANDED_TIMER_KEY);
    }

    /**
     * Starts a temporary expansion phase (e.g. 100 ticks).
     */
    public static void startExpansion(LivingEntity boss) {
        CompoundTag tag = boss.getPersistentData();
        tag.putInt(EXPANDED_TIMER_KEY, Config.targetExpansionDurationTicks);
    }

    /**
     * Ticks down the temporary expansion timer.
     */
    public static void tick(LivingEntity boss) {
        CompoundTag tag = boss.getPersistentData();
        if (tag.contains(EXPANDED_TIMER_KEY)) {
            int ticks = tag.getInt(EXPANDED_TIMER_KEY) - 1;
            if (ticks <= 0) {
                tag.remove(EXPANDED_TIMER_KEY);
            } else {
                tag.putInt(EXPANDED_TIMER_KEY, ticks);
            }
        }
    }
}
