package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.world.World;

public class VampirismHamonSuicide extends VampirismAction {

    public VampirismHamonSuicide(NonStandAction.Builder builder) {
        super(builder); 
    }
    
    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (!world.isClientSide()) {
            if (ticksHeld % 10 == 5) {
                DamageUtil.dealHamonDamage(user, 4, user, null);
            }
            if (ticksHeld == 30) {
                user.addEffect(new EffectInstance(ModEffects.HAMON_SPREAD.get(), 100, 1));
            }
        }
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            DamageUtil.dealHamonDamage(user, Float.MAX_VALUE, user, null);
        }
    }
}
