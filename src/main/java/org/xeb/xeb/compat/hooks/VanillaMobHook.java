package org.xeb.xeb.compat.hooks;

import org.xeb.xeb.compat.CompatHook;
import org.xeb.xeb.compat.ModCompatManager;
import net.minecraft.world.entity.EntityType;

public class VanillaMobHook implements CompatHook {
    @Override
    public void registerTypes() {
        // Vanilla Bosses
        ModCompatManager.registerBoss(EntityType.WITHER);
        ModCompatManager.registerBoss(EntityType.ENDER_DRAGON);
        ModCompatManager.registerBoss(EntityType.WARDEN);
        ModCompatManager.registerBoss(EntityType.ELDER_GUARDIAN);
        ModCompatManager.registerBoss(EntityType.EVOKER);

        // Vanilla Common Hostile Mobs
        ModCompatManager.registerEligible(EntityType.ZOMBIE);
        ModCompatManager.registerEligible(EntityType.SKELETON);
        ModCompatManager.registerEligible(EntityType.CREEPER);
        ModCompatManager.registerEligible(EntityType.SPIDER);
        ModCompatManager.registerEligible(EntityType.CAVE_SPIDER);
        ModCompatManager.registerEligible(EntityType.ENDERMAN);
        ModCompatManager.registerEligible(EntityType.WITCH);
        ModCompatManager.registerEligible(EntityType.BLAZE);
        ModCompatManager.registerEligible(EntityType.GUARDIAN);
        ModCompatManager.registerEligible(EntityType.PIGLIN);
        ModCompatManager.registerEligible(EntityType.PIGLIN_BRUTE);
        ModCompatManager.registerEligible(EntityType.HOGLIN);
        ModCompatManager.registerEligible(EntityType.ZOGLIN);
        ModCompatManager.registerEligible(EntityType.ZOMBIFIED_PIGLIN);
        ModCompatManager.registerEligible(EntityType.DROWNED);
        ModCompatManager.registerEligible(EntityType.HUSK);
        ModCompatManager.registerEligible(EntityType.STRAY);
        ModCompatManager.registerEligible(EntityType.WITHER_SKELETON);
        ModCompatManager.registerEligible(EntityType.VINDICATOR);
        ModCompatManager.registerEligible(EntityType.PILLAGER);
        ModCompatManager.registerEligible(EntityType.RAVAGER);
        ModCompatManager.registerEligible(EntityType.GHAST);
        ModCompatManager.registerEligible(EntityType.MAGMA_CUBE);
        ModCompatManager.registerEligible(EntityType.SLIME);
        ModCompatManager.registerEligible(EntityType.SHULKER);
        ModCompatManager.registerEligible(EntityType.PHANTOM);
    }
}
