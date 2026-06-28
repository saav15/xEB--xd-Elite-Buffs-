package org.xeb.xeb.weapon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeaponStyleData {
    private final double attackRange;
    private final int comboLength;
    private final double attackSpeed;
    private final List<String> specialAbilities;
    private final boolean twoHanded;

    // Per-combo-step data (synchronized with BetterCombat's attacks[] array).
    // When non-empty, perStepSpeeds.size() == comboLength and each entry is the
    // attack speed for that combo step. perStepAbilities mirrors the ability
    // (leap/thrust/parry/sweep or null) detected for each step.
    private final List<Double> perStepSpeeds;
    private final List<String> perStepAbilities;

    /** Legacy constructor: single speed + aggregate ability set. Keeps existing callers working. */
    public WeaponStyleData(double attackRange, int comboLength, double attackSpeed, List<String> specialAbilities, boolean twoHanded) {
        this(attackRange, comboLength, attackSpeed, specialAbilities, twoHanded,
                Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Full constructor with per-step combo data.
     *
     * @param perStepSpeeds    speed per combo step; if size != comboLength it is ignored at lookup time
     * @param perStepAbilities ability keyword per step (may contain nulls / empty strings)
     */
    public WeaponStyleData(double attackRange, int comboLength, double attackSpeed,
                           List<String> specialAbilities, boolean twoHanded,
                           List<Double> perStepSpeeds, List<String> perStepAbilities) {
        this.attackRange = attackRange;
        this.comboLength = Math.max(1, comboLength);
        this.attackSpeed = attackSpeed;
        this.specialAbilities = specialAbilities != null ? new ArrayList<>(specialAbilities) : new ArrayList<>();
        this.twoHanded = twoHanded;
        this.perStepSpeeds = perStepSpeeds != null ? new ArrayList<>(perStepSpeeds) : new ArrayList<>();
        this.perStepAbilities = perStepAbilities != null ? new ArrayList<>(perStepAbilities) : new ArrayList<>();
    }

    public double getAttackRange() {
        return attackRange;
    }

    public int getComboLength() {
        return comboLength;
    }

    public double getAttackSpeed() {
        return attackSpeed;
    }

    public List<String> getSpecialAbilities() {
        return specialAbilities;
    }

    public boolean isTwoHanded() {
        return twoHanded;
    }

    /** Per-step attack speeds. Empty when the source didn't expose per-step data. */
    public List<Double> getPerStepSpeeds() {
        return perStepSpeeds;
    }

    /** Per-step ability keywords (leap/thrust/parry/sweep or null). May be shorter than comboLength. */
    public List<String> getPerStepAbilities() {
        return perStepAbilities;
    }

    /**
     * Returns the attack speed for a specific combo step (0-indexed), falling back to the
     * average {@link #getAttackSpeed()} if per-step data is unavailable or the index is out of range.
     */
    public double getSpeedForStep(int step) {
        if (step >= 0 && step < perStepSpeeds.size()) {
            double s = perStepSpeeds.get(step);
            if (s > 0) return s;
        }
        return attackSpeed;
    }

    /**
     * Returns the ability keyword for a specific combo step, or {@code null} if none.
     * Out-of-range or absent data returns {@code null}.
     */
    public String getAbilityForStep(int step) {
        if (step >= 0 && step < perStepAbilities.size()) {
            return perStepAbilities.get(step);
        }
        return null;
    }

    /** True if per-step speed/ability data was populated from BetterCombat. */
    public boolean hasPerStepData() {
        return !perStepSpeeds.isEmpty();
    }
}
