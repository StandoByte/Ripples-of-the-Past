package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.MRRedBindEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.world.World;

public class MagiciansRedRedBind extends StandEntityAction {

    public MagiciansRedRedBind(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    public void standTickPerform(World world, StandEntity standEntity, int ticks, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide() && ticks == 0) {
            world.addFreshEntity(new MRRedBindEntity(world, standEntity, userPower));
            standEntity.playSound(ModSounds.MAGICIANS_RED_RED_BIND.get(), 1.0F, 1.0F);
        }
    }
}
