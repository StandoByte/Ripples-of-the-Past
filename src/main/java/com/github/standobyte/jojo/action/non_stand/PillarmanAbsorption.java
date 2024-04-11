package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.entity.mob.HamonMasterEntity;
import com.github.standobyte.jojo.entity.mob.HungryZombieEntity;
import com.github.standobyte.jojo.init.ModCustomStats;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.vampirism.VampirismUtil;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.INPC;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.AbstractIllagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class PillarmanAbsorption extends PillarmanAction {

    public PillarmanAbsorption(NonStandAction.Builder builder) {
        super(builder.holdType());
    }
    
    private boolean canAbsorb = false;
    
    @Override
    public ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, INonStandPower power) {
        Entity entityTarget = target.getEntity();
        if (entityTarget instanceof LivingEntity) {
            return ActionConditionResult.POSITIVE;
        }
        return ActionConditionResult.NEGATIVE_CONTINUE_HOLD;
    }
    
    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (user.level.getDifficulty() == Difficulty.PEACEFUL) {
            return conditionMessage("peaceful");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
            if (!world.isClientSide() && target.getEntity() instanceof LivingEntity) {
                LivingEntity targetEntity = (LivingEntity) target.getEntity();
                if (!targetEntity.isDeadOrDying()) {
                	if(!HamonUtil.preventBlockDamage(targetEntity, user.level, null, null, DamageUtil.PILLAR_MAN_ABSORPTION, 2)){
                		float bloodAndHealModifier = GeneralUtil.getOrLast(
                                JojoModConfig.getCommonConfigInstance(false).bloodDrainMultiplier.get(), 
                                world.getDifficulty().getId()).floatValue();
                        power.addEnergy(bloodAndHealModifier);
                        canAbsorb = true;
                	} else { canAbsorb = false; }
                	if(absorb(user, targetEntity, 2)) {}
                }
            }
        }
    }

    private static final Effect[] BLOOD_DRAIN_EFFECTS = {
            Effects.MOVEMENT_SLOWDOWN,
            Effects.DIG_SLOWDOWN,
            Effects.WEAKNESS,
            Effects.CONFUSION
    };
    public static boolean absorb(LivingEntity attacker, LivingEntity target, float absorbDamage) {
        boolean hurt = false;
        if(HamonUtil.preventBlockDamage(target, attacker.level, null, null, DamageUtil.PILLAR_MAN_ABSORPTION, 2)){
    		Vector3d userPos = attacker.getEyePosition(1.0F);
            double distanceToTarget = JojoModUtil.getDistance(attacker, target.getEntity().getBoundingBox());
            Vector3d targetPos = attacker.getEyePosition(1.0F).add(attacker.getLookAngle().scale(distanceToTarget));
            Vector3d particlesPos = userPos.add(targetPos.subtract(userPos).scale(0.5));
            //The sound doesn't seem to work :(
            HamonSparksLoopSound.playSparkSound(attacker, particlesPos, 1.0F, true);
            CustomParticlesHelper.createHamonSparkParticles(null, particlesPos, 1);
    	} else {
    		hurt = DamageUtil.dealPillarmanAbsorptionDamage(target, absorbDamage, null);
    	}
        if (hurt) {
            int effectsLvl = attacker.level.getDifficulty().getId() - 1;
            if (effectsLvl >= 0) {
                for (Effect effect : BLOOD_DRAIN_EFFECTS) {
                    int duration = MathHelper.floor(20F * absorbDamage);
                    EffectInstance effectInstance = target.getEffect(effect);
                    EffectInstance newInstance = effectInstance == null ? 
                            new EffectInstance(effect, duration, effectsLvl)
                            : new EffectInstance(effect, effectInstance.getDuration() + duration, effectsLvl);
                    target.addEffect(newInstance);
                }
            }
        }
        return hurt;
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }
    
    @Override
    public double getMaxRangeSqEntityTarget() {
        return 4;
    }
    
    @Override
    public boolean isHeldSentToTracking() {
        return true;
    }
    
    @Override
    public void onHoldTickClientEffect(LivingEntity user, INonStandPower power, int ticksHeld, boolean requirementsFulfilled, boolean stateRefreshed) {
        if (stateRefreshed && requirementsFulfilled && canAbsorb == true) {
            ClientTickingSoundsHelper.playHeldActionSound(ModSounds.VAMPIRE_BLOOD_DRAIN.get(), 1.0F, 1.0F, true, user, power, this);
        }
    }
    
    @Override
    public boolean heldAllowsOtherAction(INonStandPower power, Action<INonStandPower> action) {
        return true;
    }
    
}
