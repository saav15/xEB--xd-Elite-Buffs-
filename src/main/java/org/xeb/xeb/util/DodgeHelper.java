package org.xeb.xeb.util;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import org.xeb.xeb.network.BuffParticlePacket;
import org.xeb.xeb.network.XEBNetwork;
import net.minecraftforge.network.PacketDistributor;

/**
 * Shared dodge logic used by Lucky and Adrenaline effects.
 * Fully cancels a hurt event (or projectile impact) and plays a
 * subtle wave-ring particle + dodge sound on the dodging entity.
 */
public class DodgeHelper {

    /**
     * Cancels a LivingHurtEvent as a "dodge": zeroes damage, marks the event
     * cancelled, spawns dodge_wave particles and plays a dodge sound.
     */
    public static void triggerDodge(LivingEntity entity, LivingHurtEvent event) {
        event.setAmount(0.0F);
        event.setCanceled(true);
        sendDodgeEffects(entity);
    }

    /**
     * Cancels a ProjectileImpactEvent as a "dodge": cancels the impact
     * so no damage or secondary effects are applied, then plays effects.
     */
    public static void triggerDodge(LivingEntity entity, ProjectileImpactEvent event) {
        event.setCanceled(true);
        sendDodgeEffects(entity);
    }

    private static void sendDodgeEffects(LivingEntity entity) {
        if (entity.level().isClientSide()) return;

        // Subtle wave-ring particles
        BuffParticlePacket wavePacket = new BuffParticlePacket(
                entity.getX(), entity.getY(), entity.getZ(), "dodge_wave", 12);
        XEBNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), wavePacket);

        // Poof particles (original dodge visual)
        BuffParticlePacket poofPacket = new BuffParticlePacket(
                entity.getX(), entity.getY(), entity.getZ(), "dodge", 5);
        XEBNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), poofPacket);

        // Dodge sound: shield block pitched up for a quick "swish"
        entity.level().playSound(null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.SHIELD_BLOCK,
                SoundSource.PLAYERS,
                0.6F,
                1.8F + entity.getRandom().nextFloat() * 0.2F);
    }
}
