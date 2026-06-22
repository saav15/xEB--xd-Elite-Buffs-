package org.xeb.xeb.entity;

import org.xeb.xeb.Xeb;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Xeb.MODID);

    public static final RegistryObject<EntityType<EliteFlyEntity>> ELITE_FLY = ENTITY_TYPES.register("elite_fly",
            () -> EntityType.Builder.of(EliteFlyEntity::new, MobCategory.MONSTER)
                    .sized(0.3F, 0.3F)
                    .clientTrackingRange(8)
                    .build("elite_fly")
    );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
