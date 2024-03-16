package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class HamonScarletOverdrive extends HamonSunlightYellowOverdrive {

    public HamonScarletOverdrive(HamonAction.Builder builder) {
        super(builder.needsFreeMainHand());
    }
    
    
    
    @Override 
    public void forPerform(World world, LivingEntity user, INonStandPower power, ActionTarget target){
        Entity entity = target.getEntity();
        LivingEntity targetEntity = (LivingEntity) entity;
        HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
        int maxTicks = Math.max(getHoldDurationToFire(power), 1);
        int ticksHeld = Math.min(power.getHeldActionTicks(), maxTicks);
        float holdRatio = (float) ticksHeld / (float) maxTicks;
        
        float efficiency = hamon.getActionEfficiency(0, true);
        
        float damage = 1.5F + 3.5F * holdRatio;
        damage *= efficiency;

        if (DamageUtil.dealHamonDamage(targetEntity, damage, user, null, attack -> attack.hamonParticle(ModParticles.HAMON_SPARK_RED.get()))) {
            if (holdRatio > 0.25F) {
                DamageUtil.setOnFire(targetEntity, MathHelper.floor(2 + 8F * (float) hamon.getHamonStrengthLevel() / 
                        (float) HamonData.MAX_STAT_LEVEL * hamon.getActionEfficiency(getEnergyCost(power, target), true)), false);
                world.playSound(null, targetEntity.getX(), targetEntity.getEyeY(), targetEntity.getZ(), ModSounds.HAMON_SYO_PUNCH.get(), targetEntity.getSoundSource(), holdRatio, 1.0F);
            }
            hamon.hamonPointsFromAction(HamonStat.STRENGTH, power.getMaxEnergy() * holdRatio * efficiency);
        }
    }
}
