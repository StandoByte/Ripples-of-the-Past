package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.mob.HungryZombieEntity;
import com.github.standobyte.jojo.power.IPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public class VampirismZombieSummon extends Action {

    public VampirismZombieSummon(AbstractBuilder<?> builder) {
        super(builder);
    }
    
    @Override
    public ActionConditionResult checkConditions(LivingEntity user, LivingEntity performer, IPower<?> power, ActionTarget target) {
        if (user.level.getDifficulty() == Difficulty.PEACEFUL) {
            return conditionMessage("peaceful");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public void perform(World world, LivingEntity user, IPower<?> power, ActionTarget target) {
        if (!world.isClientSide()) {
            int difficulty = world.getDifficulty().getId();
            for (int i = 0; i < difficulty; i++) {
                HungryZombieEntity zombie = new HungryZombieEntity(world);
                zombie.copyPosition(user);
                zombie.setOwner(user);
                world.addFreshEntity(zombie);
            }
        }
    }
}
