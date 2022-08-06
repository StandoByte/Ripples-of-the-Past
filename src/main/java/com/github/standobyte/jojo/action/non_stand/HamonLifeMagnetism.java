package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.LeavesGliderEntity;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;

import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class HamonLifeMagnetism extends HamonAction {

    public HamonLifeMagnetism(HamonAction.Builder builder) {
        super(builder.emptyMainHand());
    }
    
    @Override
    public ActionConditionResult checkTarget(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!(user.level.getBlockState(target.getBlockPos()).getBlock() instanceof LeavesBlock)) {
            return conditionMessage("leaves");
        }
        return super.checkTarget(user, power, target);
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.BLOCK;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            world.destroyBlock(target.getBlockPos(), false);
            LeavesGliderEntity glider = new LeavesGliderEntity(world);
            glider.moveTo(target.getBlockPos(), user.xRot, user.yRot);
            world.addFreshEntity(glider);
            HamonData hamon = power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).get();
            hamon.hamonPointsFromAction(HamonStat.CONTROL, getEnergyCost(power));
        }
    }

}
