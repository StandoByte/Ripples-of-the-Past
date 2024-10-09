package com.github.standobyte.jojo.client.particle;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DivineSandstormParticle extends SpriteTexturedParticle {
    private final IAnimatedSprite sprites;

    protected DivineSandstormParticle(ClientWorld pLevel, double pX, double pY, double pZ, double pQuadSizeMulitiplier, IAnimatedSprite pSprites) {
        super(pLevel, pX, pY, pZ, 0.0D, 0.0D, 0.0D);
        this.lifetime = 6 + this.random.nextInt(4);
        float f = this.random.nextFloat() * 0.6F + 0.4F;
        this.rCol = f;
        this.gCol = f;
        this.bCol = f;
        this.quadSize = 2.0F * (1.0F - (float)pQuadSizeMulitiplier * 0.5F);
        this.sprites = pSprites;
        this.setSpriteFromAge(pSprites);
        alpha = 0.2F;
    }

    @Override
    public int getLightColor(float pPartialTick) {
        return 0xF000F0;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.sprites);
        }
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite sprites;

        public Factory(IAnimatedSprite sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            DivineSandstormParticle sandstormparticle = new DivineSandstormParticle(pLevel, pX, pY, pZ, pXSpeed, this.sprites);
            return sandstormparticle;
        }
    }
}