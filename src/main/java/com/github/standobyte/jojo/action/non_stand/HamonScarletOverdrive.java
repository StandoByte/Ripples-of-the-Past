package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;
import com.github.standobyte.jojo.util.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class HamonScarletOverdrive extends HamonAction {

    public HamonScarletOverdrive(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            if (target.getType() == TargetType.ENTITY) {
                HamonData hamon = power.getTypeSpecificData(ModNonStandPowers.HAMON.get()).get();
                Entity targetEntity = target.getEntity();
                if (DamageUtil.dealDamageAndSetOnFire(targetEntity, 
                        entity -> DamageUtil.dealHamonDamage(entity, 0.1F, user, null), 
                        MathHelper.floor(2 + 8F * (float) hamon.getHamonStrengthLevel() / (float) HamonData.MAX_STAT_LEVEL * hamon.getBloodstreamEfficiency()), false)) {
                    hamon.hamonPointsFromAction(HamonStat.STRENGTH, getEnergyCost(power));
                }
                user.doHurtTarget(targetEntity);
            }
        }
    }
}
