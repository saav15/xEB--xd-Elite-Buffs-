package org.xeb.xeb.mana;

import org.xeb.xeb.attribute.ModAttributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

public class ManaManager {
    private static final String MANA_KEY = "xebCurrentMana";

    public static double getMana(LivingEntity entity) {
        if (entity instanceof Player player) {
            if (ModList.get().isLoaded("irons_spellbooks")) {
                float mana = IronsSpellsManaCompat.getMana(player);
                if (mana > 0) return mana;
            }
        }
        
        if (ModList.get().isLoaded("ars_nouveau")) {
            int mana = ArsNouveauManaCompat.getMana(entity);
            if (mana > 0) return mana;
        }

        // Fallback to custom attribute
        if (entity.getAttribute(ModAttributes.MANA.get()) != null) {
            CompoundTag data = entity.getPersistentData();
            if (!data.contains(MANA_KEY)) {
                double maxMana = entity.getAttributeValue(ModAttributes.MANA.get());
                data.putDouble(MANA_KEY, maxMana);
                return maxMana;
            }
            return data.getDouble(MANA_KEY);
        }

        return 0;
    }

    public static double getMaxMana(LivingEntity entity) {
        if (entity instanceof Player player) {
            if (ModList.get().isLoaded("irons_spellbooks")) {
                // Return default max since Irons Spells has internal max, or handle default
                // In most cases, we just return the query value if needed, but fallback is fine
            }
        }

        if (ModList.get().isLoaded("ars_nouveau")) {
            int max = ArsNouveauManaCompat.getMaxMana(entity);
            if (max > 0) return max;
        }

        if (entity.getAttribute(ModAttributes.MANA.get()) != null) {
            return entity.getAttributeValue(ModAttributes.MANA.get());
        }

        return 0;
    }

    public static boolean drainMana(LivingEntity entity, double amount) {
        if (entity instanceof Player player) {
            if (ModList.get().isLoaded("irons_spellbooks")) {
                if (IronsSpellsManaCompat.drainMana(player, (float) amount)) {
                    return true;
                }
            }
        }

        if (ModList.get().isLoaded("ars_nouveau")) {
            if (ArsNouveauManaCompat.drainMana(entity, (int) amount)) {
                return true;
            }
        }

        // Fallback to custom attribute
        if (entity.getAttribute(ModAttributes.MANA.get()) != null) {
            CompoundTag data = entity.getPersistentData();
            double current = getMana(entity);
            if (current >= amount) {
                data.putDouble(MANA_KEY, current - amount);
                return true;
            }
        }

        return false;
    }

    public static void regenMana(LivingEntity entity, double amount) {
        // We only manually regenerate for the custom attribute system,
        // since external mods (Iron's Spells, Ars Nouveau) manage their own regeneration loops.
        if (entity.getAttribute(ModAttributes.MANA.get()) != null) {
            CompoundTag data = entity.getPersistentData();
            double current = getMana(entity);
            double max = getMaxMana(entity);
            if (current < max) {
                data.putDouble(MANA_KEY, Math.min(max, current + amount));
            }
        }
    }
}
