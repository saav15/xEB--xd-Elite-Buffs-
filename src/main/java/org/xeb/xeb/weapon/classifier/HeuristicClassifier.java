package org.xeb.xeb.weapon.classifier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.xeb.xeb.weapon.WeaponClass;
import org.xeb.xeb.weapon.WeaponClassifier;

public class HeuristicClassifier implements WeaponClassifier {

    private static final String[] MELEE_KEYWORDS = {
        "sword", "blade", "claymore", "katana", "rapier", "spear", "halberd", 
        "greataxe", "hammer", "maul", "dagger", "scythe", "glaive", "pike", 
        "cutlass", "saber", "scimitar", "cleaver", "fist", "gauntlet", "claw", 
        "tonfa", "mace", "battleaxe", "weapon", "scythe"
    };

    private static final String[] RANGED_KEYWORDS = {
        "bow", "crossbow", "longbow", "shortbow", "repeater", "railgun", 
        "musket", "pistol", "blaster", "cannon", "launcher", "throwstar", 
        "shuriken", "boomerang", "chakram", "disc", "trident"
    };

    private static final String[] MAGIC_KEYWORDS = {
        "staff", "wand", "tome", "grimoire", "spellbook"
    };

    @Override
    public WeaponClass classify(ItemStack stack) {
        if (stack.isEmpty()) return WeaponClass.NON_WEAPON;
        
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (rl == null) return WeaponClass.NON_WEAPON;
        
        String path = rl.getPath().toLowerCase();

        // Check HYBRID specific keyword first (like Incinerator)
        if (path.contains("incinerator")) {
            return WeaponClass.HYBRID;
        }

        for (String kw : MAGIC_KEYWORDS) {
            if (path.contains(kw)) {
                return WeaponClass.MAGIC;
            }
        }

        for (String kw : RANGED_KEYWORDS) {
            if (path.contains(kw)) {
                return WeaponClass.RANGED;
            }
        }

        for (String kw : MELEE_KEYWORDS) {
            if (path.contains(kw)) {
                return WeaponClass.MELEE;
            }
        }

        return WeaponClass.NON_WEAPON;
    }

    @Override
    public double confidence(ItemStack stack) {
        return classify(stack) != WeaponClass.NON_WEAPON ? 0.4 : 0.0;
    }
}
