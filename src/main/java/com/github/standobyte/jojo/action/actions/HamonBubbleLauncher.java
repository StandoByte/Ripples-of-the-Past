package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleEntity;
import com.github.standobyte.jojo.power.IPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class HamonBubbleLauncher extends HamonAction {

    public HamonBubbleLauncher(Builder builder) {
        super(builder);
    }
    
    @Override
    public void onHoldTickUser(World world, LivingEntity user, IPower<?> power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
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
