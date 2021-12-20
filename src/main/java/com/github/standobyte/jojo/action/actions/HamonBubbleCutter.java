package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonBubbleCutterEntity;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class HamonBubbleCutter extends HamonAction {

    public HamonBubbleCutter(Builder builder) {
        super(builder);
    }
    
    @Override
    public void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            boolean shift = isShiftVariation();
            int bubbles = shift ? 6 : 12;
            for (int i = 0; i < bubbles; i++) {
                HamonBubbleCutterEntity bubbleCutterEntity = new HamonBubbleCutterEntity(user, world);
                float velocity = 1.35F + user.getRandom().nextFloat() * 0.3F;
                bubbleCutterEntity.shootFromRotation(user, velocity, shift ? 2.0F : 8.0F);
                bubbleCutterEntity.setGliding(shift);
                world.addFreshEntity(bubbleCutterEntity);
            }
        }
    }
}
