package org.xeb.xeb.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.api.distmarker.Dist;

import java.util.function.Supplier;

public class BuffParticlePacket {
    private final double x;
    private final double y;
    private final double z;
    private final String particleName;
    private final int count;

    public BuffParticlePacket(double x, double y, double z, String particleName, int count) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.particleName = particleName;
        this.count = count;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public String getParticleName() {
        return particleName;
    }

    public int getCount() {
        return count;
    }

    public static void encode(BuffParticlePacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeUtf(msg.particleName);
        buf.writeInt(msg.count);
    }

    public static BuffParticlePacket decode(FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        String particleName = buf.readUtf();
        int count = buf.readInt();
        return new BuffParticlePacket(x, y, z, particleName, count);
    }

    public static void handle(BuffParticlePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleBuffParticle(msg));
        });
        ctx.setPacketHandled(true);
    }
}
