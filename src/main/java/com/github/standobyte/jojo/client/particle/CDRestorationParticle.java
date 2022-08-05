package com.github.standobyte.jojo.client.particle;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;

public class CDRestorationParticle extends SpriteTexturedParticle {

    protected CDRestorationParticle(ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(world, x, y, z, xSpeed, ySpeed, zSpeed);
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private static Factory instance;
        private final IAnimatedSprite sprite;

        public Factory(IAnimatedSprite sprite) {
            this.sprite = sprite;
            instance = this;
        }

        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            CDRestorationParticle particle = new CDRestorationParticle(world, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(sprite);
            return particle;
        }
        
        public static IAnimatedSprite getSprite() {
            return instance.sprite;
        }
    }
}
