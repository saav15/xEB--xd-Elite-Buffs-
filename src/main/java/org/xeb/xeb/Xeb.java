package org.xeb.xeb;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.xeb.xeb.attribute.ModAttributes;
import org.xeb.xeb.buff.EliteBuffRegistry;
import org.xeb.xeb.buff.impl.*;
import org.xeb.xeb.compat.ModCompatManager;
import org.xeb.xeb.effect.ModEffects;
import org.xeb.xeb.entity.ModEntities;
import org.xeb.xeb.entity.EliteFlyEntity;
import org.xeb.xeb.entity.EliteFlyModel;
import org.xeb.xeb.network.XEBNetwork;
import org.xeb.xeb.render.*;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(Xeb.MODID)
public class Xeb {
    public static final String MODID = "xeb";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Xeb() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register custom systems
        ModAttributes.register(modEventBus);
        ModEffects.register(modEventBus);
        ModEntities.register(modEventBus);

        // Register lifecycle listeners
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerEntityAttributes);
        modEventBus.addListener(this::onAttributeModification);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register network channel
        XEBNetwork.register();

        // Register all 28 buffs
        registerAllBuffs();

        LOGGER.info("xEB (xd Elite Buffs) loaded!");
    }

    private void registerAllBuffs() {
        EliteBuffRegistry.register(new SpikyBuff());
        EliteBuffRegistry.register(new ReactiveBuff());
        EliteBuffRegistry.register(new DamagingBuff());
        EliteBuffRegistry.register(new ToughBuff());
        EliteBuffRegistry.register(new ShieldedBuff());
        EliteBuffRegistry.register(new ProtectedBuff());
        EliteBuffRegistry.register(new SpeedyBuff());
        EliteBuffRegistry.register(new FlamingBuff());
        EliteBuffRegistry.register(new CreepyBuff());
        EliteBuffRegistry.register(new LuckyBuff());
        EliteBuffRegistry.register(new StaticBuff());
        EliteBuffRegistry.register(new BouncyBuff());
        EliteBuffRegistry.register(new MirrorBuff());
        EliteBuffRegistry.register(new ResonantBuff());
        EliteBuffRegistry.register(new UndyingBuff());
        EliteBuffRegistry.register(new HealthyBuff());
        EliteBuffRegistry.register(new SandyBuff());
        EliteBuffRegistry.register(new InfestedBuff());
        EliteBuffRegistry.register(new AbsorbentBuff());
        EliteBuffRegistry.register(new DepressingBuff());
        EliteBuffRegistry.register(new SlightlyDepressingBuff());
        EliteBuffRegistry.register(new EvolvingBuff());
        EliteBuffRegistry.register(new PlowBuff());
        EliteBuffRegistry.register(new MegaBuff());
        EliteBuffRegistry.register(new MadBuff());
        EliteBuffRegistry.register(new TwinBuff());
        EliteBuffRegistry.register(new HardyBuff());
        EliteBuffRegistry.register(new StickyBuff());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Initialize cross-mod compatibility hooks
            ModCompatManager.init();
        });
    }

    @SubscribeEvent
    public void onRegisterCommands(net.minecraftforge.event.RegisterCommandsEvent event) {
        org.xeb.xeb.command.XebCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ELITE_FLY.get(), EliteFlyEntity.createAttributes().build());
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onAttributeModification(EntityAttributeModificationEvent event) {
        for (EntityType<?> entityType : event.getTypes()) {
            EntityType<? extends LivingEntity> livingType = (EntityType<? extends LivingEntity>) entityType;
            if (event.has(livingType, Attributes.MAX_HEALTH)) {
                event.add(livingType, ModAttributes.MANA.get(), 20.0D);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("xEB Client Setup Complete.");
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            // Register client entity renderer for EliteFly using software.bernie.geckolib.renderer.GeoEntityRenderer
            event.registerEntityRenderer(ModEntities.ELITE_FLY.get(),
                    context -> new software.bernie.geckolib.renderer.GeoEntityRenderer<>(context, new EliteFlyModel()));
        }

        @SuppressWarnings("unchecked")
        @SubscribeEvent
        public static void addLayers(EntityRenderersEvent.AddLayers event) {
            // Add layers to player skins
            for (String skin : event.getSkins()) {
                LivingEntityRenderer renderer = event.getSkin(skin);
                if (renderer != null) {
                    renderer.addLayer(new MedallionRenderLayer<>(renderer));
                    renderer.addLayer(new MobColorOverlay<>(renderer));
                    renderer.addLayer(new GlowEyeOverlay<>(renderer));
                }
            }

            // Add layers to all registered living entity renderers
            for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES) {
                try {
                    EntityType<? extends LivingEntity> livingType = (EntityType<? extends LivingEntity>) type;
                    EntityRenderer<?> renderer = event.getRenderer(livingType);
                    if (renderer instanceof LivingEntityRenderer livingRenderer) {
                        livingRenderer.addLayer(new MedallionRenderLayer<>(livingRenderer));
                        livingRenderer.addLayer(new MobColorOverlay<>(livingRenderer));
                        livingRenderer.addLayer(new GlowEyeOverlay<>(livingRenderer));
                    }
                    if (renderer instanceof software.bernie.geckolib.renderer.GeoEntityRenderer geoRenderer) {
                        geoRenderer.addRenderLayer(new MedallionGeoLayer(geoRenderer));
                        geoRenderer.addRenderLayer(new MobColorGeoLayer(geoRenderer));
                        geoRenderer.addRenderLayer(new GlowEyeGeoLayer(geoRenderer));
                    }
                } catch (Exception e) {
                    // Ignore non-living entity types
                }
            }
        }
    }
}
