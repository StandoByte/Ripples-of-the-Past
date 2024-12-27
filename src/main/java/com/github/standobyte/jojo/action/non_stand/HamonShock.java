package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.action.player.IPlayerAction;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.world.World;

public class HamonShock extends HamonAction implements IPlayerAction<HamonShock.Instance, INonStandPower> {
    
    public HamonShock(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    public ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, INonStandPower power) {
        Entity entity = target.getEntity();
        if (entity == null) {
            return ActionConditionResult.NEGATIVE;
        }

        boolean isLiving;
        if (entity instanceof LivingEntity) {
            LivingEntity targetLiving = (LivingEntity) entity;
            if (entity.getType() == ModEntityTypes.HAMON_MASTER.get() || ModStatusEffects.isStunned(targetLiving)) {
                return ActionConditionResult.NEGATIVE;
            }
            isLiving = HamonUtil.isLiving(targetLiving);
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
    public ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (power.getHeldAction() != this) {
            if (power.getEnergy() <= 0) {
                return conditionMessage("some_energy");
            }
        }
        return ActionConditionResult.POSITIVE;
    }
    
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            setPlayerAction(user, power);
        }
    }
    
    @Override
    public HamonShock.Instance createContinuousActionInstance(
            LivingEntity user, PlayerUtilCap userCap, INonStandPower power) {
        if (user.level.isClientSide() && user instanceof PlayerEntity) {
            ModPlayerAnimations.hamonShock.setAnimEnabled((PlayerEntity) user, true);
        }
        return new Instance(user, userCap, power, this);
    }
    
    
    @Override
    public void setCooldownOnUse(INonStandPower power) {}
    
    @Override
    protected void consumeEnergy(World world, LivingEntity user, INonStandPower power, ActionTarget target) {}
    
    
    public static class Instance extends ContinuousActionInstance<HamonShock, INonStandPower> {
        private LivingEntity shockedTarget;

        public Instance(LivingEntity user, PlayerUtilCap userCap, INonStandPower playerPower, HamonShock action) {
            super(user, userCap, playerPower, action);
        }
        
        @Override
        public void playerTick() {
            if (user.level.isClientSide()) {
                HamonSparksLoopSound.playSparkSound(user, user.position(), 1.0F, true);
                
//                if (shockedTarget != null) {
//                    Vector3d userPos = user.getEyePosition(1.0F);
//                    double distanceToTarget = JojoModUtil.getDistance(user, shockedTarget.getEntity().getBoundingBox());
//                    Vector3d targetPos = user.getEyePosition(1.0F).add(user.getLookAngle().scale(distanceToTarget));
//                    Vector3d particlesPos = userPos.add(targetPos.subtract(userPos).scale(0.5));
//                    CustomParticlesHelper.createHamonSparkParticles(null, particlesPos, 1);
//                }
            }
            
            if (getTick() > 10 && shockedTarget != null && !user.level.isClientSide()) {
                HamonUtil.emitHamonSparkParticles(user.level, null, shockedTarget.getBoundingBox().getCenter(), 1.0F);
            }
            switch (getTick()) {
            case 9:
                if (!user.level.isClientSide()) {
                    ActionTarget target = playerPower.getMouseTarget();
                    ActionConditionResult result = getAction().checkTarget(target, user, playerPower);
                    if (!result.isPositive()) {
                        ActionConditionResult.sendActionFailedMessage(action, result, user);
                    }
                    else if (target.getType() == TargetType.ENTITY && target.getEntity() instanceof LivingEntity) {
                        LivingEntity targetEntity = (LivingEntity) target.getEntity();
                        HamonData hamon = playerPower.getTypeSpecificData(ModPowers.HAMON.get()).get();
                        float strengthLvl = hamon.getHamonStrengthLevelRatio();
                        float controlLvl = hamon.getHamonControlLevelRatio();
                        float energyRatio = hamon.getEnergyRatio();
                        float efficiency = hamon.getActionEfficiency(0, false, getAction().getUnlockingSkill());
                        int duration = (int) (20 + (80 * controlLvl + 60 * energyRatio) * efficiency);
                        int amplifier = (int) (strengthLvl * 0.05F * efficiency);
                        hamon.hamonPointsFromAction(HamonStat.CONTROL, playerPower.getEnergy() * efficiency);
                        playerPower.setEnergy(0);
                        shockedTarget = targetEntity;
                        targetEntity.addEffect(new EffectInstance(
                                ModStatusEffects.HAMON_SHOCK.get(), duration, amplifier, false, false, true));
                        HamonUtil.emitHamonSparkParticles(user.level, null, targetEntity.getBoundingBox().getCenter(), 1.0F);
                    }
                }
                break;
            case 15:
                stopAction();
                break;
            }
        }
        
        @Override
        public boolean updateTarget() {
            return true;
        }
        
        @Override
        public float getWalkSpeed() {
            return 0.25f;
        }
        
        @Override
        public void onStop() {
            super.onStop();
            if (user.level.isClientSide() && user instanceof PlayerEntity) {
                ModPlayerAnimations.hamonShock.setAnimEnabled((PlayerEntity) user, false);
            }
        }
        
    }
    
}
