package org.xeb.xeb.bot;

public enum BotState {
    IDLE,
    SCANNING,
    APPROACHING,
    ATTACKING_MELEE,
    ATTACKING_RANGED,
    RETREATING,
    SWITCHING_WEAPON,
    SPECIAL_ABILITY,
    FROZEN_RECOVERY
}
