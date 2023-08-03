package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.capability.entity.LivingUtilCap;
import com.github.standobyte.jojo.capability.entity.LivingUtilCap.HypnosisTargetCheck;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.client.particle.custom.CustomParticlesHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.potion.HypnosisEffect;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HamonHypnosis extends HamonAction {
    
    public HamonHypnosis(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }
    
    @Override
    public ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, INonStandPower power) {
        if (target.getEntity() instanceof LivingEntity) {
            HypnosisTargetCheck check = LivingUtilCap.canBeHypnotized((LivingEntity) target.getEntity(), user);
            switch (check) {
            case CORRECT:
                return ActionConditionResult.POSITIVE;
            case INVALID:
                return conditionMessage("hypnosis");
            case ALREADY_TAMED_BY_USER:
                return conditionMessage("already_tamed");
            }
        }
        return conditionMessage("hypnosis");
    }
    
    @Override
    protected void holdTick(World world, LivingEntity user, INonStandPower power, 
            int ticksHeld, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled) {
            if (!world.isClientSide()) {
                target.getEntity().getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.startedHypnosisProcess(user);
                });
            }
            else if (target.getEntity() != null) {
                Vector3d userPos = user.getEyePosition(1.0F);
                double distanceToTarget = JojoModUtil.getDistance(user, target.getEntity().getBoundingBox());
                Vector3d targetPos = user.getEyePosition(1.0F).add(user.getLookAngle().scale(distanceToTarget));
                Vector3d particlesPos = userPos.add(targetPos.subtract(userPos).scale(0.5));
                HamonSparksLoopSound.playSparkSound(user, particlesPos, 1.0F, true);
                CustomParticlesHelper.createHamonSparkParticles(null, particlesPos, 1);
            }
        }
    }
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide() && target.getType() == TargetType.ENTITY) {
            HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
            float controlLvl = hamon.getHamonControlLevelRatio();
            int duration = (int) (controlLvl * controlLvl * 24000);
            if (duration > 0) {
                HypnosisEffect.hypnotizeEntity((LivingEntity) target.getEntity(), user, duration);
            }
        }
    }
    
    // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! add proper energy check
    
    /* FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! to add
     *  spark visuals on cast
     *  spark visuals on hypnotized entities
     */
    
    @Override
    public boolean cancelHeldOnGettingAttacked(INonStandPower power, DamageSource dmgSource, float dmgAmount) {
        return true;
    }
}
