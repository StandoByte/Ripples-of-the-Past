package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.capability.entity.LivingUtilCap;
import com.github.standobyte.jojo.capability.entity.LivingUtilCap.HypnosisTargetCheck;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.potion.HypnosisEffect;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class HamonHypnosis extends HamonAction {
    
    public HamonHypnosis(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }
    
    @Override
    public ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, INonStandPower power) {
        if (target.getEntity() instanceof LivingEntity) {
            HypnosisTargetCheck check = LivingUtilCap.canBeHypnotized((LivingEntity) target.getEntity(), user);
            switch (check) {
            case CORRECT:
                return ActionConditionResult.POSITIVE;
            case INVALID:
                return conditionMessage("hypnosis");
            case ALREADY_TAMED_BY_USER:
                return ActionConditionResult.NEGATIVE;
            }
        }
        return conditionMessage("hypnosis");
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide() && target.getType() == TargetType.ENTITY) {
            HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
            float controlLvl = hamon.getHamonControlLevelRatio();
            int duration = (int) (controlLvl * controlLvl * 24000);
            if (duration > 0) {
                HypnosisEffect.hypnotizeEntity((LivingEntity) target.getEntity(), user, duration);
            }
        }
    }
    
    // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! add proper energy check
    
    /* FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! to add
     *  spark visuals on cast
     *  spark visuals on hypnotized entities
     */
    
    // make the target not walk around
}
