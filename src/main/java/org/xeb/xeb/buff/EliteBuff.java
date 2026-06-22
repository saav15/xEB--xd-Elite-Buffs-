package org.xeb.xeb.buff;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public abstract class EliteBuff {
    private final String id;
    private final String displayName;
    private final BuffType buffType;
    private final int color;
    private final double weight;

    public EliteBuff(String id, String displayName, BuffType buffType, int color, double weight) {
        this.id = id;
        this.displayName = displayName;
        this.buffType = buffType;
        this.color = color;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BuffType getBuffType() {
        return buffType;
    }

    public int getColor() {
        return color;
    }

    public double getWeight() {
        return weight;
    }

    public abstract void onAttach(LivingEntity entity);

    public abstract void onDetach(LivingEntity entity);

    public abstract void onServerTick(LivingEntity entity, ServerLevel level);

    public void onDamageTaken(LivingEntity entity, LivingHurtEvent event) {}

    public void onDamageDealt(LivingEntity entity, LivingHurtEvent event) {}

    public void onHurt(LivingEntity entity, LivingHurtEvent event) {}

    public void onDeath(LivingEntity entity, LivingDeathEvent event) {}

    public void onKill(LivingEntity entity, LivingDeathEvent event) {}

    public void onProjectileImpact(LivingEntity entity, ProjectileImpactEvent event) {}
}
