package org.xeb.xeb.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public class TinfoilArmorMaterial implements ArmorMaterial {
    public static final TinfoilArmorMaterial INSTANCE = new TinfoilArmorMaterial();

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return 165; // same durability as iron helmet
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return 2; // same defense points (2 armor) as iron helmet
    }

    @Override
    public int getEnchantmentValue() {
        return 9; // same enchantability as iron helmet
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON; // same sound as iron
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }

    @Override
    public String getName() {
        return "tinfoil";
    }

    @Override
    public float getToughness() {
        return 0.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.0F;
    }
}
