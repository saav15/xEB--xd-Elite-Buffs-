package org.xeb.xeb.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.xeb.xeb.Xeb;
import org.xeb.xeb.item.ModItems;
import org.xeb.xeb.network.XEBNetwork;
import org.xeb.xeb.network.ActuarKeyPacket;

@Mod.EventBusSubscriber(modid = Xeb.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KeyInputHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null && mc.screen == null) {
            Player player = mc.player;
            // Check if player holds Doomfist in either hand
            boolean holdsDoomfist = player.getMainHandItem().is(ModItems.DOOMFIST.get())
                    || player.getOffhandItem().is(ModItems.DOOMFIST.get());

            if (holdsDoomfist) {
                if (ModKeyMappings.ACTUAR_1_KEY.consumeClick()) {
                    XEBNetwork.CHANNEL.sendToServer(new ActuarKeyPacket(1));
                }
                if (ModKeyMappings.ACTUAR_2_KEY.consumeClick()) {
                    XEBNetwork.CHANNEL.sendToServer(new ActuarKeyPacket(2));
                }
            }
        }
    }
}
