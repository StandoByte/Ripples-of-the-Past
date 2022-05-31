package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.SCRapierEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.world.World;

public class SilverChariotRapierLaunch extends StandEntityAction {

    public SilverChariotRapierLaunch(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        if (stand instanceof SilverChariotEntity && !((SilverChariotEntity) stand).hasRapier()) {
            return conditionMessage("chariot_rapier");
        }
        return super.checkStandConditions(stand, power, target);
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide()) {
        	SilverChariotEntity chariot = (SilverChariotEntity) standEntity;
        	SCRapierEntity rapier = new SCRapierEntity(standEntity, world);
        	if (chariot.isRapierOnFire()) {
        		rapier.setSecondsOnFire(rapier.ticksLifespan());
        	}
            standEntity.shootProjectile(rapier, 1F, 0);
            if (!userPower.isUserCreative()) {
            	chariot.setRapier(false);
            }
        }
    }
}
