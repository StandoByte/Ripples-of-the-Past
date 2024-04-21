package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;

import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class PillarmanWindCloak extends PillarmanAction {

    public PillarmanWindCloak(PillarmanAction.Builder builder) {
        super(builder);
        mode = Mode.WIND;
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean requirementsFulfilled, boolean stateRefreshed) {
        if (requirementsFulfilled) {
        	for (int i = 0; i < 4; i++) {
            Vector3d particlePos = user.position().add(
                    (Math.random() - 0.5) * (user.getBbWidth() + 0.75), 
                    Math.random() * (user.getBbHeight() + 0.25 ), 
                    (Math.random() - 0.5) * (user.getBbWidth() + 0.75));
            user.level.addParticle(ParticleTypes.CLOUD, particlePos.x, particlePos.y, particlePos.z, 0, 0.1, 0);
        	}
        }
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
        	user.addEffect(new EffectInstance(Effects.INVISIBILITY, 200, 0));
        	user.addEffect(new EffectInstance(ModStatusEffects.SUN_RESISTANCE.get(), 200, 0));
        }
    }

}
