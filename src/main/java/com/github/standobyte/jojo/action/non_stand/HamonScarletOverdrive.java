package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class HamonScarletOverdrive extends HamonOverdrive {

    public HamonScarletOverdrive(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected Action<INonStandPower> replaceAction(INonStandPower power, ActionTarget target) {
        return this;
    }

    @Override
    protected float getDamage() {
        return 0.6F;
    }

    @Override
    protected boolean dealDamage(LivingEntity target, float dmgAmount, LivingEntity user, INonStandPower power, HamonData hamon) {
        return DamageUtil.dealDamageAndSetOnFire(target, 
                entity -> DamageUtil.dealHamonDamage(entity, dmgAmount, user, null, attack -> attack.hamonParticle(ModParticles.HAMON_SPARK_RED.get())), 
                MathHelper.floor(2 + 8F * (float) hamon.getHamonStrengthLevel() / (float) HamonData.MAX_STAT_LEVEL * hamon.getActionEfficiency(getEnergyCost(power))), false);
    }
}
