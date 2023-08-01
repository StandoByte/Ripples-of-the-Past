package com.github.standobyte.jojo.client.particle.custom;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;

public class EntityOffsetParticle extends EntityPosParticle {
    protected Vector3d offset;
    @Nullable private final LivingEntity entityLiving;
    
    protected EntityOffsetParticle(ClientWorld world, Entity entity, 
            double x, double y, double z) {
        super(world, entity, false);
        setPos(x, y, z);
        this.entityLiving = entity instanceof LivingEntity ? ((LivingEntity) entity) : null;
        this.offset = new Vector3d(x, y, z).subtract(entity.position())
                .yRot(getYRot() * MathUtil.DEG_TO_RAD);
        xo = x;
        yo = y;
        zo = z; 
    }
    
    private float getYRot() {
        return entityLiving != null ? entityLiving.yBodyRot : entity.yRot;
    }
    
    @Override
    protected Vector3d getNextTickPos(Vector3d entityPos) {
        tickSpeed();
        return entity != null ? entityPos.add(offset
                .yRot(-getYRot() * MathUtil.DEG_TO_RAD)) : null;
    }
    
    protected void tickSpeed() {
        yd -= 0.04 * gravity;
        offset = offset.add(xd, yd, zd);
        xd *= 0.98;
        yd *= 0.98;
        zd *= 0.98;
        if (onGround) {
            xd *= 0.7;
            zd *= 0.7;
        }
    }
    
}
