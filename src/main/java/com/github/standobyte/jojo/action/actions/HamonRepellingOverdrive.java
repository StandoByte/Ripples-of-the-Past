package com.github.standobyte.jojo.action.actions;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.HamonPowerType;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class HamonRepellingOverdrive extends HamonAction {

    public HamonRepellingOverdrive(Builder builder) {
        super(builder);
    }
    
    @Override
    public void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            HamonData hamon = power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).get();
            float effectStr = (float) hamon.getHamonControlLevel() / (float) HamonData.MAX_STAT_LEVEL;
            int resistDuration = 100 + MathHelper.floor(400F * effectStr);
            int resistLvl = MathHelper.floor(2.5F * effectStr);
            hamon.hamonPointsFromAction(HamonStat.CONTROL, getManaCost());
            user.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, resistDuration, resistLvl));
            HamonPowerType.createHamonSparkParticlesEmitter(user, effectStr / 2F);
        }
    }

}
