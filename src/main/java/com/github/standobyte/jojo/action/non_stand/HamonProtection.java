package com.github.standobyte.jojo.action.non_stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.general.LazySupplier;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HamonProtection extends HamonAction {
    
    public HamonProtection(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (power.getHeldAction() != this) {
            if (power.getEnergy() <= 0) {
                return conditionMessage("some_energy");
            }
        }
        return ActionConditionResult.POSITIVE;
    }
    
    private final LazySupplier<ResourceLocation> protectionTex = 
            new LazySupplier<>(() -> makeIconVariant(this, "_on"));

    @Override
    public ResourceLocation getIconTexturePath(@Nullable INonStandPower power) {
        if (power != null && power.getTypeSpecificData(ModPowers.HAMON.get()).get().isProtectionEnabled()) {
            return protectionTex.get();
        }
        else {
            return super.getIconTexturePath(power);
        }
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {  
        if (!world.isClientSide()) {
            power.getTypeSpecificData(ModPowers.HAMON.get()).get().toggleHamonProtection();
        }
    }
    
    @Override
    public boolean greenSelection(INonStandPower power, ActionConditionResult conditionCheck) {
        return power.getTypeSpecificData(ModPowers.HAMON.get())
                .map(HamonData::isProtectionEnabled).orElse(false);
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
