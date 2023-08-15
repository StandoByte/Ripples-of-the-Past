package com.github.standobyte.jojo.potion;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectType;
import net.minecraftforge.common.util.LazyOptional;

public class StandVirusEffect extends UncurableEffect implements IApplicableEffect {
    
    public StandVirusEffect(int liquidColor) {
        super(EffectType.HARMFUL, liquidColor);
    }
    
    @Override
    public boolean isApplicable(LivingEntity entity) {
        return entity instanceof PlayerEntity;
    }
    
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level.isClientSide() && entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            
            boolean tookAwayLevel = player.abilities.instabuild || player.experienceLevel > 0;
            boolean stopEffect = false;
            LazyOptional<PlayerUtilCap> utilData = player.getCapability(PlayerUtilCapProvider.CAPABILITY);
            if (tookAwayLevel) {
                player.giveExperienceLevels(-1);
                stopEffect = utilData.map(cap -> {
                    return cap.decXpLevelsTakenByArrow() >= cap.getStandXpLevelsRequirement();
                }).orElse(true); // fail-safe, better to give a stand for 1 level than to have the effect permanently
            }
            
            float damage = 0.2F + amplifier * 0.1F;
            if (tookAwayLevel) {
                if (damage > entity.getHealth()) {
                    damage = 0.001F;
                }
            }
            else {
                damage *= 10;
            }
            DamageUtil.hurtThroughInvulTicks(entity, DamageUtil.STAND_VIRUS, damage);
            
            if (stopEffect) {
                entity.removeEffect(this);
                StandType<?> stand = StandUtil.randomStand(player, player.getRandom());
                if (stand != null && GeneralUtil.orElseFalse(IStandPower.getStandPowerOptional(player), power -> power.givePower(stand))) {
                    utilData.ifPresent(cap -> cap.onGettingStandFromArrow());
                }
            }
        }
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
    
    
    public static final int MAX_VIRUS_INHIBITION = 3;
    public static final int STAND_XP_REQUIREMENT = 40;
    
    public static int getEffectLevelToApply(int inhibition) {
        return Math.max(MAX_VIRUS_INHIBITION - inhibition, 0);
    }
}
