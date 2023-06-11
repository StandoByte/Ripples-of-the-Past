package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class HamonOverdrive extends HamonAction {

    public HamonOverdrive(HamonAction.Builder builder) {
        super(builder.doNotCancelClick());
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }
    
    @Override
    public ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (user instanceof PlayerEntity && ((PlayerEntity) user).getAttackStrengthScale(0.5F) < 0.9F) {
            return ActionConditionResult.NEGATIVE;
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected Action<INonStandPower> replaceAction(INonStandPower power, ActionTarget target) {
        if (GeneralUtil.orElseFalse(power.getTypeSpecificData(ModPowers.HAMON.get()), hamon -> {
            return hamon.isSkillLearned(ModHamonSkills.METAL_SILVER_OVERDRIVE.get());
        }) && HamonMetalSilverOverdrive.itemUsesMSO(power.getUser())) {
            return ModHamonActions.JONATHAN_METAL_SILVER_OVERDRIVE.get();
        }
        return super.replaceAction(power, target);
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide() && target.getType() == TargetType.ENTITY) {
            Entity entity = target.getEntity();
            if (entity instanceof LivingEntity) {
                LivingEntity targetEntity = (LivingEntity) entity;
                HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
                float cost = getEnergyCost(power);
                float efficiency = hamon.getActionEfficiency(cost);
                
                float damage = getDamage();
                float dmgScale = efficiency;
                if (user instanceof PlayerEntity) {
                    float swingStrengthScale = ((PlayerEntity) user).getAttackStrengthScale(0.5F);
                    dmgScale *= (0.2F + swingStrengthScale * swingStrengthScale * 0.8F);
                }
                damage *= dmgScale;
                
                int attackStrengthTicker = CommonReflection.getAttackStrengthTicker(user);
                if (dealDamage(targetEntity, damage, user, power, hamon)) {
                    addPointsForAction(power, hamon, HamonStat.STRENGTH, cost, efficiency);
                }
                // FIXME (hamon) !! ClientPlayerEntity#swing sends the packet to server, and THEN ServerPlayerEntity#swing resets attackStrengthTicker
                CommonReflection.setAttackStrengthTicker(user, attackStrengthTicker);
            }
        }
    }
    
    protected float getDamage() {
        return 2.0F;
    }
    
    protected boolean dealDamage(LivingEntity target, float dmgAmount, LivingEntity user, INonStandPower power, HamonData hamon) {
        return DamageUtil.dealHamonDamage(target, dmgAmount, user, null);
    }
}
