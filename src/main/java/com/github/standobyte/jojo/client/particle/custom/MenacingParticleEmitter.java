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
    private final Entity entity;
    private int life;
    private final int lifeTime;
    private final BasicParticleType particleType;
    private final Vector3d offset1;
    private final Vector3d offset2;
    private boolean superConstructorTicked;
    private IParticleFactory<BasicParticleType> particleFactory;
    
    public MenacingParticleEmitter(ClientWorld level, Entity entity, BasicParticleType particleType, PlayerEntity player) {
        super(level, entity, particleType, 40);
        this.entity = entity;
        this.lifeTime = 200;
        this.particleType = particleType;
        
        Vector3d vecFromPlayer = entity.position().subtract(player.position());
        float yRot = (90 - MathUtil.yRotDegFromVec(vecFromPlayer)) * MathUtil.DEG_TO_RAD;
        this.offset1 = new Vector3d(0, 0, entity.getBbWidth()).yRot(yRot);
        this.offset2 = new Vector3d(0, 0, -(entity.getBbWidth())).yRot(yRot);
        
        IAnimatedSprite sprites = CustomParticlesHelper.getSavedSpriteSet((ParticleType<?>) particleType);
        if (sprites != null) {
            this.particleFactory = new OnomatopoeiaParticle.GoFactory(sprites);
        }
        
        this.tick();
    }
    
    @Override
    public void tick() {
        if (!superConstructorTicked) {
            superConstructorTicked = true;
            return;
        }
        
        if (life % 40 == 0) {
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
                    pos.x, pos.y, pos.z, 0, 0.01, 0);
            particle.setLifetime(200);
            Minecraft.getInstance().particleEngine.add(particle);
        }
        else {
            level.addParticle(particleType, false, pos.x, pos.y, pos.z, 0, 0.01, 0);
        }
    }

}
