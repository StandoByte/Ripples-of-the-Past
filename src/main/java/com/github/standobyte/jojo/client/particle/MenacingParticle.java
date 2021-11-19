package com.github.standobyte.jojo.client.particle;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;

public class MenacingParticle extends SpriteTexturedParticle {
    private double offsetX;
    private double offsetY;
    private double offsetZ;

    protected MenacingParticle(ClientWorld world, double posX, double posY, double posZ) {
        this(world, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
    }

    public MenacingParticle(BasicParticleType type, ClientWorld world, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed) {
        this(world, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed);
    }

    protected MenacingParticle(ClientWorld world, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed) {
        super(world, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed);
        lifetime = 400;
        quadSize = 0.15F;
        hasPhysics = false;
        xd = xSpeed;
        yd = ySpeed;
        zd = zSpeed;
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
        else {
            double x = xd - offsetX;
            double y = yd - offsetY;
            double z = zd - offsetZ;
            offsetX = (Math.random() - 0.5) * 0.02;
            offsetY = (Math.random() - 0.5) * 0.01;
            offsetZ = (Math.random() - 0.5) * 0.02;
            move(x + offsetX, y + offsetY, z + offsetZ);
        }
        alpha = MathHelper.clamp((float) lifetime / (float) age * 3F - 3F, 0F, 1F);
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite sprite) {
            this.spriteSet = sprite;
        }

        public Particle createParticle(BasicParticleType type, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            MenacingParticle particle = new MenacingParticle(world, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(spriteSet);
            return particle;
        }
    }
}