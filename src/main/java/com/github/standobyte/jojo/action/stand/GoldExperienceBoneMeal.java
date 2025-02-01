package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.non_stand.HamonHealing;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.TreeLeavesDecay;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.WorldEvents;

public class GoldExperienceBoneMeal extends StandEntityAction {

    public GoldExperienceBoneMeal(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower power) {
        switch (target.getType()) {
        case ENTITY:
            Entity targetEntity = target.getEntity();
            return ActionConditionResult.noMessage(targetEntity instanceof AnimalEntity && ((AnimalEntity) targetEntity).isBaby());
        case BLOCK:
            World world = user.level;
            BlockPos blockPos = target.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);
            Block block = blockState.getBlock();
            if (block instanceof IGrowable || TreeLeavesDecay.isTreeStemBlock(block)) {
                return ActionConditionResult.POSITIVE;
            }
            
            blockPos = blockPos.relative(target.getFace());
            blockState = world.getBlockState(blockPos);
            if (blockState.is(Blocks.WATER) && world.getFluidState(blockPos).getAmount() == 8) {
                return ActionConditionResult.POSITIVE;
            }

            return ActionConditionResult.NEGATIVE;
        default:
            return ActionConditionResult.NEGATIVE;
        }
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ANY;
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        ActionTarget target = standEntity.aimWithThisOrUser(64, task.getTarget());
        standEntity.setTaskTarget(target);
        Entity targetEntity = target.getType() == TargetType.ENTITY ? target.getEntity() : null;
        
        if (targetEntity instanceof AnimalEntity) {
            AnimalEntity animal = (AnimalEntity) targetEntity;
            int age = animal.getAge();
            // particles don't appear if it's only called on the client side
            ((AnimalEntity) targetEntity).ageUp((int) ((-age / 20) * 0.2F), true);
        }
        
        if (!world.isClientSide()) {
            LivingEntity user = userPower.getUser();
            if (user instanceof PlayerEntity && target.getType() == TargetType.BLOCK) {
                BlockPos blockPos = target.getBlockPos();
                TreeLeavesDecay tree = TreeLeavesDecay.startDecay(world, blockPos, Integer.MAX_VALUE, 1);
                if (tree != null) {
                    tree.logs.forEach(logPos -> {
                        world.levelEvent(WorldEvents.BONEMEAL_PARTICLES, logPos, 5);
                    });
                }
                
                Direction face = target.getType() == TargetType.BLOCK ? target.getFace() : Direction.UP;
                HamonHealing.bonemealEffect(user.level, (PlayerEntity) user, blockPos, face);
            }
        }
    }
}
