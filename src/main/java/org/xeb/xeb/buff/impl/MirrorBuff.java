package org.xeb.xeb.buff.impl;

import org.xeb.xeb.buff.BuffType;
import org.xeb.xeb.buff.EliteBuff;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;

public class MirrorBuff extends EliteBuff {
    public MirrorBuff() {
        super("mirror", "Mirror", BuffType.ENEMY_ONLY, 0xC0C0C0, 1.0D, false);
    }

    @Override
    public void onAttach(LivingEntity entity) {}

    @Override
    public void onDetach(LivingEntity entity) {}

    @Override
    public void onServerTick(LivingEntity entity, ServerLevel level) {}

    @Override
    public void onProjectileImpact(LivingEntity entity, ProjectileImpactEvent event) {
        HitResult hit = event.getRayTraceResult();
        if (hit.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hit;
            if (entityHit.getEntity() == entity) {
                Projectile proj = event.getProjectile();
                
                // Reflect velocity
                Vec3 vel = proj.getDeltaMovement();
                proj.setDeltaMovement(vel.scale(-1.2D));
                proj.hurtMarked = true;
                
                // Swap owner to current entity so it doesn't hurt us again
                Entity originalOwner = proj.getOwner();
                proj.setOwner(entity);
                
                // Deal 2 damage to mirroring entity
                entity.hurt(entity.damageSources().magic(), 2.0F);
                
                // Cancel collision
                event.setCanceled(true);
            }
        }
    }
}
