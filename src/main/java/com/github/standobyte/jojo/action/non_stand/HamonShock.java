package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HamonShock extends HamonAction {
    
    public HamonShock(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }
    
    @Override
    public ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, INonStandPower power) {
        Entity entity = target.getEntity();
        
        boolean isLiving;
        if (entity instanceof LivingEntity) {
            LivingEntity targetLiving = (LivingEntity) entity;
            if (entity.getType() == ModEntityTypes.HAMON_MASTER.get() || ModStatusEffects.isStunned(targetLiving)) {
                return ActionConditionResult.NEGATIVE;
            }
            // not the best way to determine living mobs in other mods
            isLiving = !(targetLiving instanceof StandEntity || targetLiving instanceof ArmorStandEntity || targetLiving instanceof GolemEntity)
                    && !JojoModUtil.isUndead(targetLiving);
        }
        else {
            isLiving = false;
        }
        if (!isLiving) {
            return conditionMessage("living_mob_shock");
        }
        
        return super.checkTarget(target, user, power);
    }
    
    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, 
            int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled && world.isClientSide() && target.getEntity() != null) {
            Vector3d userPos = user.getEyePosition(1.0F);
            double distanceToTarget = JojoModUtil.getDistance(user, target.getEntity().getBoundingBox());
            Vector3d targetPos = user.getEyePosition(1.0F).add(user.getLookAngle().scale(distanceToTarget));
            Vector3d particlesPos = userPos.add(targetPos.subtract(userPos).scale(0.5));
            HamonSparksLoopSound.playSparkSound(user, particlesPos, 1.0F, true);
            CustomParticlesHelper.createHamonSparkParticles(null, particlesPos, 1);
        }
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide() && target.getType() == TargetType.ENTITY && target.getEntity() instanceof LivingEntity) {
            LivingEntity targetEntity = (LivingEntity) target.getEntity();
            HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
            float strengthLvl = hamon.getHamonStrengthLevelRatio();
            float controlLvl = hamon.getHamonControlLevelRatio();
            int duration = 60 + (int) (40 * controlLvl);
            int amplifier = (int) (strengthLvl * 0.05F);
            targetEntity.addEffect(new EffectInstance(
                    ModStatusEffects.HAMON_SHOCK.get(), duration, amplifier, false, false, true));
            HamonUtil.emitHamonSparkParticles(world, null, targetEntity.getBoundingBox().getCenter(), 1.0F);
        }
    }
    
    
    /*@Override
    public boolean cancelHeldOnGettingAttacked(INonStandPower power, DamageSource dmgSource, float dmgAmount) {
        return true;
    }*/
}
