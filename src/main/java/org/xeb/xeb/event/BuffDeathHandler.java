package org.xeb.xeb.event;

import org.xeb.xeb.Xeb;
import org.xeb.xeb.medallion.MedallionData;
import org.xeb.xeb.medallion.MedallionManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Xeb.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BuffDeathHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity target = event.getEntity();
        if (target == null) return;

        // Route death to target's buffs
        List<MedallionData> targetMedallions = MedallionManager.getMedallions(target);
        if (!targetMedallions.isEmpty()) {
            for (MedallionData m : targetMedallions) {
                m.getBuff().onDeath(target, event);
            }
        }

        // Route kill to attacker's buffs
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof LivingEntity livingAttacker) {
            List<MedallionData> attackerMedallions = MedallionManager.getMedallions(livingAttacker);
            if (!attackerMedallions.isEmpty()) {
                for (MedallionData m : attackerMedallions) {
                    m.getBuff().onKill(livingAttacker, event);
                }
            }
        }
    }
}
