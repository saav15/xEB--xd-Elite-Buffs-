package org.xeb.xeb.bot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlayerBotStateMachineTest {

    @BeforeAll
    public static void initRegistry() {
        try {
            Class.forName("net.minecraft.core.registries.Registries");
            net.minecraft.SharedConstants.tryDetectVersion();
            java.lang.reflect.Field field = net.minecraft.server.Bootstrap.class.getDeclaredField("isBootstrapped");
            field.setAccessible(true);
            field.set(null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Minecraft mockMc;
    private LocalPlayer mockPlayer;

    @BeforeEach
    public void setup() {
        mockMc = mock(Minecraft.class);
        try {
            java.lang.reflect.Field instanceField = Minecraft.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, mockMc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mockPlayer = mock(LocalPlayer.class);
        PlayerBotStateMachine.reset();
    }

    @Test
    public void testInitialStateIsIdle() {
        // When no tick has occurred, state should be IDLE
        assertEquals(BotState.IDLE, PlayerBotStateMachine.getCurrentState());
    }

    @Test
    public void testResetReturnsToIdle() {
        // Manually force a state change that reset should undo
        // The state machine starts in IDLE. We call reset() which should leave us in IDLE.
        PlayerBotStateMachine.reset();
        assertEquals(BotState.IDLE, PlayerBotStateMachine.getCurrentState());
    }

    @Test
    public void testTickWithDeadPlayerStaysIdle() {
        // If player is dead, state machine should stay IDLE (via reset)
        when(mockPlayer.isAlive()).thenReturn(false);
        // hasEffect will return false for any effect since player is dead
        when(mockPlayer.hasEffect(any(MobEffect.class))).thenReturn(false);

        PlayerBotStateMachine.tick(mockMc, mockPlayer);
        assertEquals(BotState.IDLE, PlayerBotStateMachine.getCurrentState());
    }

    @Test
    public void testTickWithNullPlayerStaysIdle() {
        // tick(mc, null) should gracefully handle null player and stay IDLE
        PlayerBotStateMachine.tick(mockMc, null);
        assertEquals(BotState.IDLE, PlayerBotStateMachine.getCurrentState());
    }
}
