package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.PillarmanDivineSandstormEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class PillarmanSmallSandstorm extends PillarmanAction {

    public PillarmanSmallSandstorm(PillarmanAction.Builder builder) {
        super(builder.swingHand());
        mode = Mode.WIND;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            PillarmanDivineSandstormEntity sandstormWave = new PillarmanDivineSandstormEntity(world, user, 0)
            		.setAtmospheric(false)
                    .setRadius(1.5F)
                    .setDamage(6F)
                    .setDuration(40);
            sandstormWave.shootFromRotation(user, 1.5F, 1F);
            world.addFreshEntity(sandstormWave);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.MAGICIANS_RED_FIRE_BLAST.get(), 
                    SoundCategory.AMBIENT, 0.2F, 1.0F);
        }
    }

}
