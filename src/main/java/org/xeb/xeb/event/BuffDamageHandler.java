package org.xeb.xeb.event;

import org.xeb.xeb.Xeb;
import org.xeb.xeb.medallion.MedallionData;
import org.xeb.xeb.medallion.MedallionManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xeb.xeb.effect.ModEffects;

import java.util.List;

@Mod.EventBusSubscriber(modid = Xeb.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BuffDamageHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target == null) return;

        // Route damage taken to target's buffs
        List<MedallionData> targetMedallions = MedallionManager.getMedallions(target);
        if (!targetMedallions.isEmpty()) {
            for (MedallionData m : targetMedallions) {
                m.getBuff().onDamageTaken(target, event);
            }
        }

        // Route damage dealt to attacker's buffs
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof LivingEntity livingAttacker) {
            List<MedallionData> attackerMedallions = MedallionManager.getMedallions(livingAttacker);
            if (!attackerMedallions.isEmpty()) {
                for (MedallionData m : attackerMedallions) {
                    m.getBuff().onDamageDealt(livingAttacker, event);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        HitResult hit = event.getRayTraceResult();
        if (hit.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hit;
            if (entityHit.getEntity() instanceof LivingEntity target) {
                List<MedallionData> medallions = MedallionManager.getMedallions(target);
                if (!medallions.isEmpty()) {
                    for (MedallionData m : medallions) {
                        m.getBuff().onProjectileImpact(target, event);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity != null && MedallionManager.hasBuff(entity, "hardy")) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity != null && !entity.level().isClientSide() && entity.hasEffect(ModEffects.MADNESS.get())) {
            LivingEntity newTarget = event.getNewTarget();
            if (newTarget instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator())) {
                event.setNewTarget(null);
                return;
            }
            LivingEntity originalTarget = event.getOriginalTarget();
            if (newTarget == null && originalTarget != null && originalTarget.isAlive() && entity.distanceToSqr(originalTarget) < 256.0D) {
                if (!(originalTarget instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator()))) {
                    event.setNewTarget(originalTarget);
                }
            }
        }
    }
}
