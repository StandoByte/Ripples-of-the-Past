package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleEntity;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class HamonBubbleLauncher extends HamonAction {

    public HamonBubbleLauncher(HamonAction.Builder builder) {
        super(builder.holdType());
    }
    
    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled && !world.isClientSide()) {
            for (int i = 0; i < 4; i++) {
                HamonBubbleEntity bubbleEntity = new HamonBubbleEntity(user, world);
                float velocity = 0.1F + user.getRandom().nextFloat() * 0.5F;
                bubbleEntity.shootFromRotation(user, velocity, 16.0F);
                world.addFreshEntity(bubbleEntity);
            }
        }
    }

}
