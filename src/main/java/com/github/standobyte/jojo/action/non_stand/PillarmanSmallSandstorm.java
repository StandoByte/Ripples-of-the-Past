package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.HamonTurquoiseBlueOverdriveEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.PillarmanDivineSandstormEntity;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class PillarmanSmallSandstorm extends PillarmanAction {

    public PillarmanSmallSandstorm(PillarmanAction.Builder builder) {
        super(builder.doNotCancelClick());
        mode = Mode.WIND;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            PillarmanDivineSandstormEntity sandstormWave = new PillarmanDivineSandstormEntity(world, user)
                    .setRadius(1.5F)
                    .setDamage(7.5F)
                    .setDuration(40);
            sandstormWave.shootFromRotation(user, 1.5F, 1F);
            world.addFreshEntity(sandstormWave);
        }
    }

}
