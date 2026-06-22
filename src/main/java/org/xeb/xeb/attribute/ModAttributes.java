package org.xeb.xeb.attribute;

import org.xeb.xeb.Xeb;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Xeb.MODID);

    public static final RegistryObject<Attribute> MANA = ATTRIBUTES.register("mana", 
        () -> new RangedAttribute("attribute.name.xeb.mana", 20.0, 0.0, 1000.0).setSyncable(true)
    );

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }
}
