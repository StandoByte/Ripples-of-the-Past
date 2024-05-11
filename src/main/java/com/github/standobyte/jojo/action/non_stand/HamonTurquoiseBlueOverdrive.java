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
        super(builder.needsFreeMainHand());
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
            float energyCost = getEnergyCost(power, target);
            float hamonEfficiency = hamon.getActionEfficiency(energyCost, true);
            float hamonControl = hamon.getHamonControlLevelRatio();
            
            HamonTurquoiseBlueOverdriveEntity overdriveWave = new HamonTurquoiseBlueOverdriveEntity(world, user)
                    .setRadius(1F + (float) (2.5F * hamonControl * hamonEfficiency))
                    .setDamage(1F * hamonEfficiency)
                    .setPoints(Math.min(energyCost, power.getEnergy()) * hamonEfficiency)
                    .setDuration(30 + (int) (70 * hamonControl));
            overdriveWave.shootFromRotation(user, 1.5F, 0);
            world.addFreshEntity(overdriveWave);
        }
    }

}
