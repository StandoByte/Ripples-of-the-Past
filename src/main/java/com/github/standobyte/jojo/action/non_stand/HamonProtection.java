package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.Action.TargetRequirement;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HamonProtection extends HamonAction {
    
    public HamonProtection(HamonAction.Builder builder) {
        super(builder);
    }
    
    
    
    /*@Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, 
            int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (world.isClientSide()) {
            HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
            float efficiency = hamon.getActionEfficiency(getHeldTickEnergyCost(power), false);
            HamonSparksLoopSound.playSparkSound(user, user.getBoundingBox().getCenter(), 1.0F, efficiency);
            CustomParticlesHelper.createHamonSparkParticles(user, 
                    user.getRandomX(0.5), user.getRandomY(), user.getRandomZ(0.5), 
                    (int) (MathUtil.fractionRandomInc(efficiency) * 2));
        }
    }*/
    
    @Override
    public ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (power.getHeldAction() != this) {
            if (power.getEnergy() <= 0) {
                return conditionMessage("some_energy");
            }
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected Action<INonStandPower> replaceAction(INonStandPower power, ActionTarget target) {
        LivingEntity user = power.getUser();
        boolean prot = power.getTypeSpecificData(ModPowers.HAMON.get()).get().getHamonProtection();
    	if (prot == true) {
            return ModHamonActions.HAMON_PROTECTION_OFF.get();
        }
        return super.replaceAction(power, target);
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {    
    	power.getTypeSpecificData(ModPowers.HAMON.get()).get().toggleHamonProtection();
    }

    public float reduceDamageAmount(INonStandPower power, LivingEntity user, 
            DamageSource dmgSource, float dmgAmount) {
        float damageReductionMult;
        if (user.getType() == ModEntityTypes.HAMON_MASTER.get()) {
            damageReductionMult = 1;
        }
        
        else {
            HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
            float energyCost = dmgAmount * 50;
            damageReductionMult = hamon.consumeHamonEnergyTo(efficiency -> {
                float baseReduction = 0.3F + hamon.getHamonControlLevelRatio() * 0.5F;
                hamon.hamonPointsFromAction(HamonStat.CONTROL, Math.min(energyCost, power.getEnergy()) * efficiency);
                return MathHelper.clamp(baseReduction * efficiency, 0, 1);
            }, energyCost);
        }
        
        if (damageReductionMult > 0) {
            float damageReduced = dmgAmount * damageReductionMult;
            
            Entity sourceEntity = dmgSource.getDirectEntity();
            Vector3d sourcePos = sourceEntity.getEyePosition(1.0F);
            AxisAlignedBB userHitbox = user.getBoundingBox();
            Vector3d damagePos;
            if (userHitbox.contains(sourcePos)) {
                damagePos = sourcePos;
            }
            else {
                Vector3d userEyePos = user.getEyePosition(1.0F);
                damagePos = userHitbox.clip(sourcePos, sourcePos.add(sourceEntity.getLookAngle().scale(16))).orElse(userEyePos);
            }
            HamonUtil.emitHamonSparkParticles(user.level, null, damagePos, damageReduced * 0.25F);
            return dmgAmount - damageReduced;
        }
        else {
            return dmgAmount;
        }
    }
}
