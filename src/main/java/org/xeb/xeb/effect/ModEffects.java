package org.xeb.xeb.effect;

import org.xeb.xeb.Xeb;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Xeb.MODID);

    public static final RegistryObject<MobEffect> HOLY_SHIELD = EFFECTS.register("holy_shield", HolyShieldEffect::new);
    public static final RegistryObject<MobEffect> PETRIFY = EFFECTS.register("petrify", PetrifyEffect::new);
    public static final RegistryObject<MobEffect> TARRED = EFFECTS.register("tarred", TarredEffect::new);
    public static final RegistryObject<MobEffect> SANDSTORM = EFFECTS.register("sandstorm", SandstormEffect::new);
    public static final RegistryObject<MobEffect> MANA_DRAIN = EFFECTS.register("mana_drain", ManaDrainEffect::new);
    public static final RegistryObject<MobEffect> BURN = EFFECTS.register("burn", BurnEffect::new);
    public static final RegistryObject<MobEffect> MADNESS = EFFECTS.register("madness", MadnessEffect::new);
    public static final RegistryObject<MobEffect> ALL_STATS_DOWN = EFFECTS.register("all_stats_down", AllStatsDownEffect::new);
    public static final RegistryObject<MobEffect> ALL_STATS_UP = EFFECTS.register("all_stats_up", AllStatsUpEffect::new);

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
