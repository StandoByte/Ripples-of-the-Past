package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class ZombieClawLacerate extends ZombieAction {

    public ZombieClawLacerate(ZombieAction.Builder builder) {
        super(builder.withUserPunch());
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (power.getTypeSpecificData(ModPowers.ZOMBIE.get()).get().isDisguiseEnabled()) {
            return conditionMessage("disguise");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ANY;
    }
 
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
    	VampirismClawLacerate.punchPerform(world, user, power, target, ModSounds.THE_WORLD_PUNCH_HEAVY_ENTITY.get(), 1.0F, 1.0F);
    }
    
}
