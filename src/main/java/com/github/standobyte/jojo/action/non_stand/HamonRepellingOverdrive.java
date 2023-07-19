package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class HamonRepellingOverdrive extends HamonAction {

    public HamonRepellingOverdrive(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
            float effectStr = (float) hamon.getHamonControlLevel() / (float) HamonData.MAX_STAT_LEVEL * hamon.getActionEfficiency(getEnergyCost(power, target), false);
            int resistDuration = 100 + MathHelper.floor(400F * effectStr);
            int resistLvl = MathHelper.floor(1.5F * effectStr);
            hamon.hamonPointsFromAction(HamonStat.CONTROL, getEnergyCost(power, target));
            user.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, resistDuration, resistLvl));
            HamonPowerType.createHamonSparkParticlesEmitter(user, effectStr / 2F);
        }
    }

}
