package org.xeb.xeb.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

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
            distributeToClient(msg);
        });
        ctx.setPacketHandled(true);
    }

    private static void distributeToClient(BuffParticlePacket msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Vec3 pos = new Vec3(msg.x, msg.y, msg.z);
            for (int i = 0; i < msg.count; i++) {
                double ox = (mc.level.random.nextDouble() - 0.5) * 0.5;
                double oy = mc.level.random.nextDouble() * 0.5;
                double oz = (mc.level.random.nextDouble() - 0.5) * 0.5;
                
                switch (msg.particleName) {
                    case "sonic_boom" -> mc.level.addParticle(ParticleTypes.SONIC_BOOM, msg.x, msg.y + 1.0, msg.z, 0, 0, 0);
                    case "flame" -> mc.level.addParticle(ParticleTypes.FLAME, msg.x + ox, msg.y + oy, msg.z + oz, 0, 0.05, 0);
                    case "creepy" -> mc.level.addParticle(ParticleTypes.HAPPY_VILLAGER, msg.x + ox, msg.y + oy, msg.z + oz, 0, 0.02, 0);
                    case "dodge" -> mc.level.addParticle(ParticleTypes.POOF, msg.x + ox, msg.y + oy + 0.5, msg.z + oz, 0, 0, 0);
                    case "crit" -> mc.level.addParticle(ParticleTypes.CRIT, msg.x + ox, msg.y + oy + 0.5, msg.z + oz, 0, 0.1, 0);
                    case "revival" -> mc.level.addParticle(ParticleTypes.TOTEM_OF_UNDYING, msg.x + ox, msg.y + oy + 1.0, msg.z + oz, 0, 0.2, 0);
                    case "sandstorm" -> mc.level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, msg.x + ox * 4, msg.y + oy, msg.z + oz * 4, 0, 0.02, 0);
                    case "evolve" -> mc.level.addParticle(ParticleTypes.END_ROD, msg.x + ox, msg.y + oy + 1.0, msg.z + oz, 0, 0.05, 0);
                    case "mega" -> mc.level.addParticle(ParticleTypes.DRAGON_BREATH, msg.x + ox, msg.y + oy + 0.5, msg.z + oz, 0, 0.02, 0);
                    case "static" -> mc.level.addParticle(ParticleTypes.ELECTRIC_SPARK, msg.x + ox, msg.y + oy + 0.5, msg.z + oz, 0, 0, 0);
                    case "tarred" -> mc.level.addParticle(ParticleTypes.SQUID_INK, msg.x + ox, msg.y + oy + 0.5, msg.z + oz, 0, 0, 0);
                    default -> mc.level.addParticle(ParticleTypes.PORTAL, msg.x + ox, msg.y + oy, msg.z + oz, 0, 0, 0);
                }
            }
        }
    }
}
