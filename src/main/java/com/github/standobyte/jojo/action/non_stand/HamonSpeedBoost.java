package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class HamonSpeedBoost extends HamonAction {

    public HamonSpeedBoost(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
        float hamonEfficiency = hamon.getActionEfficiency(getEnergyCost(power, target), false);
        float effectStr = (float) hamon.getHamonControlLevel() / (float) HamonData.MAX_STAT_LEVEL * hamonEfficiency;
        int speedLvl = MathHelper.floor(1.5F * effectStr);
        int hasteLvl = MathHelper.floor(1.5F * effectStr);
        if (hamon.isSkillLearned(ModHamonSkills.AFTERIMAGES.get())) {
            speedLvl++;
            hasteLvl++;
        }
        if (!world.isClientSide()) {
            int duration = 20 + MathHelper.floor(180F * effectStr);
            if (hamonEfficiency == 1 && hamon.isSkillLearned(ModHamonSkills.AFTERIMAGES.get())) {
                user.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.addAfterimages(Math.min((int) (effectStr * 7F / 1.5F), 7), duration);
                });
            }
            if (!user.hasEffect(Effects.MOVEMENT_SPEED)) {
                hamon.hamonPointsFromAction(HamonStat.CONTROL, getEnergyCost(power, target));
            }
            user.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, duration, speedLvl));
            user.addEffect(new EffectInstance(Effects.DIG_SPEED, duration, hasteLvl));
        }
        HamonUtil.emitHamonSparkParticles(world, user instanceof PlayerEntity ? (PlayerEntity) user : null, user.position(), (speedLvl + 1) * 0.25F);
    }
}
