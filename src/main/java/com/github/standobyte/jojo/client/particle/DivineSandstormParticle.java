package com.github.standobyte.jojo.client.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
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

   public int getLightColor(float pPartialTick) {
      return 15728880;
   }

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
   public void render(IVertexBuilder vertexBuilder, ActiveRenderInfo camera, float partialTick) {
       this.alpha = 0.2F;
       super.render(vertexBuilder, camera, partialTick);
   }

   public IParticleRenderType getRenderType() {
      return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   @OnlyIn(Dist.CLIENT)
   public static class Factory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprites;

      public Factory(IAnimatedSprite p_i50634_1_) {
         this.sprites = p_i50634_1_;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
    	  DivineSandstormParticle sandstormparticle = new DivineSandstormParticle(pLevel, pX, pY, pZ, pXSpeed, this.sprites);
    	  sandstormparticle.setAlpha(0.2F);
    	  return sandstormparticle;
      }
   }
}