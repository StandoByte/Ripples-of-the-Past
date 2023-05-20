package com.github.standobyte.jojo.client.particle;

import java.util.Random;

import com.github.standobyte.jojo.client.ClientEventHandler;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.RisingParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3d;

public class HamonAuraParticle extends RisingParticle {
    private static final Random RANDOM = new Random();
    private final int startingSpriteRandom;
    private final LivingEntity user;
    private final boolean isForFirstPerson;
    private final HandSide firstPersonHandSide;
    
    protected HamonAuraParticle(ClientWorld world, LivingEntity entity, boolean firstPerson, HandSide firstPersonHandSide,
            double x, double y, double z, double xda, double yda, double zda, IAnimatedSprite sprites) {
        super(world, x, y, z, 0.1F, 0.1F, 0.1F, xda, yda, zda, 1.2F + 0.6F * RANDOM.nextFloat(), sprites, 0.3F, 8, 0.004D, true);
        this.user = entity;
        this.isForFirstPerson = firstPerson;
        this.firstPersonHandSide = firstPersonHandSide;
        this.rCol = 1;
        this.gCol = 1;
        this.bCol = 1;
        lifetime = 25 + random.nextInt(10);
        startingSpriteRandom = random.nextInt(lifetime);
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

    private static final float ALPHA_MIN = 0.05F;
    private static final float ALPHA_DIFF = 0.3F;
    @Override
    public void render(IVertexBuilder vertexBuilder, ActiveRenderInfo camera, float partialTick) {
        if (user != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.cameraEntity == user) {
                boolean gameInFirstPerson = mc.options.getCameraType() == PointOfView.FIRST_PERSON;
                
                if (isForFirstPerson ^ gameInFirstPerson) {
                    return;
                }
                
                if (isForFirstPerson) {
                    boolean isAtMainArm = user.getMainArm() == firstPersonHandSide;
                    if (!ClientEventHandler.mainHandRendered && isAtMainArm
                     || !ClientEventHandler.offHandRendered  && !isAtMainArm) {
                        return;
                    }
                    // FIXME !!!!!! particles at arms in 1st person
                }
            }
        }
        
        float ageF = ((float) age + partialTick) / (float) lifetime;
        float alphaFunc = ageF <= 0.5F ? ageF * 2 : (1 - ageF) * 2;
        this.alpha = ALPHA_MIN + alphaFunc * ALPHA_DIFF;
        super.render(vertexBuilder, camera, partialTick);
    }
    
    @Override
    public void setSpriteFromAge(IAnimatedSprite pSprite) {
        setSprite(pSprite.get((age + startingSpriteRandom) % lifetime, lifetime));
    }

    public static HamonAuraParticle createCustomParticle(IAnimatedSprite sprites, ClientWorld world, 
            LivingEntity entity, double x, double y, double z) {
        HamonAuraParticle particle = new HamonAuraParticle(world, entity, false, null, x, y, z, 0, 0, 0, sprites);
        return particle;
    }

    public static HamonAuraParticle firstPersonHandParticle(IAnimatedSprite sprites, 
            ClientWorld world, LivingEntity entity, HandSide handSide, Vector3d initialVec) {
        HamonAuraParticle particle = new HamonAuraParticle(world, entity, true, handSide, 
                initialVec.x, initialVec.y, initialVec.z, 0, 0, 0, sprites);
        return particle;
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            HamonAuraParticle particle = new HamonAuraParticle(world, null, false, null, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
            return particle;
        }
    }
}
