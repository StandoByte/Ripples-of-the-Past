package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.action.player.IPlayerAction;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class HamonOverdriveBeat extends HamonAction implements IPlayerAction<HamonOverdriveBeat.Instance, INonStandPower> {

    public HamonOverdriveBeat(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkHeldItems(LivingEntity user, INonStandPower power) {
        if (!MCUtil.isHandFree(user, Hand.OFF_HAND)) {
            return conditionMessage("offhand");
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
    public HamonOverdriveBeat.Instance createContinuousActionInstance(
            LivingEntity user, PlayerUtilCap userCap, INonStandPower power) {
        if (user.level.isClientSide() && user instanceof PlayerEntity) {
            ModPlayerAnimations.hamonBeat.setAnimEnabled((PlayerEntity) user, true);
        }
        return new Instance(user, userCap, power, this);
    }
    
    
    @Override
    public void setCooldownOnUse(INonStandPower power) {} // cooldown is set inside the continuous action instance
    
    @Override
    protected void consumeEnergy(World world, LivingEntity user, INonStandPower power, ActionTarget target) {} // and so is energy consumption
    
    
    public static class Instance extends ContinuousActionInstance<HamonOverdriveBeat, INonStandPower> {
        private HamonData userHamon;
        
        public Instance(LivingEntity user, PlayerUtilCap userCap, 
                INonStandPower playerPower, HamonOverdriveBeat action) {
            super(user, userCap, playerPower, action);
            
            userHamon = playerPower.getTypeSpecificData(ModPowers.HAMON.get()).get();
        }
        
        @Override
        public void playerTick() {
            switch (getTick()) {
            case 2:
                if (user.level.isClientSide()) {
                    user.level.playSound(ClientUtil.getClientPlayer(), user.getX(), user.getEyeY(), user.getZ(), 
                            ModSounds.HAMON_SYO_SWING.get(), user.getSoundSource(), 1.0f, 1.5f);
                    user.swing(Hand.OFF_HAND, true);
                }
                break;
            case 5:
                if (!user.level.isClientSide()) {
                    ActionTarget target = playerPower.getMouseTarget();
                    if (target.getEntity() instanceof LivingEntity) {
                        punch((LivingEntity) target.getEntity());
                    }
                }
                break;
            case 8:
                stopAction();
                break;
            }
        }
        
        private void punch(LivingEntity target) {
            World world = user.level;
            if (!world.isClientSide()) {
                HamonOverdriveBeat hamonAction = getAction();
                if (hamonAction.checkHeldItems(user, playerPower).isPositive()) {
                    float damage = 3.0f;
                    float cost = hamonAction.getEnergyCost(playerPower, new ActionTarget(target));
                    float efficiency = userHamon.getActionEfficiency(cost, true, hamonAction.getUnlockingSkill());
                    
                    if (DamageUtil.dealHamonDamage(target, damage * efficiency, user, null)) {
                        world.playSound(null, target.getX(), target.getEyeY(), target.getZ(), ModSounds.HAMON_SYO_PUNCH.get(), target.getSoundSource(), 1F, 1.5F);
                        target.knockback(1.25F, user.getX() - target.getX(), user.getZ() - target.getZ());
                        addPointsForAction(playerPower, userHamon, HamonStat.STRENGTH, cost, efficiency);
                        playerPower.consumeEnergy(cost);
                    }
                }
                
                HamonSunlightYellowOverdrive.doMeleeAttack(user, target);
            }
            
            if (user instanceof PlayerEntity) {
                ((PlayerEntity) user).resetAttackStrengthTicker();
            }
        }
        
        @Override
        public boolean updateTarget() {
            return true;
        }
        
        
        @Override
        public float getWalkSpeed() {
            return 0.5f;
        }
        
        @Override
        public void onStop() {
            super.onStop();
            if (user.level.isClientSide() && user instanceof PlayerEntity) {
                ModPlayerAnimations.hamonBeat.setAnimEnabled((PlayerEntity) user, false);
            }
        }
        
    }
}
