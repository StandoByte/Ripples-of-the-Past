package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.SCRapierEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class SilverChariotRapierLaunch extends StandEntityAction {

    public SilverChariotRapierLaunch(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, LivingEntity performer, IStandPower power, ActionTarget target) {
        if (performer instanceof SilverChariotEntity && !((SilverChariotEntity) performer).hasRapier()) {
            return conditionMessage("chariot_rapier");
        }
        return super.checkSpecificConditions(user, performer, power, target);
    }
    
    @Override
    public void standTickPerform(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide() && ticks == 0) {
            SCRapierEntity rapierEntity = new SCRapierEntity(standEntity, world);
            rapierEntity.shootFromRotation(standEntity, 1F, 0.0F);
            world.addFreshEntity(rapierEntity);
            ((SilverChariotEntity) standEntity).setRapier(false);
        }
    }

}
