package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.entity.damaging.projectile.MRFireballEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.world.World;

public class MagiciansRedFireball extends StandEntityAction {

    public MagiciansRedFireball(StandEntityAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            standEntity.shootProjectile(new MRFireballEntity(standEntity, world), 2.0F, 2.0F);
            standEntity.playSound(ModSounds.MAGICIANS_RED_FIREBALL.get(), 1.0F, 1.0F);
        }
    }

}
