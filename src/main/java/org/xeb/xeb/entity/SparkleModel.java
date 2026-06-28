package org.xeb.xeb.entity;

import net.minecraft.resources.ResourceLocation;
import org.xeb.xeb.Xeb;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model definition for the SparkleEntity arrowhead.
 * Points to sparkle_arrow.geo.json / sparkle_arrow.png with no animations.
 */
public class SparkleModel extends GeoModel<SparkleEntity> {

    private static final ResourceLocation MODEL =
            new ResourceLocation(Xeb.MODID, "geo/sparkle_arrow.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Xeb.MODID, "textures/entity/sparkle_arrow.png");
    private static final ResourceLocation ANIM =
            new ResourceLocation(Xeb.MODID, "animations/sparkle_arrow.animation.json");

    @Override
    public ResourceLocation getModelResource(SparkleEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SparkleEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SparkleEntity entity) {
        return ANIM; // file doesn't exist — GeckoLib silently ignores missing anim files
    }
}
