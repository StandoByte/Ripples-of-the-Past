package com.github.standobyte.jojo.action.stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.util.SoundEvent;

public class SilverChariotMeleeBarrage extends StandEntityMeleeBarrage {

    public SilverChariotMeleeBarrage(StandEntityMeleeBarrage.Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkStandConditions(StandEntity stand, IStandPower power, ActionTarget target) {
        if (stand instanceof SilverChariotEntity && !((SilverChariotEntity) stand).hasRapier()) {
            return conditionMessage("chariot_rapier");
        }
        return super.checkStandConditions(stand, power, target);
    }
    
    @Override
    public int getHoldDurationMax(IStandPower standPower) {
        if (standPower.getStandManifestation() instanceof SilverChariotEntity && !((SilverChariotEntity) standPower.getStandManifestation()).hasArmor()) {
            return Integer.MAX_VALUE;
        }
        return super.getHoldDurationMax(standPower);
    }
    
    @Override
    protected SoundEvent getShout(LivingEntity user, IStandPower power, ActionTarget target, boolean wasActive) {
        if (power.isActive()) {
            SilverChariotEntity chariot = (SilverChariotEntity) power.getStandManifestation();
            if (!chariot.hasRapier()) {
                return null;
            }
            if (!chariot.hasArmor() && chariot.getTicksAfterArmorRemoval() < 40) {
                return ModSounds.POLNAREFF_FENCING.get();
            }
        }
        return super.getShout(user, power, target, wasActive);
    }

    @Override
    public BarrageEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        BarrageEntityPunch stabBarrage = super.punchEntity(stand, target, dmgSource);
        if (target instanceof SkeletonEntity) {
            stabBarrage.damage(stabBarrage.getDamage() * 0.75F);
        }
        return stabBarrage;
    }
    
    @Override
    protected void clTtickSwingSound(int tick, StandEntity standEntity) {
        SoundEvent swingSound = getPunchSwingSound();
        if (swingSound != null) {
            standEntity.playSound(swingSound, 0.25F, 
                    0.9F + standEntity.getRandom().nextFloat() * 0.2F, ClientUtil.getClientPlayer());
        }
    }
}
