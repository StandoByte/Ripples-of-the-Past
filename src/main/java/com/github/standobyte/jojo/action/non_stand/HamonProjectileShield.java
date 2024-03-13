package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.HamonProjectileShieldEntity;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class HamonProjectileShield extends HamonAction {

    public HamonProjectileShield(HamonAction.Builder builder) {
        super(builder.holdType());
    }
    
    @Override
    public void startedHolding(World world, LivingEntity user, INonStandPower power, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide() && requirementsFulfilled) {
            HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
            if (hamon.shieldEntity == null || !hamon.shieldEntity.isAlive()) {
                float width = 8;
                float height = 4;
                HamonProjectileShieldEntity shield = new HamonProjectileShieldEntity(world, user, width, height);
                shield.yRot = user.yRot;
                shield.xRot = user.xRot;
                shield.updateShieldPos();
                world.addFreshEntity(shield);
                hamon.shieldEntity = shield;
            }
        }
    }

}
