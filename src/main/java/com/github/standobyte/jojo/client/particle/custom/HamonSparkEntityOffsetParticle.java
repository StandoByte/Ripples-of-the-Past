package com.github.standobyte.jojo.client.particle.custom;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.MathHelper;

public class HamonSparkEntityOffsetParticle extends EntityOffsetParticle {

    public HamonSparkEntityOffsetParticle(ClientWorld world, Entity entity, 
            double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
            ParticleType<?> sparkParticleType) {
        super(world, entity, x, y, z);
        
        this.xd = (Math.random() * 2.0 - 1.0) * 0.4;
        this.yd = (Math.random() * 2.0 - 1.0) * 0.4;
        this.zd = (Math.random() * 2.0 - 1.0) * 0.4;
        double f = (Math.random() + Math.random() + 1.0) * 0.15;
        double f1 = MathHelper.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
        this.xd = this.xd / f1 * f * 0.4;
        this.yd = this.yd / f1 * f * 0.4 + 0.1;
        this.zd = this.zd / f1 * f * 0.4;
        
        this.xd *= 0.1;
        this.yd *= 0.1;
        this.zd *= 0.1;
        this.xd += xSpeed * 0.4;
        this.yd += ySpeed * 0.4;
        this.zd += zSpeed * 0.4;
        this.quadSize *= 0.75F;
        this.lifetime = Math.max((int)(6.0D / (Math.random() * 0.8D + 0.6D)), 1);
        this.hasPhysics = false;
        
        pickSprite(CustomParticlesHelper.getSavedSpriteSet(sparkParticleType));
    }
    
    @Override
    protected void tickSpeed() {
        offset = offset.add(xd, yd, zd);
        xd *= 0.7;
        yd *= 0.7;
        zd *= 0.7;
        yd -= 0.02;
        if (onGround) {
            xd *= 0.7;
            zd *= 0.7;
        }
    }
    
    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
    }
    
}
