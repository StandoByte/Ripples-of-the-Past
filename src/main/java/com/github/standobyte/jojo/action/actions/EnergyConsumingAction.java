package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.SyncEnergyPacket;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class EnergyConsumingAction extends Action<INonStandPower> {
    private final float energyCost;
    private final float heldTickEnergyCost;
    
    public EnergyConsumingAction(EnergyConsumingAction.AbstractBuilder<?> builder) {
        super(builder);
        this.energyCost = builder.energyCost;
        this.heldTickEnergyCost = builder.heldTickEnergyCost;
    }
    
    public float getEnergyNeeded(int ticksHeld, INonStandPower power) {
        if (getHoldDurationMax() > 0) {
            return getEnergyCost() + getHeldTickEnergyCost() * Math.max((getHoldDurationToFire(power) - ticksHeld), 1);
        }
        return getEnergyCost();
    }
    
    public float getEnergyCost() {
        return energyCost;
    }
    
    public float getHeldTickEnergyCost() {
        return heldTickEnergyCost;
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, LivingEntity performer, INonStandPower power, ActionTarget target) {
        if (!power.isUserCreative()) {
            if (power.getEnergy() < getEnergyNeeded(power.getHeldActionTicks(), power)) {
                serverPlayerUser.ifPresent(player -> {
                    PacketManager.sendToClient(new SyncEnergyPacket(getPowerClassification(), getMana()), player);
                });
                ITextComponent message = new TranslationTextComponent("jojo.message.action_condition.no_mana_" + getType().getManaString());
                return ActionConditionResult.createNegative(message);
            }
        }
        return super.checkConditions(user, performer, power, target);
    }
    
    
    
    public static class Builder extends EnergyConsumingAction.AbstractBuilder<EnergyConsumingAction.Builder> {

        @Override
        protected EnergyConsumingAction.Builder getThis() {
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
