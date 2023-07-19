package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.ZoomPunchEntity;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class HamonZoomPunch extends HamonAction {
    private final float hitCost;

    public HamonZoomPunch(HamonZoomPunch.Builder builder) {
        super(builder.needsFreeMainHand());
        this.hitCost = builder.hitCost;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
            float energyCost = getEnergyCost(power, target);
            float hamonEfficiency = hamon.getActionEfficiency(energyCost, true);
            
            float zoomPunchMaxLength = 4 + (4 + hamon.getHamonControlLevel() * 0.1F) * hamonEfficiency;
            int duration = Math.max(getCooldownTechnical(power), 1);
            float projSpeed = 2 * zoomPunchMaxLength / (float) duration * (0.4F + 0.6F * hamonEfficiency);
            ZoomPunchEntity zoomPunch = new ZoomPunchEntity(world, user)
                    .setSpeed(projSpeed)
                    .setDuration(duration)
                    .setHamonDamageOnHit(0.7F, hitCost, power.getEnergy() <= 0)
                    .setBaseUsageStatPoints(Math.min(energyCost, power.getEnergy()) * hamonEfficiency);
            world.addFreshEntity(zoomPunch);
        }
    }
    
    
    public static class Builder extends HamonAction.AbstractBuilder<HamonZoomPunch.Builder> {
        private float hitCost = 0;
        
        public Builder hitCost(float hitCost) {
            this.hitCost = hitCost;
            return getThis();
        }
        
        @Override
        protected HamonZoomPunch.Builder getThis() {
            return this;
        }
    }
}
