package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.SCRapierEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            SilverChariotEntity chariot = (SilverChariotEntity) standEntity;
            SCRapierEntity rapier = new SCRapierEntity(standEntity, world);
            Entity aimingEntity = chariot;
            if (chariot.isFollowingUser()) {
                LivingEntity user = userPower.getUser();
                if (user != null) {
                    aimingEntity = user;
                }
            }
            rapier.setPos(aimingEntity.getX(), aimingEntity.getEyeY(), aimingEntity.getZ());
            if (chariot.isRapierOnFire()) {
                rapier.setSecondsOnFire(rapier.ticksLifespan() / 20);
            }
            standEntity.shootProjectile(rapier, 2F, 0);
            if (!userPower.isUserCreative()) {
                chariot.setRapier(false);
            }
        }
    }
    
    @Override
    protected boolean standKeepsTarget(ActionTarget target) {
        return true;
    }
}
