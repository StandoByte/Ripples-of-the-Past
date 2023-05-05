package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class HamonSunlightYellowOverdrive extends HamonAction {

    public HamonSunlightYellowOverdrive(HamonAction.Builder builder) {
        super(builder.emptyMainHand());
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
        if (!hamon.isSkillLearned(ModHamonSkills.SUNLIGHT_YELLOW_OVERDRIVE.get())) {
            return ActionConditionResult.NEGATIVE;
        }
        Entity entity = target.getEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity targetEntity = (LivingEntity) entity;
            if (!targetEntity.isInvulnerableTo(DamageUtil.HAMON) && targetEntity.getBoundingBox()
                    .inflate(user.getBbWidth() + 0.5F).intersects(user.getBoundingBox())) {
                return ActionConditionResult.POSITIVE;
            }
        }
        return ActionConditionResult.NEGATIVE;
    }
    
    @Override
    public float getEnergyCost(INonStandPower power) {
        return Math.max(power.getEnergy(), super.getEnergyCost(power));
    }
    
    private float getDamageMultiplier(INonStandPower power) {
        return 1.2F * power.getEnergy() / super.getEnergyCost(power);
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            Entity entity = target.getEntity();
            if (entity instanceof LivingEntity) {
                LivingEntity targetEntity = (LivingEntity) entity;
                HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
                
                float damage = 0.5F * getDamageMultiplier(power);
                
                float dmgScale = 1;
                if (user instanceof PlayerEntity) {
                    float swingStrengthScale = ((PlayerEntity) user).getAttackStrengthScale(0.5F);
                    dmgScale = (0.2F + swingStrengthScale * swingStrengthScale * 0.8F);
                    damage *= dmgScale;
                }
                damage *= dmgScale;
                
                if (DamageUtil.dealHamonDamage(targetEntity, damage, user, null)) {
                    hamon.hamonPointsFromAction(HamonStat.STRENGTH, getEnergyCost(power) * dmgScale);
                }
            }
        }
    }
    
}
