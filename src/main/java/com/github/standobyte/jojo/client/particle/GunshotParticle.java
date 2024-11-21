package com.github.standobyte.jojo.client.particle;

import com.github.standobyte.jojo.client.ClientUtil;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;

public class GunshotParticle extends SpriteTexturedParticle {
    
    protected GunshotParticle(ClientWorld world, double x, double y, double z) {
        super(world, x, y, z, 0, 0, 0);
        xd = 0;
        yd = 0;
        zd = 0;
        gravity = 0;
        hasPhysics = false;
        setLifetime(3);
        scale(0.75F);
        roll = (float)Math.random() * ((float)Math.PI * 2F);
        oRoll = roll;
    }
    
    @Override
    public void render(IVertexBuilder pBuffer, ActiveRenderInfo pRenderInfo, float pPartialTicks) {
        alpha = age > lifetime - 2 ? 1 - pPartialTicks : 1;
        super.render(pBuffer, pRenderInfo, pPartialTicks);
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
    
    @Override
    protected int getLightColor(float partialTick) {
        return ClientUtil.MAX_MODEL_LIGHT;
    }
    
    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;
        private static IAnimatedSprite spriteStatic;

        public Factory(IAnimatedSprite sprite) {
            this.spriteSet = sprite;
            spriteStatic = sprite;
        }

        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            GunshotParticle particle = new GunshotParticle(world, x, y, z);
            particle.pickSprite(spriteSet);
            return particle;
        }
        
        public static IAnimatedSprite getSprite() {
            return spriteStatic;
        }
    }

}