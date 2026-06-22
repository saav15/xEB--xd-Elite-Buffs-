package org.xeb.xeb.render;

import org.xeb.xeb.network.BuffParticlePacket;
import org.xeb.xeb.network.XEBNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;

public class BuffParticleHandler {
    public static void spawnParticles(LivingEntity entity, String particleName, int count) {
        if (entity.level().isClientSide()) {
            // Client side direct spawn if needed, but usually packets handle this.
        } else {
            // Server side: send packet to all tracking clients
            BuffParticlePacket packet = new BuffParticlePacket(entity.getX(), entity.getY(), entity.getZ(), particleName, count);
            XEBNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
        }
    }
}
