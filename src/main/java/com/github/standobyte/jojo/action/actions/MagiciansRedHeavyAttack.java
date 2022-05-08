package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.world.World;

public class MagiciansRedHeavyAttack extends StandEntityHeavyAttack {

    public MagiciansRedHeavyAttack(Builder builder) {
        super(builder);
    }

    @Override
    protected void setAction(IStandPower standPower, StandEntity standEntity, int ticks, Phase phase, ActionTarget target) {
        if (standEntity.willHeavyPunchCombo()) {
            MagiciansRedRedBind.getLandedRedBind(standEntity).ifPresent(redBind -> {
                redBind.setKickCombo();
            });
        }
        super.setAction(standPower, standEntity, ticks, phase, target);
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide()) {
            MagiciansRedRedBind.getLandedRedBind(standEntity).ifPresent(redBind -> {
                if (redBind.isInKickCombo()) {
                    redBind.remove();
                }
            });
        }
        super.standPerform(world, standEntity, userPower, target);
    }
}
