package org.xeb.xeb.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.xeb.xeb.buff.EliteBuff;
import org.xeb.xeb.buff.EliteBuffRegistry;
import org.xeb.xeb.medallion.MedallionData;
import org.xeb.xeb.medallion.MedallionManager;
import org.xeb.xeb.medallion.MedallionType;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class MedallionSyncPacket {
    private final int entityId;
    private final List<String> buffIds;
    private final List<String> tiers;

    public MedallionSyncPacket(int entityId, List<String> buffIds, List<String> tiers) {
        this.entityId = entityId;
        this.buffIds = buffIds;
        this.tiers = tiers;
    }

    public static void encode(MedallionSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeInt(msg.buffIds.size());
        for (int i = 0; i < msg.buffIds.size(); i++) {
            buf.writeUtf(msg.buffIds.get(i));
            buf.writeUtf(msg.tiers.get(i));
        }
    }

    public static MedallionSyncPacket decode(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        int size = buf.readInt();
        List<String> buffIds = new ArrayList<>();
        List<String> tiers = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            buffIds.add(buf.readUtf());
            tiers.add(buf.readUtf());
        }
        return new MedallionSyncPacket(entityId, buffIds, tiers);
    }

    public static void handle(MedallionSyncPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            // Client side handling
            distributeToClient(msg);
        });
        ctx.setPacketHandled(true);
    }

    private static final java.util.Map<Integer, ListTag> PENDING_SYNCS = new java.util.concurrent.ConcurrentHashMap<>();

    public static ListTag getPendingSync(int entityId) {
        return PENDING_SYNCS.remove(entityId);
    }

    private static void distributeToClient(MedallionSyncPacket msg) {
        Entity entity = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getEntity(msg.entityId) : null;
        List<MedallionData> list = new ArrayList<>();
        for (int i = 0; i < msg.buffIds.size(); i++) {
            String id = msg.buffIds.get(i);
            String tierName = msg.tiers.get(i);
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
            PENDING_SYNCS.put(msg.entityId, listTag);
        }
    }
}
