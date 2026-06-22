package org.xeb.xeb.event;

import org.xeb.xeb.Xeb;
import org.xeb.xeb.compat.ModCompatManager;
import org.xeb.xeb.medallion.MedallionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Xeb.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MedallionSpawnHandler {

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            if (event.getEntity() instanceof LivingEntity living) {
                net.minecraft.nbt.ListTag pending = org.xeb.xeb.network.MedallionSyncPacket.getPendingSync(living.getId());
                if (pending != null) {
                    living.getPersistentData().put(org.xeb.xeb.medallion.MedallionManager.MEDALLIONS_KEY, pending);
                }
            }
            return;
        }
        
        if (event.getEntity() instanceof LivingEntity living && !(living instanceof Player)) {
            // Check if eligible
            if (ModCompatManager.isEligible(living)) {
                MedallionManager.assignRandomMedallions(living, (ServerLevel) event.getLevel());
            }
        }
    }

    @SubscribeEvent
    public static void onStartTracking(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            if (event.getTarget() instanceof LivingEntity target) {
                MedallionManager.syncToPlayer(target, serverPlayer);
            }
        }
    }
}
