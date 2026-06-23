package org.xeb.xeb.bettercombat;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.xeb.xeb.weapon.WeaponStyleData;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public class BetterCombatWeaponAnalyzer {
    private static final Map<Item, CachedStyle> STYLE_CACHE = new WeakHashMap<>();
    // Lazily initialized to avoid ModList.get() being called at class-load time (breaks unit tests)
    private static volatile BetterCombatCompatAdapter ADAPTER = null;

    private static BetterCombatCompatAdapter getAdapter() {
        if (ADAPTER == null) {
            synchronized (BetterCombatWeaponAnalyzer.class) {
                if (ADAPTER == null) {
                    try {
                        ADAPTER = new BetterCombatCompatAdapter();
                    } catch (Exception | LinkageError e) {
                        // In test environments ModList is not available; use a no-op adapter
                        ADAPTER = new BetterCombatCompatAdapter() {
                            @Override public boolean isLoaded() { return false; }
                        };
                    }
                }
            }
        }
        return ADAPTER;
    }

    public static Optional<WeaponStyleData> getWeaponStyle(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Optional.empty();
        Item item = stack.getItem();
        long now = System.currentTimeMillis();

        CachedStyle cached = STYLE_CACHE.get(item);
        if (cached != null && (now - cached.timestamp) < 10000) { // 10000 ms is roughly 200 ticks
            return cached.style;
        }

        Optional<WeaponStyleData> parsed;
        try {
            parsed = getAdapter().getWeaponStyle(stack);
        } catch (Exception | LinkageError e) {
            parsed = Optional.empty();
        }
        STYLE_CACHE.put(item, new CachedStyle(parsed, now));
        return parsed;
    }

    private static class CachedStyle {
        final Optional<WeaponStyleData> style;
        final long timestamp;

        CachedStyle(Optional<WeaponStyleData> style, long timestamp) {
            this.style = style;
            this.timestamp = timestamp;
        }
    }
}

