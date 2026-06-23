package org.xeb.xeb.weapon;

import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.BeforeAll;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WeaponClassificationEngineTest {

    @BeforeAll
    public static void initRegistry() {
        try {
            net.minecraft.SharedConstants.tryDetectVersion();
            java.lang.reflect.Field field = net.minecraft.server.Bootstrap.class.getDeclaredField("isBootstrapped");
            field.setAccessible(true);
            field.set(null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ItemStack mockStack;
    private Item mockItem;

    @BeforeEach
    public void setup() {
        mockStack = mock(ItemStack.class);
        mockItem = mock(Item.class);
        when(mockStack.getItem()).thenReturn(mockItem);
        when(mockStack.isEmpty()).thenReturn(false);
    }

    @Test
    public void testClassifyVanillaSwordReturnsMelee() {
        SwordItem sword = mock(SwordItem.class);
        when(mockStack.getItem()).thenReturn(sword);
        when(mockStack.getTags()).thenAnswer(inv -> Stream.empty());

        WeaponClass wc = WeaponClassificationEngine.classify(mockStack);
        assertEquals(WeaponClass.MELEE, wc);
    }

    @Test
    public void testClassifyVanillaBowReturnsRanged() {
        BowItem bow = mock(BowItem.class);
        when(mockStack.getItem()).thenReturn(bow);
        when(mockStack.getTags()).thenAnswer(inv -> Stream.empty());

        WeaponClass wc = WeaponClassificationEngine.classify(mockStack);
        assertEquals(WeaponClass.RANGED, wc);
    }

    @Test
    public void testClassifyVanillaSteakReturnsNonWeapon() {
        Item food = mock(Item.class);
        when(mockStack.getItem()).thenReturn(food);
        when(mockStack.getTags()).thenAnswer(inv -> Stream.empty());

        WeaponClass wc = WeaponClassificationEngine.classify(mockStack);
        assertEquals(WeaponClass.NON_WEAPON, wc);
    }
}
