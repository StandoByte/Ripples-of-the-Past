package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.MRFireballEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class MagiciansRedFireball extends StandEntityAction {

    public MagiciansRedFireball(Builder builder) {
        super(builder);
    }
    
    @Override
    public void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            LivingEntity stand = getPerformer(user, power);
            MRFireballEntity fireball = new MRFireballEntity(stand, world);
            fireball.shootFromRotation(stand, 1.5F, 4.0F);
            world.addFreshEntity(fireball);
            stand.playSound(ModSounds.MAGICIANS_RED_FIREBALL.get(), 1.0F, 1.0F);
        }
    }

}
