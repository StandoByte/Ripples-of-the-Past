package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class HamonSunlightYellowOverdrive extends HamonAction {

    public HamonSunlightYellowOverdrive(HamonAction.Builder builder) {
        super(builder.emptyMainHand());
    }
    
    @Override
    public ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (power.getHeldAction() != this) {
            if (power.getEnergy() < power.getMaxEnergy()) {
                return conditionMessage("full_energy");
            }
            if (user instanceof PlayerEntity && ((PlayerEntity) user).getAttackStrengthScale(0.5F) < 0.9F) {
                return ActionConditionResult.NEGATIVE;
            }
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public int getHoldDurationToFire(INonStandPower power) {
        int maxTicks = getHoldDurationMax(power);
        if (maxTicks == 0) return 0;
        int ret = MathHelper.ceil(ModHamonActions.HAMON_OVERDRIVE.get().getEnergyCost(power) * maxTicks / power.getMaxEnergy());
        return Math.min(ret, maxTicks);
    }
    
    @Override
    public int getHoldDurationMax(INonStandPower power) {
        int ticks = super.getHoldDurationMax(power);
        if (ticks == 0) return 0;
        float control = power.getTypeSpecificData(ModPowers.HAMON.get()).map(HamonData::getHamonControlLevelRatio).orElse(0F);
        return Math.max((int) ((float) ticks * (1 - control * 0.25F)), 1);
    }
    
    @Override
    public boolean holdOnly(INonStandPower power) {
        return false;
    }
    
    @Override
    public float getHeldTickEnergyCost(INonStandPower power) {
        return power.getMaxEnergy() / Math.max(getHoldDurationMax(power), 1);
    }
    
    @Override
    public void startedHolding(World world, LivingEntity user, INonStandPower power, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled && world.isClientSide()) {
            ClientTickingSoundsHelper.playStoppableEntitySound(user, ModSounds.HAMON_SYO_CHARGE.get(), 
                    1.0F, 1.0F, false, entity -> power.getHeldAction() != this);
        }
    }
    
    @Override
    public void stoppedHolding(World world, LivingEntity user, INonStandPower power, int ticksHeld, boolean willFire) {
        if (!world.isClientSide() && !power.isUserCreative()) {
            float holdRatio = (float) ticksHeld / (float) Math.max(getHoldDurationMax(power), 1);
            power.setEnergy(Math.min(power.getEnergy(), power.getMaxEnergy() * (1F - holdRatio)));
        }
    }
    
    
    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        Entity entity = target.getEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity targetEntity = (LivingEntity) entity;
            
            PlayerEntity playerUser = user instanceof PlayerEntity ? ((PlayerEntity) user) : null;
            if (playerUser != null) {
                CommonReflection.setAttackStrengthTicker(user, MathHelper.ceil(playerUser.getCurrentItemAttackStrengthDelay()));
            }
            
            if (!world.isClientSide()) {
                HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
                float holdRatio = (float) power.getHeldActionTicks() / (float) Math.max(getHoldDurationMax(power), 1);
                if (!power.isUserCreative()) {
                    power.setEnergy(Math.max(power.getEnergy(), power.getMaxEnergy() * (1F - holdRatio)));
                }
                float efficiency = hamon.getHamonEfficiency(0);
                
                float damage = 3F + 12F * holdRatio;
                damage *= efficiency;

                if (DamageUtil.dealHamonDamage(targetEntity, damage, user, null, attack -> attack.hamonParticle(ModParticles.HAMON_SPARK_YELLOW.get()))) {
                    if (holdRatio > 0.25F) {
                        world.playSound(null, targetEntity.getX(), targetEntity.getEyeY(), targetEntity.getZ(), ModSounds.HAMON_SYO_PUNCH.get(), targetEntity.getSoundSource(), holdRatio, 1.0F);
                    }
                    hamon.hamonPointsFromAction(HamonStat.STRENGTH, getHeldTickEnergyCost(power) * holdRatio * efficiency);
                }
            }

            if (playerUser != null) {
                playerUser.attack(targetEntity);
            }
            else if (!world.isClientSide()) {
                user.doHurtTarget(targetEntity);
            }

            if (world.isClientSide()) {
                user.swing(Hand.MAIN_HAND);
            }
        }
    }

    @Override
    public boolean isHeldSentToTracking() {
        return true;
    }
    
}
