package org.xeb.xeb.weapon;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.xeb.xeb.weapon.classifier.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Orchestrates multiple WeaponClassifier implementations in priority order,
 * caching results to minimize tick overhead.
 */
public class WeaponClassificationEngine {
    private static final Map<Item, CachedClass> CACHE = new WeakHashMap<>();
    private static final List<WeaponClassifier> CLASSIFIERS = new ArrayList<>();

    static {
        // Prioritized detection pipeline (Tag-based > Attribute-based > Class-based > Capability-based > Heuristics)
        CLASSIFIERS.add(new TagBasedClassifier());
        CLASSIFIERS.add(new AttributeBasedClassifier());
        CLASSIFIERS.add(new ClassBasedClassifier());
        CLASSIFIERS.add(new CapabilityBasedClassifier());
        CLASSIFIERS.add(new HeuristicClassifier());
    }

    /**
     * Resolves the WeaponClass for a given ItemStack using the prioritized classifier list.
     */
    public static WeaponClass classify(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return WeaponClass.NON_WEAPON;
        Item item = stack.getItem();
        long now = System.currentTimeMillis();

        synchronized (CACHE) {
            CachedClass cached = CACHE.get(item);
            if (cached != null && (now - cached.timestamp) < 5000) { // 5000ms is ~100 ticks
                return cached.weaponClass;
            }
        }

        WeaponClass bestClass = WeaponClass.NON_WEAPON;
        double bestConf = -1.0;

        for (WeaponClassifier classifier : CLASSIFIERS) {
            double conf = classifier.confidence(stack);
            if (conf > bestConf && conf > 0.0) {
                WeaponClass wc = classifier.classify(stack);
                if (wc != WeaponClass.NON_WEAPON) {
                    bestConf = conf;
                    bestClass = wc;
                }
            }
        }

        synchronized (CACHE) {
            CACHE.put(item, new CachedClass(bestClass, now));
        }

        return bestClass;
    }

    private static class CachedClass {
        final WeaponClass weaponClass;
        final long timestamp;

        CachedClass(WeaponClass weaponClass, long timestamp) {
            this.weaponClass = weaponClass;
            this.timestamp = timestamp;
        }
    }
}
