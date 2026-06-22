package org.xeb.xeb.buff.impl;

import org.xeb.xeb.buff.BuffType;
import org.xeb.xeb.buff.EliteBuff;
import org.xeb.xeb.network.BuffParticlePacket;
import org.xeb.xeb.network.XEBNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

public class ReactiveBuff extends EliteBuff {
    public ReactiveBuff() {
        super("reactive", "Reactive", BuffType.UNIVERSAL, 0xFF6347, 5.0D);
    }

    @Override
    public void onAttach(LivingEntity entity) {}

    @Override
    public void onDetach(LivingEntity entity) {}

    @Override
    public void onServerTick(LivingEntity entity, ServerLevel level) {}

    @Override
    public void onDamageTaken(LivingEntity entity, LivingHurtEvent event) {
        if (entity.level().isClientSide()) return;

        AABB aabb = entity.getBoundingBox().inflate(3.0D);
        List<LivingEntity> entities = entity.level().getEntitiesOfClass(LivingEntity.class, aabb);

        for (LivingEntity target : entities) {
            if (target != entity) {
                target.hurt(entity.damageSources().magic(), 2.0F);
            }
        }

        // Send particle packet
        BuffParticlePacket packet = new BuffParticlePacket(entity.getX(), entity.getY(), entity.getZ(), "sonic_boom", 1);
        XEBNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
    }
}
