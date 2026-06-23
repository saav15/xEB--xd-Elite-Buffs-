package org.xeb.xeb.boss;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import org.xeb.xeb.Config;
import org.xeb.xeb.compat.ModCompatManager;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Monitors boss entities under Madness, identifying if their AI freezes,
 * and dynamically injecting override movement goals to recover them.
 */
public class FrozenBossRecoverySystem {
    private static final Map<Integer, FrozenTracker> MONITOR_MAP = new ConcurrentHashMap<>();

    public static void registerDamageDealt(LivingEntity boss) {
        if (boss == null) return;
        FrozenTracker tracker = MONITOR_MAP.get(boss.getId());
        if (tracker != null) {
            tracker.ticksSinceLastDamage = 0;
        }
    }

    public static boolean isForcedMovementActive(Mob mob) {
        FrozenTracker tracker = MONITOR_MAP.get(mob.getId());
        return tracker != null && tracker.forcedTicksRemaining > 0;
    }

    public static void tick(Mob mob) {
        if (mob.level().isClientSide()) return;

        int entityId = mob.getId();
        LivingEntity target = mob.getTarget();

        if (target == null) {
            MONITOR_MAP.remove(entityId);
            return;
        }

        // Initialize tracker
        FrozenTracker tracker = MONITOR_MAP.computeIfAbsent(entityId, id -> new FrozenTracker(target.getId()));

        // Update target tracking
        if (target.getId() != tracker.lastTargetId) {
            tracker.lastTargetId = target.getId();
            tracker.targetDurationTicks = 0;
        } else {
            tracker.targetDurationTicks++;
        }

        tracker.ticksSinceLastDamage++;

        // If forced movement is already running
        if (tracker.forcedTicksRemaining > 0) {
            tracker.forcedTicksRemaining--;
            if (tracker.forcedTicksRemaining <= 0) {
                // Remove override goal
                if (tracker.recoveryGoal != null) {
                    mob.goalSelector.removeGoal(tracker.recoveryGoal);
                    tracker.recoveryGoal = null;
                }
                // Exclude target and force selection of a new target
                TargetRejectionBuffer.addRejectedTarget(mob, target.getId());
                mob.setTarget(null);
            }
            return;
        }

        // Detect if frozen: target held for > timeout (default 60), no damage dealt, movement = 0, not in phase transition
        boolean sameTargetLongTime = tracker.targetDurationTicks > Config.frozenDetectionTimeoutTicks;
        boolean noDamageDealt = tracker.ticksSinceLastDamage > Config.frozenDetectionTimeoutTicks;
        boolean movementZero = mob.getDeltaMovement().lengthSqr() < 0.001;

        boolean inPhaseTransition = isInvulnerableOrTransitioning(mob);

        if (sameTargetLongTime && noDamageDealt && movementZero && !inPhaseTransition) {
            // Mark as frozen, trigger target candidate expansion if needed, and start forced movement
            if (tracker.recoveryGoal == null) {
                // If candidate pool was empty, try temporary target expansion
                if (!hasAnyValidTargets(mob)) {
                    BossTargetCandidateExpander.startExpansion(mob);
                }

                tracker.forcedTicksRemaining = Config.forcedMovementDurationTicks; // default 30
                tracker.recoveryGoal = new MadnessFrozenRecoveryGoal(mob);
                mob.goalSelector.addGoal(0, tracker.recoveryGoal);
            }
        }
    }

    private static boolean hasAnyValidTargets(Mob mob) {
        // Simple search to see if any valid targets exist in standard range
        double range = 16.0D;
        net.minecraft.world.phys.AABB searchBox = mob.getBoundingBox().inflate(range);
        var list = mob.level().getEntitiesOfClass(LivingEntity.class, searchBox,
            t -> t != mob && t.isAlive() && mob.hasLineOfSight(t) &&
                 !(t instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator()))
        );
        return !list.isEmpty();
    }

    private static boolean isInvulnerableOrTransitioning(Mob mob) {
        // 1. Invulnerability check
        if (mob.isInvulnerable()) return true;

        // 2. Mod compat check (e.g. Cataclysm adapters)
        if (ModCompatManager.isBossInTransition(mob)) {
            return true;
        }

        // 3. Heuristic NBT checks for "phase" or similar fields
        net.minecraft.nbt.CompoundTag tag = mob.getPersistentData();
        if (tag.contains("phase") || tag.contains("transition") || tag.contains("invulnerable")) {
            return true;
        }

        return false;
    }

    private static class FrozenTracker {
        int lastTargetId;
        int targetDurationTicks = 0;
        int ticksSinceLastDamage = 0;
        int forcedTicksRemaining = 0;
        MadnessFrozenRecoveryGoal recoveryGoal = null;

        FrozenTracker(int lastTargetId) {
            this.lastTargetId = lastTargetId;
        }
    }

    /**
     * Goal that executes high priority movement and look controls when the boss AI freezes.
     */
    private static class MadnessFrozenRecoveryGoal extends Goal {
        private final Mob mob;
        private int ticksActive = 0;

        MadnessFrozenRecoveryGoal(Mob mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            return FrozenBossRecoverySystem.isForcedMovementActive(mob);
        }

        @Override
        public void start() {
            ticksActive = 0;
        }

        @Override
        public void tick() {
            ticksActive++;
            LivingEntity target = mob.getTarget();
            if (target != null) {
                mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
                mob.getNavigation().moveTo(target, 1.25D);
                mob.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.25D);

                double distSq = mob.distanceToSqr(target);
                double reachSq = mob.getMeleeAttackRangeSqr(target);
                if (distSq <= reachSq) {
                    mob.swing(InteractionHand.MAIN_HAND);
                    if (ticksActive % 10 == 0) {
                        mob.doHurtTarget(target);
                    }
                } else {
                    // Fallback to deal damage directly if forced to melee attack but unable to resolve
                    if (ticksActive >= 25 && ticksActive % 10 == 0 && Config.forceAttackOnFrozenBoss) {
                        double dmg = mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
                        if (dmg <= 0) dmg = 5.0;
                        target.hurt(mob.damageSources().mobAttack(mob), (float) dmg);
                    }
                }
            }
        }
    }
}
