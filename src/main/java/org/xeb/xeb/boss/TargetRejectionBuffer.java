package org.xeb.xeb.boss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a persistent ring buffer in NBT to prevent infinite targeting loops on mobs.
 */
public class TargetRejectionBuffer {
    private static final String BUFFER_KEY = "xebRejectedTargets";
    private static final String TIMER_KEY = "xebRejectionTimer";

    /**
     * Appends a target ID to the NBT buffer, maintaining a maximum size of 5.
     */
    public static void addRejectedTarget(LivingEntity boss, int targetId) {
        CompoundTag tag = boss.getPersistentData();
        List<Integer> list = getRejectedTargets(boss);

        if (!list.contains(targetId)) {
            list.add(targetId);
            if (list.size() > 5) {
                list.remove(0);
            }
        }

        ListTag listTag = new ListTag();
        for (int id : list) {
            listTag.add(IntTag.valueOf(id));
        }
        tag.put(BUFFER_KEY, listTag);

        if (!tag.contains(TIMER_KEY)) {
            tag.putInt(TIMER_KEY, 0);
        }
    }

    /**
     * Retrieves the list of rejected target IDs from the boss's NBT.
     */
    public static List<Integer> getRejectedTargets(LivingEntity boss) {
        CompoundTag tag = boss.getPersistentData();
        List<Integer> list = new ArrayList<>();
        if (tag.contains(BUFFER_KEY)) {
            ListTag listTag = tag.getList(BUFFER_KEY, 3); // 3 represents IntTag
            for (int i = 0; i < listTag.size(); i++) {
                list.add(listTag.getInt(i));
            }
        }
        return list;
    }

    /**
     * Ticks the buffer, clearing all rejected target IDs after 200 ticks.
     */
    public static void tick(LivingEntity boss) {
        CompoundTag tag = boss.getPersistentData();
        if (tag.contains(TIMER_KEY)) {
            int timer = tag.getInt(TIMER_KEY) + 1;
            if (timer >= 200) {
                tag.remove(BUFFER_KEY);
                tag.remove(TIMER_KEY);
            } else {
                tag.putInt(TIMER_KEY, timer);
            }
        }
    }

    /**
     * Checks if a specific target ID is currently rejected.
     */
    public static boolean isRejected(LivingEntity boss, int targetId) {
        return getRejectedTargets(boss).contains(targetId);
    }
}
