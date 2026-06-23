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
        if (!org.xeb.xeb.Config.enabled) return;
        LivingEntity target = event.getEntity();
        if (target == null) return;

        Entity attacker = event.getSource().getEntity();

        // Kinetic Spikes logic
        if (!target.level().isClientSide() && target.hasEffect(ModEffects.KINETIC_SPIKES.get())) {
            net.minecraft.world.effect.MobEffectInstance effect = target.getEffect(ModEffects.KINETIC_SPIKES.get());
            if (effect != null) {
                int amplifier = effect.getAmplifier();
                double baseDamage = 1.0D;
                if (target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) != null) {
                    baseDamage = target.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                }
                double projectileDamage = baseDamage * (0.10D + 0.15D * amplifier);
                int count = 2 + target.getRandom().nextInt(2); // 2 or 3 Sparkles
                
                // Find nearest enemy using player-friendly logic
                LivingEntity projectileTarget = findNearestEnemy(target, 16.0D);
                
                for (int i = 0; i < count; i++) {
                    org.xeb.xeb.entity.SparkleEntity sparkle = new org.xeb.xeb.entity.SparkleEntity(
                            target.level(), target, projectileDamage, projectileTarget);
                    sparkle.moveTo(target.getX(), target.getY(0.5D), target.getZ(), 0.0F, 0.0F);
                    target.level().addFreshEntity(sparkle);

                    // Play Amethyst step sound with randomized pitch
                    target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_STEP,
                            net.minecraft.sounds.SoundSource.NEUTRAL,
                            1.0F,
                            0.5F + target.getRandom().nextFloat() * 1.0F);
                }
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
            // Register damage dealt to prevent frozen AI detection triggering
            if (livingAttacker instanceof net.minecraft.world.entity.Mob) {
                org.xeb.xeb.boss.FrozenBossRecoverySystem.registerDamageDealt(livingAttacker);
            }

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
            // If the entity is blacklisted, block Madness targeting completely
            if (org.xeb.xeb.boss.UniversalBossDetector.isBlacklisted(mob)) {
                return;
            }

            boolean isBoss = org.xeb.xeb.compat.ModCompatManager.isBoss(mob);
            LivingEntity newTargetChoice = event.getNewTarget();
            
            // If the boss naturally selected a valid target (Player, Boss, or Elite),
            // let it stand to avoid AI override freeze loops.
            if (isBoss && newTargetChoice != null) {
                boolean isValidTarget = newTargetChoice instanceof net.minecraft.world.entity.player.Player ||
                                       org.xeb.xeb.compat.ModCompatManager.isBoss(newTargetChoice) ||
                                       !org.xeb.xeb.medallion.MedallionManager.getMedallions(newTargetChoice).isEmpty();
                if (isValidTarget && !org.xeb.xeb.boss.TargetRejectionBuffer.isRejected(mob, newTargetChoice.getId())) {
                    mob.getPersistentData().putInt("xebMadnessTargetId", newTargetChoice.getId());
                    return;
                }
            }

            net.minecraft.world.effect.MobEffectInstance effect = mob.getEffect(ModEffects.MADNESS.get());
            int amplifier = effect != null ? effect.getAmplifier() : 0;
            double range = 16.0D + amplifier * 4.0D;

            net.minecraft.nbt.CompoundTag tag = mob.getPersistentData();
            LivingEntity target = null;

            if (tag.contains("xebMadnessTargetId")) {
                int targetId = tag.getInt("xebMadnessTargetId");
                // Check if target was recently rejected
                if (org.xeb.xeb.boss.TargetRejectionBuffer.isRejected(mob, targetId)) {
                    tag.remove("xebMadnessTargetId");
                } else {
                    Entity cached = mob.level().getEntity(targetId);
                    if (cached instanceof LivingEntity living && living.isAlive() && mob.distanceToSqr(living) <= range * range) {
                        boolean isValid = !(living instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator()))
                                && !org.xeb.xeb.boss.UniversalBossDetector.isBlacklisted(living);
                        if (isValid && isBoss && !org.xeb.xeb.boss.BossTargetCandidateExpander.shouldAttackAllMobs(mob)) {
                            isValid = living instanceof net.minecraft.world.entity.player.Player ||
                                      org.xeb.xeb.compat.ModCompatManager.isBoss(living) ||
                                      !org.xeb.xeb.medallion.MedallionManager.getMedallions(living).isEmpty();
                        }
                        if (isValid) {
                            target = living;
                        }
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
        boolean isBoss = org.xeb.xeb.compat.ModCompatManager.isBoss(mob);
        net.minecraft.world.phys.AABB searchBox = mob.getBoundingBox().inflate(range);
        List<LivingEntity> potentialTargets = mob.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                target -> {
                    if (target == mob || !target.isAlive() || !mob.hasLineOfSight(target)) return false;
                    if (target instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator())) return false;
                    if (org.xeb.xeb.boss.UniversalBossDetector.isBlacklisted(target)) return false;
                    if (org.xeb.xeb.boss.TargetRejectionBuffer.isRejected(mob, target.getId())) return false;

                    if (isBoss) {
                        if (org.xeb.xeb.boss.BossTargetCandidateExpander.shouldAttackAllMobs(mob)) {
                            return true;
                        }
                        return target instanceof net.minecraft.world.entity.player.Player ||
                               org.xeb.xeb.compat.ModCompatManager.isBoss(target) ||
                               !org.xeb.xeb.medallion.MedallionManager.getMedallions(target).isEmpty();
                    }
                    return true;
                });
        
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

    public static LivingEntity findNearestEnemy(LivingEntity owner, double range) {
        net.minecraft.world.phys.AABB box = owner.getBoundingBox().inflate(range);
        List<LivingEntity> list = owner.level().getEntitiesOfClass(LivingEntity.class, box,
            entity -> {
                if (entity == owner || !entity.isAlive()) return false;
                if (entity instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator())) return false;
                
                // Do not target allies
                if (owner.isAlliedTo(entity)) return false;
                
                // Do not target pets owned by the owner
                if (entity instanceof net.minecraft.world.entity.TamableAnimal tame && tame.isOwnedBy(owner)) return false;
                
                return true;
            });
        
        LivingEntity nearest = null;
        double minDist = Double.MAX_VALUE;
        
        // Prioritize monsters/bosses/players first if the owner is a player
        if (owner instanceof net.minecraft.world.entity.player.Player) {
            for (LivingEntity e : list) {
                boolean isHostile = e instanceof net.minecraft.world.entity.monster.Enemy ||
                                    e instanceof net.minecraft.world.entity.player.Player ||
                                    org.xeb.xeb.compat.ModCompatManager.isBoss(e);
                if (isHostile) {
                    double dist = owner.distanceToSqr(e);
                    if (dist < minDist) {
                        minDist = dist;
                        nearest = e;
                    }
                }
            }
        }
        
        // Fallback to any living entity if no primary hostile target is found
        if (nearest == null) {
            minDist = Double.MAX_VALUE;
            for (LivingEntity e : list) {
                double dist = owner.distanceToSqr(e);
                if (dist < minDist) {
                    minDist = dist;
                    nearest = e;
                }
            }
        }
        
        return nearest;
    }
}
