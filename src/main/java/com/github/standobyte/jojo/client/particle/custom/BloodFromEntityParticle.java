package com.github.standobyte.jojo.client.particle.custom;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.particle.BloodParticle;
import com.github.standobyte.jojo.init.ModEntityTypes;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particles.IParticleData;

public class BloodFromEntityParticle extends BloodParticle {
    private final Entity entity;

    protected BloodFromEntityParticle(ClientWorld world, Entity entity, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(world, x, y, z, xSpeed, ySpeed, zSpeed);
        this.entity = entity;
    }
    
    @Override
    protected List<Entity> checkEntity() {
        return level.getEntities(entity, this.getBoundingBox(), 
                entity -> entity.getType() != ModEntityTypes.CD_BLOOD_CUTTER.get());
    }

    public static BloodFromEntityParticle createCustomParticle(IParticleData type, ClientWorld world, @Nullable Entity entity,
            double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        BloodFromEntityParticle particle = new BloodFromEntityParticle(world, entity, x, y, z, xSpeed, ySpeed, zSpeed);
        particle.pickSprite(BloodParticle.Factory.getSprite());
        particle.setLifetime(60);
        particle.scale(0.5F);
        particle.gravity = 1;
        return particle;
    }

}
