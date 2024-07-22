package com.github.standobyte.jojo.client.particle;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.RisingParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;

public class RPSPickPartile extends RisingParticle {
    
    protected RPSPickPartile(ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, float size, IAnimatedSprite sprite) {
        super(world, x, y, z, 0, 0, 0, xSpeed, ySpeed, zSpeed, size, sprite, 1, 5, 0.004D, false);
        this.rCol = 1;
        this.gCol = 1;
        this.bCol = 1;
        this.lifetime = 40;
    }

    @Override
    public void tick() {
        super.tick();
        setAlpha(Math.min((1 - (float) (age) / (float) lifetime) * 4, 1));
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite sprites;

        public Factory(IAnimatedSprite sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new RPSPickPartile(world, x, y, z, xSpeed, ySpeed, zSpeed, 2.0F, this.sprites);
        }
    }

}
