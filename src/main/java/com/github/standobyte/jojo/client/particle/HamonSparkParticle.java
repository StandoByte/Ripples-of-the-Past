package com.github.standobyte.jojo.client.particle;

import net.minecraft.client.particle.CritParticle;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;

public class HamonSparkParticle extends CritParticle {

    public HamonSparkParticle(ClientWorld world, double x, double y, double z,
            double xDDDDD, double yd, double zd) {
        super(world, x, y, z, xDDDDD, yd, zd);
    }
    
    @Override
    public void tick() {
        super.tick();
        rCol = 1;
        gCol = 1;
        bCol = 1;
    }
    
    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }


    public static class HamonParticleFactory extends CritParticle.Factory {
        private final IAnimatedSprite sprite;

        public HamonParticleFactory(IAnimatedSprite sprite) {
            super(sprite);
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double xd, double yd, double zd) {
            HamonSparkParticle particle = new HamonSparkParticle(world, x, y, z, xd, yd, zd);
            particle.pickSprite(sprite);
            particle.setColor(1, 1, 1);
            return particle;
        }
    }
}
