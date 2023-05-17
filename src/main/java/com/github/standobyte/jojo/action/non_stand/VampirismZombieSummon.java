package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.mob.HungryZombieEntity;
import com.github.standobyte.jojo.init.ModCustomStats;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public class VampirismZombieSummon extends VampirismAction {

    public VampirismZombieSummon(NonStandAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        World world = user.level;
        if (world.getDifficulty() == Difficulty.PEACEFUL) {
            return conditionMessage("peaceful");
        }
        int zombiesMaxInArea = world.getDifficulty().getId() * 10;
        if (world.getEntitiesOfClass(HungryZombieEntity.class, new AxisAlignedBB(
                user.getX(), 0, user.getZ(), 
                user.getX(), 256, user.getZ())
                .inflate(16, 0, 16))
                .size() > zombiesMaxInArea) {
            return conditionMessage("zombies_limit");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            int zombiesToSummon = world.getDifficulty().getId();
            for (int i = 0; i < zombiesToSummon; i++) {
                HungryZombieEntity zombie = new HungryZombieEntity(world);
                zombie.setSummonedFromAbility();
                zombie.copyPosition(user);
                zombie.setOwner(user);
                world.addFreshEntity(zombie);
            }
            if (user instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) user).awardStat(ModCustomStats.VAMPIRE_ZOMBIES_SUMMONED, zombiesToSummon);
            }
        }
    }
    
    @Override
    protected int maxCuringStage() {
        return 3;
    }
}
