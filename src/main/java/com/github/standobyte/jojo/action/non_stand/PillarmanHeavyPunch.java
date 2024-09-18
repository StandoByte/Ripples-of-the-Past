package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class PillarmanHeavyPunch extends PillarmanAction {

    public PillarmanHeavyPunch(PillarmanAction.Builder builder) {
        super(builder.withUserPunch());
        stage = 1;
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ANY;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
    	VampirismClawLacerate.punchPerform(world, user, power, target, ModSounds.HAMON_SYO_PUNCH.get(), 1.5F, 0.8F); // TODO separate sound event
    }

}
