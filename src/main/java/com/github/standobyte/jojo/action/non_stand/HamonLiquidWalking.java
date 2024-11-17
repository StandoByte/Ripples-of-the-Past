package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.actionshud.BarsRenderer;
import com.github.standobyte.jojo.client.ui.actionshud.BarsRenderer.BarType;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;

public class HamonLiquidWalking {
    
    public static boolean onLiquidWalkingEvent(LivingEntity entity, FluidState fluidState) {
        boolean liquidWalking = isLiquidWalking(entity, fluidState);
        
        if (liquidWalking && entity.isEffectiveAi()) {
            INonStandPower.getNonStandPowerOptional((LivingEntity) entity).resolve()
            .flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get()))
            .ifPresent(hamon -> {
                hamon.setWaterWalkingThisTick();
            });
        }
        
        return liquidWalking;
    }
    
    private static boolean isLiquidWalking(LivingEntity entity, FluidState fluidState) {
        boolean doubleShift = entity.isShiftKeyDown() && entity.getCapability(PlayerUtilCapProvider.CAPABILITY).map(
                cap -> cap.getDoubleShiftPress()).orElse(false);
        if (doubleShift) {
            return false;
        }

        return INonStandPower.getNonStandPowerOptional(entity).map(power -> {
            return power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
                boolean liquidWalking = hamon.isSkillLearned(ModHamonSkills.LIQUID_WALKING.get());
                if (liquidWalking) {
                    Fluid fluidType = fluidState.getType();
                    if (!(fluidType.is(FluidTags.WATER) && entity.isOnFire())) {
                        if (power.getEnergy() > 0) {
                            entity.setOnGround(true);
                            if (!entity.level.isClientSide()) {
                                if (fluidType.is(FluidTags.LAVA) 
                                        && !entity.fireImmune() && !EnchantmentHelper.hasFrostWalker(entity)) {
                                    entity.hurt(DamageSource.HOT_FLOOR, 1.0F);
                                }
                                power.consumeEnergy(hamon.waterWalkingTickCost());
                            }
                            return true;
                        }
                        else if (entity.level.isClientSide() && entity == ClientUtil.getClientPlayer()) {
                            BarsRenderer.getBarEffects(BarType.ENERGY_HAMON).triggerRedHighlight(1);
                        }
                    }
                }
                return false;
            }).orElse(false);
        }).orElse(false);
        
    }

}
