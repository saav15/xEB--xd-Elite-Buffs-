package org.xeb.xeb.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * Exhausted — Unique level.
 * While active:
 *  • Deals 0.5 damage (half-heart) every 4 seconds (80 ticks)
 *  • Prevents ALL passive health regeneration (checked in BuffTickHandler / heal event)
 *  • Automatically grants Adrenaline with the same duration when first applied
 *
 * Linked mechanic:  Exhausted ↔ Adrenaline
 *  - Exhausted is applied → Adrenaline appears with matching duration
 *  - Exhausted expires   → Adrenaline is stripped
 *  - Adrenaline expires  → Exhausted is NOT removed (they are separate clocks that
 *    just started at the same time)
 *
 * The Adrenaline grant and the heal-block are handled in BuffDamageHandler /
 * BuffTickHandler rather than here to keep cross-effect logic centralised.
 */
public class ExhaustedEffect extends MobEffect {
    /** Ticks between damage ticks (4 seconds). */
    private static final int DAMAGE_INTERVAL = 80;

    public ExhaustedEffect() {
        // Washed-out orange — physical depletion
        super(MobEffectCategory.HARMFUL, 0xC86400);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return;
        // Deal half-heart of true damage (bypasses armour so it always hurts)
        entity.hurt(entity.damageSources().magic(), 0.5F);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % DAMAGE_INTERVAL == 0;
    }
}
