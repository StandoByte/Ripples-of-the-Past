package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

public class HamonPlantInfusion extends HamonOrganismInfusion {

    public HamonPlantInfusion(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public HamonAction replaceAction(INonStandPower power, ActionTarget target) {
        return ModHamonActions.HAMON_ORGANISM_INFUSION.get();
    }

    @Override
    public ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, INonStandPower power) {
        BlockPos blockPos = target.getBlockPos();
        BlockState blockState = user.level.getBlockState(blockPos);
        if (blockState.getMaterial() == Material.EGG) {
            return conditionMessage("turtle_egg");
        }
        Block block = blockState.getBlock();
        if (!(isBlockLiving(blockState) || block instanceof FlowerPotBlock && blockState.getBlock() != Blocks.FLOWER_POT)) {
            return conditionMessage("living_plant");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.BLOCK;
    }
}
