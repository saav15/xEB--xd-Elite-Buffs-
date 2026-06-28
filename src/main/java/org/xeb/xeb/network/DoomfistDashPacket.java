package org.xeb.xeb.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.api.distmarker.Dist;

import java.util.function.Supplier;

public class DoomfistDashPacket {
    private final int entityId;
    private final boolean dashing;
    private final float chargeRatio;

    public DoomfistDashPacket(int entityId, boolean dashing, float chargeRatio) {
        this.entityId = entityId;
        this.dashing = dashing;
        this.chargeRatio = chargeRatio;
    }

    public int getEntityId() {
        return entityId;
    }

    public boolean isDashing() {
        return dashing;
    }

    public float getChargeRatio() {
        return chargeRatio;
    }

    public static void encode(DoomfistDashPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeBoolean(msg.dashing);
        buf.writeFloat(msg.chargeRatio);
    }

    public static DoomfistDashPacket decode(FriendlyByteBuf buf) {
        return new DoomfistDashPacket(buf.readInt(), buf.readBoolean(), buf.readFloat());
    }

    public static void handle(DoomfistDashPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleDoomfistDash(msg));
        });
        ctx.setPacketHandled(true);
    }
}
