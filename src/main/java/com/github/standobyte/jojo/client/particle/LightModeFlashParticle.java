package com.github.standobyte.jojo.client.particle;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.particle.custom.HamonAuraParticleRenderType;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;

public class LightModeFlashParticle extends SpriteTexturedParticle {
    
    private LightModeFlashParticle(ClientWorld world, double x, double y, double z) {
        super(world, x, y, z);
        this.lifetime = 4;
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
//        return HamonAuraParticleRenderType.HAMON_AURA; // KEKW
    }
    
    @Override
    protected int getLightColor(float partialTick) {
        return ClientUtil.MAX_MODEL_LIGHT;
    }

    @Override
    public void render(IVertexBuilder pBuffer, ActiveRenderInfo pRenderInfo, float pPartialTicks) {
        this.setAlpha(0.6F - ((float)this.age + pPartialTicks - 1.0F) * 0.25F * 0.5F);
        super.render(pBuffer, pRenderInfo, pPartialTicks);
    }

    @Override
    public float getQuadSize(float pScaleFactor) {
        return 7.1F * MathHelper.sin(((float)this.age + pScaleFactor - 1.0F) * 0.25F * (float)Math.PI);
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            LightModeFlashParticle particle = new LightModeFlashParticle(world, x, y, z);
            particle.pickSprite(spriteSet);
            return particle;
        }
    }

}
