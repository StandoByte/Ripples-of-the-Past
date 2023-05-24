package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonTurquoiseBlueOverdriveEntity;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class HamonTurquoiseBlueOverdrive extends HamonAction {

    public HamonTurquoiseBlueOverdrive(HamonAction.Builder builder) {
        super(builder.emptyMainHand());
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!user.isInWaterOrBubble()) {
            return conditionMessage("underwater");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
            float hamonEfficiency = hamon.getActionEfficiency(getEnergyCost(power));
            HamonTurquoiseBlueOverdriveEntity overdriveWave = new HamonTurquoiseBlueOverdriveEntity(world, user, 
                    1.5F + (float) (4.5F * hamon.getHamonControlLevelRatio() * hamonEfficiency), 
                    1.5F * hamonEfficiency, 
                    getEnergyCost(power) * hamonEfficiency);
            overdriveWave.shootFromRotation(user, 2F, 0);
            world.addFreshEntity(overdriveWave);
        }
    }

}
