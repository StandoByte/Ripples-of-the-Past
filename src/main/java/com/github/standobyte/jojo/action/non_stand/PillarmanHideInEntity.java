package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.mod.IPlayerPossess;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class PillarmanHideInEntity extends PillarmanAction {

    public PillarmanHideInEntity(PillarmanAction.Builder builder) {
        super(builder);
        stage = 2;
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {  
        if (!world.isClientSide() && user instanceof IPlayerPossess && target.getEntity() instanceof LivingEntity) {
            ((IPlayerPossess) user).jojoPossessEntity((LivingEntity) target.getEntity(), true, this);
        }
    }
}
