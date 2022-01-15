package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.MRFireballEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.world.World;

public class MagiciansRedFireball extends StandEntityAction {

    public MagiciansRedFireball(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, ActionTarget target) {
        if (!world.isClientSide()) {
            MRFireballEntity fireball = new MRFireballEntity(standEntity, world);
            fireball.shootFromRotation(standEntity, 1.5F, 4.0F);
            world.addFreshEntity(fireball);
            standEntity.playSound(ModSounds.MAGICIANS_RED_FIREBALL.get(), 1.0F, 1.0F);
        }
    }

}
