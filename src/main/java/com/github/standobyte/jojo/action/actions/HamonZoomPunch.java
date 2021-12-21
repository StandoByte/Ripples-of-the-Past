package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.ZoomPunchEntity;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class HamonZoomPunch extends HamonAction {

    public HamonZoomPunch(Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, LivingEntity performer, INonStandPower power, ActionTarget target) {
        ItemStack heldItemStack = performer.getMainHandItem();
        if (!heldItemStack.isEmpty()) {
            return conditionMessage("hand");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            HamonData hamon = power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).get();
            ZoomPunchEntity zoomPunch = new ZoomPunchEntity(world, user, hamon.getHamonControlLevel());
            world.addFreshEntity(zoomPunch);
        }
    }

}
