package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class HamonOverdrive extends HamonAction {

    public HamonOverdrive(HamonAction.Builder builder) {
        super(builder.withUserPunch());
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide() && target.getType() == TargetType.ENTITY) {
            Entity entity = target.getEntity();
            if (entity instanceof LivingEntity) {
                LivingEntity targetEntity = (LivingEntity) entity;
                HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
                float cost = getEnergyCost(power, target);
                float efficiency = hamon.getActionEfficiency(cost, true);
                boolean shift = isShiftVariation();
                
                int attackStrengthTicker = CommonReflection.getAttackStrengthTicker(user);
                if (dealDamage(target, targetEntity, getDamage() * efficiency, user, power, hamon)) {
                	if(shift==true) {
                		world.playSound(null, targetEntity.getX(), targetEntity.getEyeY(), targetEntity.getZ(), ModSounds.HAMON_SYO_PUNCH.get(), targetEntity.getSoundSource(), 1F, 1.5F);
                		targetEntity.knockback(1.75F, user.getX()-targetEntity.getX(), user.getZ()-targetEntity.getZ());
                	}
                    addPointsForAction(power, hamon, HamonStat.STRENGTH, cost, efficiency);
                }
                // FIXME !! (hamon) ClientPlayerEntity#swing sends the packet to server, and THEN ServerPlayerEntity#swing resets attackStrengthTicker
                CommonReflection.setAttackStrengthTicker(user, attackStrengthTicker);
            }
        }
    }
    
    protected float getDamage() {
        return 2.0F;
    }
    
    protected boolean dealDamage(ActionTarget target, LivingEntity targetEntity, float dmgAmount, LivingEntity user, INonStandPower power, HamonData hamon) {
    	boolean shift = isShiftVariation();
    	dmgAmount = shift? 1.5F * dmgAmount : dmgAmount;
        return DamageUtil.dealHamonDamage(targetEntity, dmgAmount, user, null);
    }
}
