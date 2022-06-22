package com.github.standobyte.jojo.client.particle;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;

public class BloodParticle extends SpriteTexturedParticle {

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
        if (age++ >= lifetime) {
            remove();
        }
        else if (xd > 0 || yd > 0 || zd > 0) {
            yd -= 0.04D * (double) gravity;
            double xdPrev = xd;
            double ydPrev = yd;
            double zdPrev = zd;
            move(xd, yd, zd);
            double smallVal = 1.0E-5;
            if (Math.abs(xdPrev) >= smallVal && Math.abs(xd) < smallVal || 
                Math.abs(ydPrev) >= smallVal && Math.abs(yd) < smallVal || 
                Math.abs(zdPrev) >= smallVal && Math.abs(zd) < smallVal) {
                xd = 0;
                yd = 0;
                zd = 0;
            }
        }
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
            particle.setLifetime(20);
            particle.scale(0.5F);
            particle.gravity = 0;
            return particle;
        }
    }

}
