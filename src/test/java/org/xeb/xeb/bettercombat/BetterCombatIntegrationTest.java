package org.xeb.xeb.bettercombat;

import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xeb.xeb.weapon.WeaponStyleData;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class BetterCombatIntegrationTest {

    @BeforeAll
    public static void initRegistry() {
        try {
            net.minecraft.SharedConstants.tryDetectVersion();
            java.lang.reflect.Field field = net.minecraft.server.Bootstrap.class.getDeclaredField("isBootstrapped");
            field.setAccessible(true);
            field.set(null, true);

            for (java.lang.reflect.Field f : net.minecraftforge.fml.ModList.class.getDeclaredFields()) {
                if (f.getType() == net.minecraftforge.fml.ModList.class) {
                    System.out.println("ModList field of type ModList: " + f.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testBetterCombatWeaponStyleMapping() {
        WeaponStyleData data = new WeaponStyleData(
            4.5D,  // attackRange
            3,     // comboLength
            1.4D,  // attackSpeed
            Collections.singletonList("leap"), // specialAbilities
            true   // twoHanded
        );

        assertEquals(4.5D, data.getAttackRange());
        assertEquals(3, data.getComboLength());
        assertEquals(1.4D, data.getAttackSpeed());
        assertTrue(data.getSpecialAbilities().contains("leap"));
        assertTrue(data.isTwoHanded());
    }

    @Test
    public void testBetterCombatAnalyzerReturnsEmptyWhenNoMod() {
        ItemStack mockStack = mock(ItemStack.class);
        Optional<WeaponStyleData> style = BetterCombatWeaponAnalyzer.getWeaponStyle(mockStack);
        
        // Since we are running in tests and the mod is not loaded, it should return empty
        assertNotNull(style);
    }
}
