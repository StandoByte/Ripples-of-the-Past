package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.MRRedBindEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.IPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class MagiciansRedRedBind extends StandEntityAction {

    public MagiciansRedRedBind(Builder builder) {
        super(builder);
    }
    
    @Override
    public void onStartedHolding(World world, LivingEntity user, IPower<?> power, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide() && requirementsFulfilled) {
            LivingEntity entity = getPerformer(user, power);
            if (entity instanceof StandEntity) {
                StandEntity stand = (StandEntity) getPerformer(user, power);
                world.addFreshEntity(new MRRedBindEntity(world, stand, power));
                stand.setStandPose(StandPose.ABILITY);
                stand.playSound(ModSounds.MAGICIANS_RED_RED_BIND.get(), 1.0F, 1.0F);
            }
        }
    }
    
    @Override
    public void onStoppedHolding(World world, LivingEntity user, IPower<?> power, int ticksHeld) {
        if (!world.isClientSide()) {
            LivingEntity entity = getPerformer(user, power);
            if (entity instanceof StandEntity) {
                ((StandEntity) entity).setStandPose(StandPose.NONE);
            }
        }
    }
}
