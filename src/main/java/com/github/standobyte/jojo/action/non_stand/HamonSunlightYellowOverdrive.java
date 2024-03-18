package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
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
        super(builder.needsFreeMainHand());
    }
    
    @Override
    public ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (power.getHeldAction() != this) {
            if (power.getEnergy() < power.getMaxEnergy()) {
                return conditionMessage("full_energy");
            }
        }
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    protected ActionConditionResult checkEnergy(LivingEntity user, INonStandPower power, ActionTarget target) {
        return ActionConditionResult.POSITIVE;
    }
    
    @Override
    public boolean holdOnly(INonStandPower power) {
        return false;
    }
    
    @Override
    public float getHeldTickEnergyCost(INonStandPower power) {
        return Math.min(power.getMaxEnergy() / Math.max(getHoldDurationToFire(power), 1), power.getEnergy());
    }
    
    @Override
    public void startedHolding(World world, LivingEntity user, INonStandPower power, ActionTarget target, boolean requirementsFulfilled) {
        if (requirementsFulfilled && world.isClientSide()) {
            ClientTickingSoundsHelper.playStoppableEntitySound(user, ModSounds.HAMON_SYO_CHARGE.get(), 
                    1.0F, 1.0F, false, entity -> power.getHeldAction() == this);
        }
    }
    
    @Override
    public void stoppedHolding(World world, LivingEntity user, INonStandPower power, int ticksHeld, boolean willFire) {
        if (!willFire && !world.isClientSide() && !power.isUserCreative()) {
            float holdRatio = (float) ticksHeld / (float) Math.max(getHoldDurationToFire(power), 1);
            float energySpent = power.getMaxEnergy() * holdRatio;
            power.setEnergy(Math.min(power.getMaxEnergy(), power.getEnergy() + energySpent));
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
                forPerform(world, user, power, target);
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
        else {
            user.playSound(ModSounds.HAMON_SYO_SWING.get(), 1.0F, 1.0F);
        }
    }
    
    public void forPerform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        Entity entity = target.getEntity();
        LivingEntity targetEntity = (LivingEntity) entity;
        HamonData hamon = power.getTypeSpecificData(ModPowers.HAMON.get()).get();
        int maxTicks = Math.max(getHoldDurationToFire(power), 1);
        int ticksHeld = Math.min(power.getHeldActionTicks(), maxTicks);
        float holdRatio = (float) ticksHeld / (float) maxTicks;
        
        float efficiency = hamon.getActionEfficiency(0, true);
        
        float damage = 2.5F + 7.5F * holdRatio;
        damage *= efficiency;

        if (DamageUtil.dealHamonDamage(targetEntity, damage, user, null, attack -> attack.hamonParticle(ModParticles.HAMON_SPARK_YELLOW.get()))) {
            if (holdRatio > 0.25F) {
                world.playSound(null, targetEntity.getX(), targetEntity.getEyeY(), targetEntity.getZ(), ModSounds.HAMON_SYO_PUNCH.get(), targetEntity.getSoundSource(), holdRatio, 1.0F);
            }
            hamon.hamonPointsFromAction(HamonStat.STRENGTH, power.getMaxEnergy() * holdRatio * efficiency);
        }
    }
    
    @Override
    public boolean isHeldSentToTracking() {
        return true;
    }
}
