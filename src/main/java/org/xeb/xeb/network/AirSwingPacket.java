package org.xeb.xeb.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import org.xeb.xeb.effect.ModEffects;

import java.util.function.Supplier;

public class AirSwingPacket {

    public AirSwingPacket() {}

    public static void encode(AirSwingPacket msg, FriendlyByteBuf buf) {
        // No data needed
    }

    public static AirSwingPacket decode(FriendlyByteBuf buf) {
        return new AirSwingPacket();
    }

    public static void handle(AirSwingPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null && player.hasEffect(ModEffects.CHARGED_FIST.get())) {
                net.minecraft.world.effect.MobEffectInstance cfEffect =
                        player.getEffect(ModEffects.CHARGED_FIST.get());
                if (cfEffect != null) {
                    long currentTick = player.level().getGameTime();
                    long lastTrigger = player.getPersistentData().getLong("xebChargedFistLastTrigger");
                    int amplifier = cfEffect.getAmplifier();
                    int cooldown = Math.max(2, 16 - 4 * amplifier);
                    if (currentTick - lastTrigger >= cooldown) {
                        player.getPersistentData().putLong("xebChargedFistLastTrigger", currentTick);

                        double baseDamage = 1.0D;
                        if (player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) != null) {
                            baseDamage = player.getAttributeValue(
                                    net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                        }
                        double projectileDamage = baseDamage * (0.10D + 0.15D * amplifier);
                        
                        // Auto-target nearest enemy
                        LivingEntity target = org.xeb.xeb.event.BuffDamageHandler.findNearestEnemy(player, 16.0D);
                        int count = 2 + player.getRandom().nextInt(2); // 2 or 3 projectiles
                        net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                        net.minecraft.world.phys.Vec3 eyePos = player.getEyePosition(1.0F);

                        for (int i = 0; i < count; i++) {
                            org.xeb.xeb.entity.SparkleEntity sparkle = new org.xeb.xeb.entity.SparkleEntity(
                                    player.level(), player, projectileDamage, target);
                            sparkle.moveTo(eyePos.x, eyePos.y - 0.1D, eyePos.z, 0.0F, 0.0F);
                            
                            double spread = 0.12D;
                            double rx = look.x + (player.getRandom().nextDouble() - 0.5D) * spread;
                            double ry = look.y + (player.getRandom().nextDouble() - 0.5D) * spread;
                            double rz = look.z + (player.getRandom().nextDouble() - 0.5D) * spread;
                            net.minecraft.world.phys.Vec3 initialVel = new net.minecraft.world.phys.Vec3(rx, ry, rz).normalize().scale(0.65D);
                            sparkle.setDeltaMovement(initialVel);
                            
                            player.level().addFreshEntity(sparkle);
                        }
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                                net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_STEP,
                                net.minecraft.sounds.SoundSource.PLAYERS,
                                0.8F, 1.2F + player.getRandom().nextFloat() * 0.6F);
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
