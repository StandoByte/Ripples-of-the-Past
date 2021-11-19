package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.stand.SilverChariotEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvent;

public class SilverChariotMeleeBarrage extends StandEntityMeleeBarrage {

    public SilverChariotMeleeBarrage(Builder builder) {
        super(builder);
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, LivingEntity performer, IPower<?> power, ActionTarget target) {
        if (performer instanceof SilverChariotEntity && !((SilverChariotEntity) performer).hasRapier()) {
            return conditionMessage("chariot_rapier");
        }
        return super.checkConditions(user, performer, power, target);
    }
    
    @Override
    protected SoundEvent getShout(LivingEntity user, IPower<?> power, ActionTarget target, boolean wasActive) {
        if (power.isActive()) {
            SilverChariotEntity chariot = (SilverChariotEntity) ((IStandPower) power).getStandManifestation();
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
