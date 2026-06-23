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
import net.minecraftforge.event.entity.player.PlayerEvent;
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

        // Reduce damage dealt by entities affected by All Stats Down
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof LivingEntity livingAttacker) {
            if (livingAttacker.hasEffect(ModEffects.ALL_STATS_DOWN.get())) {
                int amp = livingAttacker.getEffect(ModEffects.ALL_STATS_DOWN.get()).getAmplifier();
                float reduction = 0.10F * (amp + 1); // 10%, 20%, 30%
                event.setAmount(event.getAmount() * (1.0F - reduction));
            }
            if (livingAttacker.hasEffect(ModEffects.ALL_STATS_UP.get())) {
                int amp = livingAttacker.getEffect(ModEffects.ALL_STATS_UP.get()).getAmplifier();
                float boost = 0.10F * (amp + 1); // +10%, +20%, +30%
                event.setAmount(event.getAmount() * (1.0F + boost));
            }
        }

        // Player-specific Holy Shield potion effect logic (only if not handled by medallion)
        if (target instanceof net.minecraft.world.entity.player.Player player && !player.level().isClientSide()) {
            if (player.hasEffect(ModEffects.HOLY_SHIELD.get()) && !player.getPersistentData().contains("xebHolyShield")) {
                net.minecraft.nbt.CompoundTag tag = player.getPersistentData();
                boolean hasTimer = tag.contains("xebPlayerHolyShieldTimer");
                boolean isActive = tag.contains("xebPlayerHolyShieldActive") ? tag.getBoolean("xebPlayerHolyShieldActive") : !hasTimer;

                if (isActive) {
                    // Absorb the full hit!
                    tag.putBoolean("xebPlayerHolyShieldActive", false);
                    tag.putInt("xebPlayerHolyShieldTimer", 800); // 40 seconds (800 ticks)

                    // Glass breaking sound
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            net.minecraft.sounds.SoundEvents.GLASS_BREAK, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

                    event.setAmount(0.0F);
                    event.setCanceled(true);

                    // Spawn particles
                    org.xeb.xeb.network.BuffParticlePacket packet = new org.xeb.xeb.network.BuffParticlePacket(player.getX(), player.getY(), player.getZ(), "revival", 15);
                    org.xeb.xeb.network.XEBNetwork.CHANNEL.send(net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), packet);
                    return;
                }
            }
        }

        // Route damage taken to target's buffs
        List<MedallionData> targetMedallions = MedallionManager.getMedallions(target);
        if (!targetMedallions.isEmpty()) {
            for (MedallionData m : targetMedallions) {
                m.getBuff().onDamageTaken(target, event);
            }
        }

        // Route damage dealt to attacker's buffs
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
        if (entity == null || entity.level().isClientSide() || !(entity instanceof net.minecraft.world.entity.Mob mob)) {
            return;
        }

        if (mob.hasEffect(ModEffects.MADNESS.get())) {
            net.minecraft.world.effect.MobEffectInstance effect = mob.getEffect(ModEffects.MADNESS.get());
            int amplifier = effect != null ? effect.getAmplifier() : 0;
            double range = 16.0D + amplifier * 4.0D;

            net.minecraft.nbt.CompoundTag tag = mob.getPersistentData();
            LivingEntity target = null;

            if (tag.contains("xebMadnessTargetId")) {
                int targetId = tag.getInt("xebMadnessTargetId");
                Entity cached = mob.level().getEntity(targetId);
                if (cached instanceof LivingEntity living && living.isAlive() && mob.distanceToSqr(living) <= range * range) {
                    if (!(living instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator()))) {
                        target = living;
                    }
                }
            }

            if (target == null) {
                target = findNewMadnessTarget(mob, range);
                if (target != null) {
                    tag.putInt("xebMadnessTargetId", target.getId());
                } else {
                    tag.remove("xebMadnessTargetId");
                }
            }

            if (target != null && event.getNewTarget() != target) {
                event.setNewTarget(target);
                mob.setLastHurtByMob(target);
                mob.setLastHurtMob(target);
            }
        }
    }

    private static LivingEntity findNewMadnessTarget(net.minecraft.world.entity.Mob mob, double range) {
        net.minecraft.world.phys.AABB searchBox = mob.getBoundingBox().inflate(range);
        List<LivingEntity> potentialTargets = mob.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                target -> target != mob && target.isAlive() && mob.hasLineOfSight(target) &&
                          !(target instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator())));
        
        if (!potentialTargets.isEmpty()) {
            return potentialTargets.get(mob.getRandom().nextInt(potentialTargets.size()));
        }
        return null;
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        net.minecraft.world.entity.player.Player player = event.getEntity();
        if (player != null) {
            if (player.hasEffect(ModEffects.ALL_STATS_DOWN.get())) {
                int amp = player.getEffect(ModEffects.ALL_STATS_DOWN.get()).getAmplifier();
                float reduction = 0.10F * (amp + 1); // 10%, 20%, 30%
                event.setNewSpeed(event.getNewSpeed() * (1.0F - reduction));
            }
            if (player.hasEffect(ModEffects.ALL_STATS_UP.get())) {
                int amp = player.getEffect(ModEffects.ALL_STATS_UP.get()).getAmplifier();
                float boost = 0.10F * (amp + 1); // +10%, +20%, +30%
                event.setNewSpeed(event.getNewSpeed() * (1.0F + boost));
            }
        }
    }
}
