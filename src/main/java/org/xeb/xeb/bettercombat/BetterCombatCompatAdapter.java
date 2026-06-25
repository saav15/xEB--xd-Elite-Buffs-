package org.xeb.xeb.bettercombat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.xeb.xeb.compat.ModCompatAdapter;
import org.xeb.xeb.weapon.WeaponClass;
import org.xeb.xeb.weapon.WeaponStyleData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BetterCombatCompatAdapter implements ModCompatAdapter {
    private static final String MOD_ID = "bettercombat";
    private final boolean loaded;

    public BetterCombatCompatAdapter() {
        boolean modLoaded = false;
        try {
            modLoaded = ModList.get() != null && ModList.get().isLoaded(MOD_ID);
        } catch (Exception | LinkageError ignored) {}
        this.loaded = modLoaded;
    }

    @Override
    public String modId() {
        return MOD_ID;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public WeaponClass classifyWeapon(ItemStack stack) {
        if (!loaded) return WeaponClass.NON_WEAPON;
        Optional<WeaponStyleData> style = getWeaponStyle(stack);
        return style.isPresent() ? WeaponClass.MELEE : WeaponClass.NON_WEAPON;
    }

    @Override
    public boolean isBoss(LivingEntity entity) {
        return false;
    }

    @Override
    public Optional<WeaponStyleData> getWeaponStyle(ItemStack stack) {
        if (!loaded || stack.isEmpty()) return Optional.empty();
        try {
            Class<?> registryClass = Class.forName("net.bettercombat.api.WeaponRegistry");
            java.lang.reflect.Method getAttributes = registryClass.getMethod("getAttributes", ItemStack.class);
            Object attributes = getAttributes.invoke(null, stack);
            if (attributes == null) return Optional.empty();

            // Parse Range
            double attackRange = 3.0D;
            try {
                java.lang.reflect.Method rangeMethod = attributes.getClass().getMethod("range");
                attackRange = ((Number) rangeMethod.invoke(attributes)).doubleValue();
            } catch (Exception e) {
                try {
                    java.lang.reflect.Field rangeField = attributes.getClass().getField("range");
                    attackRange = rangeField.getDouble(attributes);
                } catch (Exception ignored) {}
            }

            // Parse Two Handed
            boolean twoHanded = false;
            try {
                java.lang.reflect.Method twoHandedMethod = attributes.getClass().getMethod("isTwoHanded");
                twoHanded = (Boolean) twoHandedMethod.invoke(attributes);
            } catch (Exception e) {
                try {
                    java.lang.reflect.Field twoHandedField = attributes.getClass().getField("isTwoHanded");
                    twoHanded = twoHandedField.getBoolean(attributes);
                } catch (Exception ignored) {}
            }

            // Parse Attacks / Combo steps
            int comboLength = 1;
            double attackSpeed = 1.6D; // fallback
            List<String> abilities = new ArrayList<>();
            try {
                java.lang.reflect.Method attacksMethod = attributes.getClass().getMethod("attacks");
                Object[] attacks = (Object[]) attacksMethod.invoke(attributes);
                if (attacks != null) {
                    comboLength = attacks.length;
                    for (Object attack : attacks) {
                        // Try to read per-attack speed (field: "speed" or method "speed()")
                        try {
                            java.lang.reflect.Method speedMethod = attack.getClass().getMethod("speed");
                            double s = ((Number) speedMethod.invoke(attack)).doubleValue();
                            if (s > 0) attackSpeed = s;
                        } catch (Exception e1) {
                            try {
                                java.lang.reflect.Field speedField = attack.getClass().getField("speed");
                                double s = speedField.getDouble(attack);
                                if (s > 0) attackSpeed = s;
                            } catch (Exception ignored) {}
                        }

                        // Detect special abilities from animation name
                        try {
                            java.lang.reflect.Method animMethod = attack.getClass().getMethod("animation");
                            String anim = (String) animMethod.invoke(attack);
                            if (anim != null) {
                                String lower = anim.toLowerCase();
                                if (lower.contains("leap") || lower.contains("jump")) abilities.add("leap");
                                if (lower.contains("thrust") || lower.contains("stab")) abilities.add("thrust");
                                if (lower.contains("parry") || lower.contains("block")) abilities.add("parry");
                                if (lower.contains("sweep") || lower.contains("spin") || lower.contains("whirl")) abilities.add("sweep");
                            }
                        } catch (Exception ignored) {}
                    }
                }
            } catch (Exception ignored) {}

            return Optional.of(new WeaponStyleData(attackRange, comboLength, attackSpeed, abilities, twoHanded));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    @Override
    public boolean isItemNonWeapon(ItemStack stack) {
        return false;
    }
}
