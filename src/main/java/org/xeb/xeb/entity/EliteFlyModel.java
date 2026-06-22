package org.xeb.xeb.entity;

import org.xeb.xeb.Xeb;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EliteFlyModel extends GeoModel<EliteFlyEntity> {
    @Override
    public ResourceLocation getModelResource(EliteFlyEntity animatable) {
        return new ResourceLocation(Xeb.MODID, "geo/elite_fly.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EliteFlyEntity animatable) {
        String hostType = animatable.getHostType();
        if (hostType != null && !hostType.isEmpty() && !hostType.equals("default")) {
            // 1. Try exact match
            ResourceLocation customLoc = new ResourceLocation(Xeb.MODID, "textures/entity/elite_fly_" + hostType + ".png");
            if (net.minecraft.client.Minecraft.getInstance().getResourceManager().getResource(customLoc).isPresent()) {
                return customLoc;
            }
            
            // 2. Try variant/family fallbacks
            String fallback = null;
            String lower = hostType.toLowerCase();
            if (lower.contains("zombie") || lower.contains("husk") || lower.contains("drowned") || lower.contains("piglin")) {
                fallback = "zombie";
            } else if (lower.contains("skeleton") || lower.contains("stray") || lower.contains("wither")) {
                fallback = "skeleton";
            } else if (lower.contains("spider")) {
                fallback = "spider";
            } else if (lower.contains("creeper")) {
                fallback = "creeper";
            }
            
            if (fallback != null) {
                ResourceLocation fallbackLoc = new ResourceLocation(Xeb.MODID, "textures/entity/elite_fly_" + fallback + ".png");
                if (net.minecraft.client.Minecraft.getInstance().getResourceManager().getResource(fallbackLoc).isPresent()) {
                    return fallbackLoc;
                }
            }
        }
        return new ResourceLocation(Xeb.MODID, "textures/entity/elite_fly.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EliteFlyEntity animatable) {
        return new ResourceLocation(Xeb.MODID, "animations/elite_fly.animation.json");
    }
}
