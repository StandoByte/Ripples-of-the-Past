package com.github.standobyte.jojo.action.non_stand;

import java.util.Optional;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.TypeSpecificData;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public abstract class NonStandAction extends Action<INonStandPower> {
    private final float energyCost;
    private final float heldTickEnergyCost;
    
    public NonStandAction(NonStandAction.AbstractBuilder<?> builder) {
        super(builder);
        this.energyCost = builder.energyCost;
        this.heldTickEnergyCost = builder.heldTickEnergyCost;
    }
    
    @Override
    public PowerClassification getPowerClassification() {
        return PowerClassification.NON_STAND;
    }
    
    public float getEnergyNeeded(int ticksHeld, INonStandPower power, ActionTarget target) {
        float cost = getEnergyCost(power, target);
        if (getHoldDurationMax(power) > 0) {
            return cost += getHeldTickEnergyCost(power) * Math.max((getHoldDurationToFire(power) - ticksHeld), 1);
        }
        return cost;
    }
    
    public float getEnergyCost(INonStandPower power, ActionTarget target) {
        return energyCost;
    }
    
    public float getHeldTickEnergyCost(INonStandPower power) {
        return heldTickEnergyCost;
    }
    
    @Override
    public float getCostToRender(INonStandPower power, ActionTarget target) {
        int ticksHeld = power.getHeldAction() == this ? power.getHeldActionTicks() : 0;
        return getEnergyNeeded(ticksHeld, power, target);
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        ActionConditionResult energyCheck = checkEnergy(user, power, target);
        if (!energyCheck.isPositive()) {
            return energyCheck;
        }
        return super.checkConditions(user, power, target);
    }
    
    protected ActionConditionResult checkEnergy(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!power.hasEnergy(getEnergyNeeded(power.getHeldActionTicks(), power, target))) {
            ITextComponent message = new TranslationTextComponent("jojo.message.action_condition.no_energy_" + power.getType().getRegistryName().getPath());
            return ActionConditionResult.createNegative(message);
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public boolean isUnlocked(INonStandPower power) {
        Optional<TypeSpecificData> dataOptional = power.getTypeSpecificData(null);
        return dataOptional.map(data -> data.isActionUnlocked(this, power)).orElse(false);
    }
    
    @Override
    public void afterPerform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        super.afterPerform(world, user, power, target);
        if (!world.isClientSide()) {
            power.consumeEnergy(getEnergyCost(power, target));
        }
    }
    
    @Override
    public void onHoldTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
            power.consumeEnergy(getHeldTickEnergyCost(power));
        }
        super.onHoldTick(world, user, power, ticksHeld, target, requirementsFulfilled);
    }
    
    
    
    public static class Builder extends NonStandAction.AbstractBuilder<NonStandAction.Builder> {

        @Override
        protected NonStandAction.Builder getThis() {
            return this;
        }
    }
    
    protected abstract static class AbstractBuilder<T extends Action.AbstractBuilder<T>> extends Action.AbstractBuilder<T> {
        private float energyCost = 0;
        private float heldTickEnergyCost = 0;
        
        public T energyCost(float energyCost) {
            this.energyCost = energyCost;
            return getThis();
        }
        
        public T holdEnergyCost(float energyCost) {
            this.heldTickEnergyCost = energyCost;
            return getThis();
        }
    }
}
