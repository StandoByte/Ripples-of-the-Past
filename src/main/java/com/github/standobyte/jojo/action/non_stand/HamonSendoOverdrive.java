package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.SendoHamonOverdriveEntity;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HamonSendoOverdrive extends HamonAction {

    public HamonSendoOverdrive(HamonAction.Builder builder) {
        super(builder);
    }

    // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! (sendo hamon overdrive) use regular overdrive when punching an entity
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
            float energyCost = getEnergyCost(power);
            float hamonEfficiency = hamon.getActionEfficiency(energyCost);
            
            BlockPos blockPos = target.getBlockPos();
            SendoHamonOverdriveEntity sendoOverdrive = new SendoHamonOverdriveEntity(world, 
                    user, target.getFace().getAxis())
                    .setRadius((4 + hamon.getHamonControlLevelRatio() * 4) * hamonEfficiency)
                    .setWaveDamage(1.25F * hamonEfficiency)
                    .setWavesCount(2 + (int) ((2 + Math.min(hamon.getHamonControlLevelRatio() * 3, 2)) * hamonEfficiency))
                    .setStatPoints(Math.min(energyCost, power.getEnergy()) * hamonEfficiency);
            sendoOverdrive.moveTo(Vector3d.atCenterOf(blockPos).subtract(0, sendoOverdrive.getDimensions(null).height * 0.5, 0));
            world.addFreshEntity(sendoOverdrive);
        }
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.BLOCK;
    }
}
