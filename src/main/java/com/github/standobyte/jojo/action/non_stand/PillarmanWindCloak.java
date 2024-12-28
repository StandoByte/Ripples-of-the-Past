package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;

import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class PillarmanWindCloak extends PillarmanAction {

    public PillarmanWindCloak(PillarmanAction.Builder builder) {
        super(builder.holdType());
        mode = Mode.WIND;
    }
    
    @Override
    public void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide() && requirementsFulfilled) {
        	user.addEffect(new EffectInstance(Effects.INVISIBILITY, 5, 0, false, false));
            user.addEffect(new EffectInstance(ModStatusEffects.SUN_RESISTANCE.get(), 5, 0, false, false));
        }
    }
      
    @Override
    public void startedHolding(World world, LivingEntity user, INonStandPower power, ActionTarget target, boolean requirementsFulfilled) {
    	if (requirementsFulfilled) {
    		windEffect(user, ModParticles.SANDSTORM.get(), 15);
    	}
    }
    
    @Override
    public void stoppedHolding(World world, LivingEntity user, INonStandPower power, int ticksHeld, boolean willFire) {
    	windEffect(user, ModParticles.SANDSTORM.get(), 15);
    }
    
    public static void windEffect(LivingEntity user, IParticleData particles, int intensity) {
        for (int i = 0; i < intensity; i++) {
            Vector3d particlePos = user.position().add(
                    (Math.random() - 0.5) * (user.getBbWidth() + 0.5), 
                    Math.random() * (user.getBbHeight()), 
                    (Math.random() - 0.5) * (user.getBbWidth() + 0.5));
            user.level.addParticle(particles, particlePos.x, particlePos.y, particlePos.z, Math.random() - 0.5, Math.random(), Math.random() - 0.5);
        }
    }
}
