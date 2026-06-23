package org.xeb.xeb.weapon;

import com.google.common.collect.ImmutableMultimap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WeaponScoringEngineTest {

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
    private Player mockPlayer;

    @BeforeEach
    public void setup() {
        mockStack = mock(ItemStack.class);
        Item mockItem = mock(SwordItem.class); // Use SwordItem so classification returns MELEE
        mockPlayer = mock(Player.class);
        when(mockStack.getItem()).thenReturn(mockItem);
        when(mockStack.isEmpty()).thenReturn(false);
        when(mockStack.getTags()).thenAnswer(inv -> Stream.empty());
        when(mockStack.isDamageableItem()).thenReturn(false);
        // Stub getAttributeModifiers to return empty multimap to avoid NPE
        when(mockStack.getAttributeModifiers(any())).thenReturn(ImmutableMultimap.of());
        // Stub player cooldowns to avoid NPE
        net.minecraft.world.item.ItemCooldowns cooldowns = mock(net.minecraft.world.item.ItemCooldowns.class);
        when(cooldowns.getCooldownPercent(any(), any(Float.class))).thenReturn(0.0F);
        when(mockPlayer.getCooldowns()).thenReturn(cooldowns);
        // Stub projectile to return empty ItemStack to avoid NPE
        when(mockPlayer.getProjectile(any())).thenReturn(ItemStack.EMPTY);
        when(mockPlayer.isCreative()).thenReturn(false);
    }

    @Test
    public void testScoreMeleeWeaponInMeleeRangeBeatsRangedScore() {
        double scoreMelee = WeaponScoringEngine.calculateScore(mockStack, true, mockPlayer);
        double scoreRanged = WeaponScoringEngine.calculateScore(mockStack, false, mockPlayer);

        // Melee should get +5.0 bonus in melee range, so scoreMelee > scoreRanged
        assertTrue(scoreMelee > scoreRanged,
            "Melee score (" + scoreMelee + ") should exceed ranged score (" + scoreRanged + ") for SwordItem in melee range");
    }

    @Test
    public void testBrokenWeaponReturnsMinusThousand() {
        ItemStack brokenStack = mock(ItemStack.class);
        Item brokenItem = mock(SwordItem.class);
        when(brokenStack.isEmpty()).thenReturn(false);
        when(brokenStack.getItem()).thenReturn(brokenItem);
        when(brokenStack.getTags()).thenAnswer(inv -> Stream.empty());
        when(brokenStack.isDamageableItem()).thenReturn(true);
        when(brokenStack.getMaxDamage()).thenReturn(100);
        when(brokenStack.getDamageValue()).thenReturn(100); // 100% damaged
        when(brokenStack.getAttributeModifiers(any())).thenReturn(ImmutableMultimap.of());

        double score = WeaponScoringEngine.calculateScore(brokenStack, true, mockPlayer);
        assertTrue(score <= -1000.0, "Broken weapon score should be <= -1000, was: " + score);
    }
}
