package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvent;

public class SilverChariotMeleeBarrage extends StandEntityMeleeBarrage {

    public SilverChariotMeleeBarrage(StandEntityAction.Builder builder) {
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
    protected SoundEvent getShout(LivingEntity user, IStandPower power, ActionTarget target, boolean wasActive) {
        if (power.isActive()) {
            SilverChariotEntity chariot = (SilverChariotEntity) power.getStandManifestation();
            if (!chariot.hasRapier()) {
                return null;
            }
            if (!chariot.hasArmor() && chariot.getTicksAfterArmorRemoval() < 40) {
                return ModSounds.POLNAREFF_FENCING.get();
            }
        }
        return super.getShout(user, power, target, wasActive);
    }
    
}
