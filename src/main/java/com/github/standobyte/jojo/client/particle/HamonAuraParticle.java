package com.github.standobyte.jojo.client.particle;

import java.util.Random;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.RisingParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;

public class HamonAuraParticle extends RisingParticle {

    protected HamonAuraParticle(ClientWorld world, double x, double y, double z, double xda, double yda, double zda, float sizeM, IAnimatedSprite sprites) {
        super(world, x, y, z, 0.1F, 0.1F, 0.1F, xda, yda, zda, sizeM, sprites, 0.3F, 8, 0.004D, true);
        this.rCol = 1;
        this.gCol = 1;
        this.bCol = 1;
        lifetime = 25 + random.nextInt(10);
        alpha = 0.25F;
    }
    
    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
    
    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public void render(IVertexBuilder vertexBuilder, ActiveRenderInfo camera, float partialTick) {
        float ageF = ((float) age + partialTick) / (float) lifetime;
        this.alpha = ageF <= 0.5F ? 
                0.05F + ageF * 0.6F
                : 0.65F - ageF * 0.6F;
        super.render(vertexBuilder, camera, partialTick);
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private static final Random RANDOM = new Random();
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            HamonAuraParticle particle = new HamonAuraParticle(world, x, y, z, xSpeed, ySpeed, zSpeed, 1.2F + 0.6F * RANDOM.nextFloat(), this.spriteSet);
            return particle;
        }
    }
}
