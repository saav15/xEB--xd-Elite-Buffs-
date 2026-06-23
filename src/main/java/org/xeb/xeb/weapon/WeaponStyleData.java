package org.xeb.xeb.weapon;

import java.util.ArrayList;
import java.util.List;

public class WeaponStyleData {
    private final double attackRange;
    private final int comboLength;
    private final double attackSpeed;
    private final List<String> specialAbilities;
    private final boolean twoHanded;

    public WeaponStyleData(double attackRange, int comboLength, double attackSpeed, List<String> specialAbilities, boolean twoHanded) {
        this.attackRange = attackRange;
        this.comboLength = comboLength;
        this.attackSpeed = attackSpeed;
        this.specialAbilities = specialAbilities != null ? new ArrayList<>(specialAbilities) : new ArrayList<>();
        this.twoHanded = twoHanded;
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
}
