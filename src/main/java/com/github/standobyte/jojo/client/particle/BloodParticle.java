package com.github.standobyte.jojo.client.particle;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.vector.Vector3d;

public class BloodParticle extends SpriteTexturedParticle {
    private Optional<Pair<Entity, Vector3d>> entityOffset = Optional.empty();

    protected BloodParticle(ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(world, x, y, z);
        this.xd = xSpeed + (Math.random() * 2.0D - 1.0D) * 0.1;
        this.yd = ySpeed + (Math.random() * 2.0D - 1.0D) * 0.1;
        this.zd = zSpeed + (Math.random() * 2.0D - 1.0D) * 0.1;
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        float ageRatio = (float) age / (float) lifetime;
        this.alpha = Math.min((1 - ageRatio) * 4, 1);
        if (age++ >= lifetime) {
            remove();
        }
        else if (entityOffset.isPresent()) {
            Entity entity = entityOffset.get().getLeft();
            if (!entity.isAlive()) {
                entityOffset = Optional.empty();
            }
            else {
                Vector3d pos = entity.position().subtract(entityOffset.get().getRight());
                setPos(pos.x, pos.y, pos.z);
            }
        }
        else if (xd > 0 || yd > 0 || zd > 0) {
            if (ageRatio >= 0.35F) {
                yd -= 0.04D * (double) gravity;
            }
            double xdPrev = xd;
            double ydPrev = yd;
            double zdPrev = zd;
            move(xd, yd, zd);
            double smallVal = 1.0E-5;
            if (Math.abs(xdPrev) >= smallVal && Math.abs(xd) < smallVal || 
                Math.abs(ydPrev) >= smallVal && Math.abs(yd) < smallVal || 
                Math.abs(zdPrev) >= smallVal && Math.abs(zd) < smallVal) {
                stopParticle();
            }
            else {
                List<Entity> entities = level.getEntities(null, this.getBoundingBox());
                if (!entities.isEmpty()) {
                    Entity entity = entities.get(0);
                    entityOffset = Optional.of(Pair.of(entity, entity.position().subtract(x, y, z)));
                }
            }
        }
    }
    
    private void stopParticle() {
        xd = 0;
        yd = 0;
        zd = 0;
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            BloodParticle particle = new BloodParticle(world, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(spriteSet);
            particle.setLifetime(60);
            particle.scale(0.5F);
            particle.gravity = 1;
            return particle;
        }
    }

}
