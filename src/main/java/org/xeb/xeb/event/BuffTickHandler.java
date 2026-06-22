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
    }
}
