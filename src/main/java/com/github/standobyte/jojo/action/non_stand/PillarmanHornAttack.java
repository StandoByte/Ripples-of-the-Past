package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.PillarmanHornEntity;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class PillarmanHornAttack extends PillarmanAction {

    public PillarmanHornAttack(PillarmanAction.Builder builder) {
        super(builder);
        stage = 2;
        canBeUsedInStone = true;
    }

    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            PillarmanHornEntity pillarmanHorn = new PillarmanHornEntity(world, user);
            pillarmanHorn.setLifeSpan(40);
            world.addFreshEntity(pillarmanHorn);
        }
    }


}
