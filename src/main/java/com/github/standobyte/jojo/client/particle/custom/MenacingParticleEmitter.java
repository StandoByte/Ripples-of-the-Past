package com.github.standobyte.jojo.client.particle.custom;

import com.github.standobyte.jojo.client.particle.OnomatopoeiaParticle;
import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EmitterParticle;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.vector.Vector3d;

public class MenacingParticleEmitter extends EmitterParticle {
    public final Entity entity;
    public int lifeTime = 200;
    public int interval = 40;
    
    public int life;
    public double speed = 1;
    public final BasicParticleType particleType;
    public final Vector3d offset1;
    public final Vector3d offset2;
    public boolean superConstructorTicked;
    public IParticleFactory<BasicParticleType> particleFactory;
    
    public MenacingParticleEmitter(ClientWorld level, Entity entity, BasicParticleType particleType, PlayerEntity player, double speed) {
        super(level, entity, particleType, 40);
        this.entity = entity;
        this.particleType = particleType;
        
        float yRot;
        if (entity == player) {
            yRot = entity.yRot;
        }
        else {
            Vector3d vecFromPlayer = entity.position().subtract(player.position());
            yRot = MathUtil.yRotDegFromVec(vecFromPlayer);
        }
        yRot = (90 - yRot) * MathUtil.DEG_TO_RAD;
        this.offset1 = new Vector3d(0, 0, entity.getBbWidth()).yRot(yRot);
        this.offset2 = new Vector3d(0, 0, -(entity.getBbWidth())).yRot(yRot);
        
        IAnimatedSprite sprites = CustomParticlesHelper.getSavedSpriteSet((ParticleType<?>) particleType);
        if (sprites != null) {
            this.particleFactory = new OnomatopoeiaParticle.GoFactory(sprites);
        }
        setSpeed(speed);
        this.tick();
    }
    
    public void setSpeed(double speed) {
        if (speed <= 0) return;
        this.lifeTime = (int) (this.lifeTime * this.speed / speed);
        this.interval = (int) (this.interval * this.speed / speed);
        this.speed = speed;
    }
    
    @Override
    public void tick() {
        if (!superConstructorTicked) {
            superConstructorTicked = true;
            return;
        }
        
        if (life % interval == 0) {
            addGoParticle(entity.position().add(offset1));
            addGoParticle(entity.position().add(offset2));
        }
        
        ++life;
        if (life >= lifeTime) {
            remove();
        }

    }
    
    protected void addGoParticle(Vector3d pos) {
        if (particleFactory != null) {
            Particle particle = particleFactory.createParticle(particleType, level, 
                    pos.x, pos.y, pos.z, 0, 0.01 * speed, 0);
            particle.setLifetime(lifeTime);
            Minecraft.getInstance().particleEngine.add(particle);
        }
        else {
            level.addParticle(particleType, false, pos.x, pos.y, pos.z, 0, 0.01 * speed, 0);
        }
    }

}
