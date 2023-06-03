package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.ZoomPunchEntity;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class HamonZoomPunch extends HamonAction {

    public HamonZoomPunch(HamonAction.Builder builder) {
        super(builder.emptyMainHand());
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
            float hamonEfficiency = hamon.getActionEfficiency(getEnergyCost(power));
            float length = (8 + hamon.getHamonControlLevel() * 0.1F);
            int duration = Math.max(getCooldownTechnical(power), 1);
            ZoomPunchEntity zoomPunch = new ZoomPunchEntity(world, user, 
                    2 * length / (float) duration * (0.4F + 0.6F * hamonEfficiency), duration,
                    0.75F, getEnergyCost(power), 
                    getEnergyCost(power) * hamonEfficiency);
            world.addFreshEntity(zoomPunch);
        }
    }

}
