package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;

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
    public HamonAction replaceAction(INonStandPower power) {
    	return ModActions.HAMON_ORGANISM_INFUSION.get();
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
    	BlockPos blockPos = target.getBlockPos();
    	BlockState blockState = user.level.getBlockState(blockPos);
    	if (blockState.getMaterial() == Material.EGG) {
    		return conditionMessage("animal_infusion");
    	}
    	Block block = blockState.getBlock();
    	if (!(isBlockLiving(blockState) || block instanceof FlowerPotBlock && blockState.getBlock() != Blocks.FLOWER_POT)) {
    		return conditionMessage("living_plant");
    	}
        return ActionConditionResult.POSITIVE;
    }
    
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.BLOCK;
    }
}
