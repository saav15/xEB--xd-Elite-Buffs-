package org.xeb.xeb.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.particles.ParticleTypes;
import org.xeb.xeb.buff.EliteBuff;
import org.xeb.xeb.buff.EliteBuffRegistry;
import org.xeb.xeb.medallion.MedallionData;
import org.xeb.xeb.medallion.MedallionManager;
import org.xeb.xeb.medallion.MedallionType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientPacketHandler {
    private static final java.util.Map<Integer, ListTag> PENDING_SYNCS = new ConcurrentHashMap<>();

    public static ListTag getPendingSync(int entityId) {
        return PENDING_SYNCS.remove(entityId);
    }

    public static void addPendingSync(int entityId, ListTag tag) {
        PENDING_SYNCS.put(entityId, tag);
    }

    public static void handleMedallionSync(MedallionSyncPacket msg) {
        Entity entity = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getEntity(msg.getEntityId()) : null;
        List<MedallionData> list = new ArrayList<>();
        for (int i = 0; i < msg.getBuffIds().size(); i++) {
            String id = msg.getBuffIds().get(i);
            String tierName = msg.getTiers().get(i);
            EliteBuff buff = EliteBuffRegistry.getById(id);
            if (buff != null) {
                try {
                    MedallionType tier = MedallionType.valueOf(tierName);
                    list.add(new MedallionData(buff, tier, UUID.randomUUID()));
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
        }
        
        ListTag listTag = new ListTag();
        for (MedallionData m : list) {
            CompoundTag entry = new CompoundTag();
            entry.putString("BuffId", m.getBuff().getId());
            entry.putString("Tier", m.getTier().name());
            entry.putUUID("UUID", m.getUniqueId());
            listTag.add(entry);
        }

        if (entity instanceof LivingEntity living) {
            living.getPersistentData().put(MedallionManager.MEDALLIONS_KEY, listTag);
            try {
                living.refreshDimensions();
            } catch (Exception ignored) {}
        } else {
            addPendingSync(msg.getEntityId(), listTag);
        }
    }

    public static void handleBuffParticle(BuffParticlePacket msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            for (int i = 0; i < msg.getCount(); i++) {
                double ox = (mc.level.random.nextDouble() - 0.5) * 0.5;
                double oy = mc.level.random.nextDouble() * 0.5;
                double oz = (mc.level.random.nextDouble() - 0.5) * 0.5;
                
                switch (msg.getParticleName()) {
                    case "sonic_boom" -> mc.level.addParticle(ParticleTypes.SONIC_BOOM, msg.getX(), msg.getY() + 1.0, msg.getZ(), 0, 0, 0);
                    case "flame" -> mc.level.addParticle(ParticleTypes.FLAME, msg.getX() + ox, msg.getY() + oy, msg.getZ() + oz, 0, 0.05, 0);
                    case "creepy" -> mc.level.addParticle(ParticleTypes.HAPPY_VILLAGER, msg.getX() + ox, msg.getY() + oy, msg.getZ() + oz, 0, 0.02, 0);
                    case "dodge" -> mc.level.addParticle(ParticleTypes.POOF, msg.getX() + ox, msg.getY() + oy + 0.5, msg.getZ() + oz, 0, 0, 0);
                    case "crit" -> mc.level.addParticle(ParticleTypes.CRIT, msg.getX() + ox, msg.getY() + oy + 0.5, msg.getZ() + oz, 0, 0.1, 0);
                    case "revival" -> mc.level.addParticle(ParticleTypes.TOTEM_OF_UNDYING, msg.getX() + ox, msg.getY() + oy + 1.0, msg.getZ() + oz, 0, 0.2, 0);
                    case "sandstorm" -> mc.level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, msg.getX() + ox * 4, msg.getY() + oy, msg.getZ() + oz * 4, 0, 0.02, 0);
                    case "evolve" -> mc.level.addParticle(ParticleTypes.END_ROD, msg.getX() + ox, msg.getY() + oy + 1.0, msg.getZ() + oz, 0, 0.05, 0);
                    case "mega" -> mc.level.addParticle(ParticleTypes.DRAGON_BREATH, msg.getX() + ox, msg.getY() + oy + 0.5, msg.getZ() + oz, 0, 0.02, 0);
                    case "static" -> mc.level.addParticle(ParticleTypes.ELECTRIC_SPARK, msg.getX() + ox, msg.getY() + oy + 0.5, msg.getZ() + oz, 0, 0, 0);
                    case "tarred" -> mc.level.addParticle(ParticleTypes.SQUID_INK, msg.getX() + ox, msg.getY() + oy + 0.5, msg.getZ() + oz, 0, 0, 0);
                    default -> mc.level.addParticle(ParticleTypes.PORTAL, msg.getX() + ox, msg.getY() + oy, msg.getZ() + oz, 0, 0, 0);
                }
            }
        }
    }
}
