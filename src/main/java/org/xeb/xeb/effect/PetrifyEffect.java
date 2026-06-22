package org.xeb.xeb.effect;

import org.xeb.xeb.medallion.MedallionManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class PetrifyEffect extends MobEffect {
    public PetrifyEffect() {
        super(MobEffectCategory.HARMFUL, 0x808080);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (MedallionManager.hasBuff(entity, "hardy")) {
            entity.removeEffect(this);
            return;
        }

        entity.setDeltaMovement(Vec3.ZERO);
        entity.hurtMarked = true;

        if (entity instanceof Mob mob) {
            if (!mob.isNoAi()) {
                mob.getPersistentData().putBoolean("xebRestoreAI", true);
                mob.setNoAi(true);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
