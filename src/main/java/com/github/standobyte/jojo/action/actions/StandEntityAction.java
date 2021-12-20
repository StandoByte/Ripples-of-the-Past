package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.stand.IStandManifestation;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public abstract class StandEntityAction extends StandAction {
    protected boolean doNotAutoSummonStand;
    
    public StandEntityAction(StandEntityAction.AbstractBuilder<?> builder) {
        super(builder);
        this.doNotAutoSummonStand = builder.doNotAutoSummonStand;
    }
    
    @Override
    public LivingEntity getPerformer(LivingEntity user, IStandPower power) {
        return power.isActive() ? (StandEntity) power.getStandManifestation() : user;
    }

    @Override
    public void updatePerformer(World world, LivingEntity user, IStandPower power) {
        if (!world.isClientSide() && !doNotAutoSummonStand && !power.isActive()) {
            power.getType().summon(user, power, true);
        }
    }
    
    @Override
    protected ActionTarget aim(World world, LivingEntity user, IStandPower power, double range) {
        LivingEntity aimingEntity = user;
        if (power.isActive()) {
            IStandManifestation stand = power.getStandManifestation();
            if (stand instanceof StandEntity) {
                StandEntity standEntity = (StandEntity) stand;
                if (standEntity.isManuallyControlled()) {
                    aimingEntity = standEntity;
                }
            }
        }
        return super.aim(world, aimingEntity, power, range);
    }
    
    public static class Builder extends StandEntityAction.AbstractBuilder<StandEntityAction.Builder> {

        @Override
        protected StandEntityAction.Builder getThis() {
            return this;
        }
    }
    
    protected abstract static class AbstractBuilder<T extends StandEntityAction.AbstractBuilder<T>> extends StandAction.AbstractBuilder<T> {
        protected boolean doNotAutoSummonStand;
        
        public T doNotAutoSummonStand() {
            this.doNotAutoSummonStand = true;
            return getThis();
        }
    }
}
