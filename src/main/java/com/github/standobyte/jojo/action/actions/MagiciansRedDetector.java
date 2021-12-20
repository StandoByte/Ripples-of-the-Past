package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.MRDetectorEntity;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class MagiciansRedDetector extends StandEntityAction {

    public MagiciansRedDetector(Builder builder) {
        super(builder);
    }
    
    @Override
    public void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            MRDetectorEntity detector = new MRDetectorEntity(user, world);
            detector.copyPosition(user);
            world.addFreshEntity(detector);
        }
    }

}
