package org.xeb.xeb.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Charged Fist — Unique offensive effect.
 *
 * Every time the affected entity deals damage (melee, ranged, or magic),
 * it fires 2–3 homing Sparkle projectiles at the nearest enemy — identical
 * to the Kinetic Spikes counter-attack logic, but triggered on the ATTACKER
 * side instead of on damage received.
 *
 * Scaling (same formula as Kinetic Spikes):
 *   projectileDamage = ATTACK_DAMAGE × (0.10 + 0.15 × amplifier)
 *
 * Handled in BuffDamageHandler.onLivingHurt (attacker branch).
 */
public class ChargedFistEffect extends MobEffect {

    public ChargedFistEffect() {
        // Electric blue — charged, offensive energy
        super(MobEffectCategory.BENEFICIAL, 0x4488FF);
    }
}
