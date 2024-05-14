package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.LivingEntity;

public class HamonMetalSilverOverdriveWeapon extends HamonOverdrive {

    public HamonMetalSilverOverdriveWeapon(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public ActionConditionResult checkHeldItems(LivingEntity user, INonStandPower power) {
        return ActionConditionResult.noMessage(MCUtil.isItemWeapon(user.getMainHandItem()));
    }
    
    @Override
    protected boolean dealDamage(ActionTarget target, LivingEntity targetEntity, float dmgAmount, LivingEntity user, INonStandPower power, HamonData hamon) {
        return DamageUtil.dealHamonDamage(targetEntity, dmgAmount, user, null, attack -> attack.hamonParticle(ModParticles.HAMON_SPARK_SILVER.get()));
    }
}
