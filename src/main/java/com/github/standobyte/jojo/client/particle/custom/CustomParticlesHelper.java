package com.github.standobyte.jojo.client.particle.custom;

import com.github.standobyte.jojo.client.ClientUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;

public class CustomParticlesHelper {
    
    public static boolean createCDRestorationParticle(LivingEntity entity, Hand hand) {
        if (!ClientUtil.canSeeStands()) return false;
        
        EntityPosParticle particle = CDRestorationHandItemParticle.createCustomParticle((ClientWorld) entity.level, entity, hand);
        return addParticle(particle, particle.getPos(), false, false);
    }
    
    public static boolean addParticle(Particle particle, Vector3d particlePos, boolean overrideLimiter, boolean alwaysVisible) {
        Minecraft mc = Minecraft.getInstance();
        ActiveRenderInfo activerenderinfo = mc.gameRenderer.getMainCamera();
        if (activerenderinfo.isInitialized() && mc.particleEngine != null && activerenderinfo.getPosition().distanceToSqr(particlePos) < 1024.0D) {
            if (alwaysVisible || calculateParticleLevel(mc, mc.level, alwaysVisible) != ParticleStatus.MINIMAL) {
                mc.particleEngine.add(particle);
                return true;
            } 
        }
        return false;
    }

    private static ParticleStatus calculateParticleLevel(Minecraft mc, ClientWorld world, boolean overrideLimiter) {
        ParticleStatus status = mc.options.particles;
        if (overrideLimiter && status == ParticleStatus.MINIMAL && world.random.nextInt(10) == 0) {
            status = ParticleStatus.DECREASED;
        }

        if (status == ParticleStatus.DECREASED && world.random.nextInt(3) == 0) {
            status = ParticleStatus.MINIMAL;
        }

        return status;
    }
}
