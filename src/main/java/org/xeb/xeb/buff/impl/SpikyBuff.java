package org.xeb.xeb.buff.impl;

import org.xeb.xeb.buff.BuffType;
import org.xeb.xeb.buff.EliteBuff;
import org.xeb.xeb.medallion.MedallionManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class SpikyBuff extends EliteBuff {
    private static final String THORNS_KEY = "xebThornsAmount";

    public SpikyBuff() {
        super("spiky", "Spiky", BuffType.UNIVERSAL, 0x8B4513, 10.0D);
    }

    @Override
    public void onAttach(LivingEntity entity) {
        int amount = MedallionManager.isBoss(entity) ? 1 : 2;
        entity.getPersistentData().putInt(THORNS_KEY, amount);
    }

    @Override
    public void onDetach(LivingEntity entity) {
        entity.getPersistentData().remove(THORNS_KEY);
    }

    @Override
    public void onServerTick(LivingEntity entity, ServerLevel level) {}

    @Override
    public void onDamageTaken(LivingEntity entity, LivingHurtEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof LivingEntity livingAttacker && attacker != entity) {
            // Check if damage source is direct melee / physical (e.g. not thorns itself to avoid loops!)
            if (!event.getSource().is(net.minecraft.world.damagesource.DamageTypes.THORNS)) {
                CompoundTag data = entity.getPersistentData();
                int thornsAmount = data.contains(THORNS_KEY) ? data.getInt(THORNS_KEY) : 1;
                livingAttacker.hurt(entity.damageSources().thorns(entity), thornsAmount);
            }
        }
    }
}
