package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.MRRedBindEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity.StandPose;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.world.World;

public class MagiciansRedRedBind extends StandEntityAction {
    public static final StandPose RED_BIND_POSE = new StandPose("MR_RED_BIND");

    public MagiciansRedRedBind(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide()) {
            standEntity.addProjectile(new MRRedBindEntity(world, standEntity, userPower));
            standEntity.playSound(ModSounds.MAGICIANS_RED_RED_BIND.get(), 1.0F, 1.0F);
        }
    }
}
