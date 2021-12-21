package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.entity.AfterimageEntity;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.HamonPowerType;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class HamonSpeedBoost extends HamonAction {

    public HamonSpeedBoost(Builder builder) {
        super(builder);
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        HamonData hamon = power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).get();
        float effectStr = (float) hamon.getHamonControlLevel() / (float) HamonData.MAX_STAT_LEVEL;
        int speedLvl = MathHelper.floor(1.5F * effectStr);
        int hasteLvl = 0;
        if (hamon.isSkillLearned(HamonSkill.AFTERIMAGES)) {
            speedLvl += 1;
            hasteLvl = 1;
        }
        if (!world.isClientSide()) {
            int duration = 20 + MathHelper.floor(180F * effectStr);
            if (hamon.isSkillLearned(HamonSkill.AFTERIMAGES)) {
                for (int i = 1; i <= 7; i++) {
                    AfterimageEntity afterimage = new AfterimageEntity(world, user, i);
                    afterimage.setLifeSpan(duration);
                    world.addFreshEntity(afterimage);
                }
            }
            if (!user.hasEffect(Effects.MOVEMENT_SPEED)) {
                hamon.hamonPointsFromAction(HamonStat.CONTROL, getEnergyCost());
            }
            user.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, duration, speedLvl));
            user.addEffect(new EffectInstance(Effects.DIG_SPEED, duration, hasteLvl));
        }
        // FIXME test particles here (was written weird)
        HamonPowerType.createHamonSparkParticles(world, user instanceof PlayerEntity ? (PlayerEntity) user : null, user.position(), (speedLvl + 1) * 0.25F);
    }
}
