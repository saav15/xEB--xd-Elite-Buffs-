package org.xeb.xeb.event;

import org.xeb.xeb.Xeb;
import org.xeb.xeb.effect.ModEffects;
import org.xeb.xeb.mana.ManaManager;
import org.xeb.xeb.medallion.MedallionData;
import org.xeb.xeb.medallion.MedallionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Xeb.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BuffTickHandler {

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!org.xeb.xeb.Config.enabled) return;
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        ServerLevel level = (ServerLevel) entity.level();
        List<MedallionData> medallions = MedallionManager.getMedallions(entity);

        // Run per-tick buff logic
        if (!medallions.isEmpty()) {
            for (MedallionData m : medallions) {
                m.getBuff().onServerTick(entity, level);
            }
        }

        // Handle custom mana regeneration (1 mana per second)
        if (entity.tickCount % 20 == 0) {
            ManaManager.regenMana(entity, 1.0D);
        }

        // Restore NoAI state for petrified entities once effect expires
        if (entity instanceof Mob mob) {
            if (mob.getPersistentData().getBoolean("xebRestoreAI") && !mob.hasEffect(ModEffects.PETRIFY.get())) {
                mob.getPersistentData().remove("xebRestoreAI");
                mob.setNoAi(false);
            }
        }

        // Player-specific Holy Shield (holy mantle) regeneration
        if (entity instanceof net.minecraft.world.entity.player.Player player) {
            net.minecraft.nbt.CompoundTag tag = player.getPersistentData();
            if (player.hasEffect(ModEffects.HOLY_SHIELD.get()) && !tag.contains("xebHolyShield")) {
                if (tag.contains("xebPlayerHolyShieldTimer")) {
                    int timer = tag.getInt("xebPlayerHolyShieldTimer");
                    if (timer > 0) {
                        tag.putInt("xebPlayerHolyShieldTimer", timer - 1);
                    } else {
                        tag.remove("xebPlayerHolyShieldTimer");
                        tag.putBoolean("xebPlayerHolyShieldActive", true);
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE, net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.5F);
                    }
                } else if (!tag.contains("xebPlayerHolyShieldActive")) {
                    tag.putBoolean("xebPlayerHolyShieldActive", true);
                }
            } else {
                if (tag.contains("xebPlayerHolyShieldActive") || tag.contains("xebPlayerHolyShieldTimer")) {
                    tag.remove("xebPlayerHolyShieldActive");
                    tag.remove("xebPlayerHolyShieldTimer");
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemUseFinish(net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Finish event) {
        if (!org.xeb.xeb.Config.enabled) return;
        LivingEntity user = event.getEntity();
        if (user == null || user.level().isClientSide()) return;

        double searchRadius = 8.0D;
        net.minecraft.world.phys.AABB aabb = user.getBoundingBox().inflate(searchRadius);
        List<LivingEntity> nearby = user.level().getEntitiesOfClass(LivingEntity.class, aabb);

        for (LivingEntity nearbyEntity : nearby) {
            List<MedallionData> medallions = MedallionManager.getMedallions(nearbyEntity);
            for (MedallionData m : medallions) {
                if (m.getBuff() instanceof org.xeb.xeb.buff.impl.ResonantBuff resonantBuff) {
                    resonantBuff.handleNearbyItemUse(nearbyEntity, event);
                }
            }
        }
    }
}
