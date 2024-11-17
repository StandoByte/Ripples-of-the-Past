package com.github.standobyte.jojo.action.non_stand;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.action.player.IPlayerAction;
import com.github.standobyte.jojo.action.stand.StandEntityAction.Phase;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.playeranim.anim.ModPlayerAnimations;
import com.github.standobyte.jojo.client.sound.ClientTickingSoundsHelper;
import com.github.standobyte.jojo.client.sound.HamonSparksLoopSound;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClStopRebuffPacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.general.LazySupplier;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mc.damage.IStandDamageSource;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

// TODO first person animation
// TODO counter polish
public class HamonRebuffOverdrive extends HamonAction implements IPlayerAction<HamonRebuffOverdrive.Instance, INonStandPower> {
    
    public HamonRebuffOverdrive(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkHeldItems(LivingEntity user, INonStandPower power) {
        if (!MCUtil.areHandsFree(user, Hand.MAIN_HAND, Hand.OFF_HAND)) {
            return conditionMessage("hands");
        }
        return ActionConditionResult.POSITIVE;
    }
    
    

    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        Optional<HamonRebuffOverdrive.Instance> curRebuff = getCurRebuff(user);
        if (curRebuff.isPresent()) {
            HamonRebuffOverdrive.Instance rebuff = curRebuff.get();
            if (!world.isClientSide()) {
                if (rebuff.canCancel()) {
                    rebuff.cancel();
                }
            }
        }
        else {
            if (!world.isClientSide()) {
                setPlayerAction(user, power);
            }
            else if (user == ClientUtil.getClientPlayer() && user.getRandom().nextInt(5) == 0) {
                ClientTickingSoundsHelper.playVoiceLine(user, ModSounds.JOSEPH_GIGGLE.get(), user.getSoundSource(), 1, 1, false);
            }
        }
    }
    
    @Override
    public HamonRebuffOverdrive.Instance createContinuousActionInstance(
            LivingEntity user, PlayerUtilCap userCap, INonStandPower power) {
        return new Instance(user, userCap, power, this);
    }
    
    
    @Override
    protected boolean canBeUsedDuringPlayerAction(ContinuousActionInstance<?, ?> curPlayerAction) {
        return curPlayerAction.getAction() == this && ((HamonRebuffOverdrive.Instance) curPlayerAction).canCancel();
    }
    
    @Override
    public String getTranslationKey(INonStandPower power, ActionTarget target) {
        String key = super.getTranslationKey(power, target);
        if (getCurRebuff(power.getUser()).map(rebuff -> rebuff.canCancel()).orElse(false)) {
            key += ".cancel";
        }
        return key;
    }
    
    private final LazySupplier<ResourceLocation> cancelTex = 
            new LazySupplier<>(() -> makeIconVariant(this, "_cancel"));
    @Override
    public ResourceLocation getIconTexturePath(@Nullable INonStandPower power) {
        if (power != null && getCurRebuff(power.getUser()).map(rebuff -> rebuff.canCancel()).orElse(false)) {
            return cancelTex.get();
        }
        else {
            return super.getIconTexturePath(power);
        }
    }
    
    @Override
    public boolean greenSelection(INonStandPower power, ActionConditionResult conditionCheck) {
        return getCurRebuff(power.getUser()).isPresent();
    }
    
    public static Optional<HamonRebuffOverdrive.Instance> getCurRebuff(LivingEntity user) {
        return ContinuousActionInstance.getCurrentAction(user)
                .filter(action -> action.getAction() == ModHamonActions.JOSEPH_REBUFF_OVERDRIVE.get())
                .map(action -> (HamonRebuffOverdrive.Instance) action);
    }
    
    
    @Override
    public void setCooldownOnUse(INonStandPower power) {}
    
    @Override
    protected void consumeEnergy(World world, LivingEntity user, INonStandPower power, ActionTarget target) {}
    
    
    public static class Instance extends ContinuousActionInstance<HamonRebuffOverdrive, INonStandPower> {
        private static final AttributeModifier NO_KNOCKBACK = new AttributeModifier(UUID.fromString("c10b7337-504f-4c92-af39-232626b9818d"), 
                "Rebuff overdrive full knockback resistance", 1, AttributeModifier.Operation.ADDITION);
        private LivingEntity counterTarget;
        private DamageSource reduceDamage;
        private boolean canAttack = true;
        private boolean didAttack = false;
        private HamonData userHamon;
        
        public Instance(LivingEntity user, PlayerUtilCap userCap, 
                INonStandPower playerPower, HamonRebuffOverdrive action) {
            super(user, userCap, playerPower, action);
            
            userHamon = playerPower.getTypeSpecificData(ModPowers.HAMON.get()).get();
        }
        
        @Override
        public void onStart() {
            super.onStart();
            setPhase(Phase.WINDUP);
            if (!user.level.isClientSide()) {
                MCUtil.applyAttributeModifier(user, Attributes.KNOCKBACK_RESISTANCE, NO_KNOCKBACK);
            }
            else {
                ClientTickingSoundsHelper.playStoppableEntitySound(user, ModSounds.HAMON_SYO_CHARGE.get(), 
                        1.0F, 1.0F, false, 
                        entity -> ContinuousActionInstance.getCurrentAction(user).map(
                                playerAction -> playerAction == this && playerAction.getPhase() != Phase.RECOVERY).orElse(false),
                        PERFORM_TICKS);
            }
        }
        
        @Override
        public void onStop() {
            super.onStop();
            if (!user.level.isClientSide()) {
                MCUtil.removeAttributeModifier(user, Attributes.KNOCKBACK_RESISTANCE, NO_KNOCKBACK);
            }
            else if (user instanceof PlayerEntity) {
                ModPlayerAnimations.rebuffOverdrive.stopAnim((PlayerEntity) user);
            }
        }
        
        private static final int WINDUP_TICKS = 14;
        public static final int COUNTER_TIMING_WINDOW = 7;
        private static final int PERFORM_TICKS = 10;
        private static final int RECOVERY_TICKS = 16;
        
        @Override
        public void playerTick() {
            if (!getAction().checkHeldItems(user, playerPower).isPositive()) {
                canAttack = false;
            }
            
            switch (getPhase()) {
            case WINDUP:
                if (!user.level.isClientSide() && !canAttack) {
                    cancel();
                    return;
                }
                if (getTick() == WINDUP_TICKS - 3) {
                    swing();
                }
                else if (getTick() >= WINDUP_TICKS) {
                    setPhase(Phase.PERFORM);
                }
                break;
            case PERFORM:
                if (getTick() >= PERFORM_TICKS) {
                    setPhase(Phase.RECOVERY);
                }
                break;
            case RECOVERY:
                if (getTick() >= RECOVERY_TICKS) {
                    stopAction();
                }
                break;
            default:
                throw new IllegalStateException();
            }
            
            if (user.level.isClientSide() && isCounterTiming()) {
                HamonSparksLoopSound.playSparkSound(user, user.position(), 1.0F);
            }
        }
        
        @Override
        protected void onPhaseSet(Phase oldPhase, Phase nextPhase) {
            switch (nextPhase) {
            case PERFORM:
                if (!didAttack) {
                    if (!user.level.isClientSide()) {
                        // TODO if there is a melee mob that is REALLY close (doesn't have to be aimed at, just in a counter angle), 
                        //    full counter it anyway, their ai is really f-ing annoying
                        // TODO also full counter if a mob is just trying to hit but can't deal damage (you can clearly see the f-ing zombie hand swing)
                        LivingEntity fullCounterAnyway = null;
                        if (fullCounterAnyway != null) {
                            doCounterAttack(fullCounterAnyway);
                        }
                        
                        ActionTarget target = playerPower.getMouseTarget();
                        if (target.getEntity() instanceof LivingEntity) {
                            punch((LivingEntity) target.getEntity(), false);
                        }
                    }
                }
                break;
//            case RECOVERY:
////                if (!user.level.isClientSide() && !didAttack) {
////                    JojoModUtil.sayVoiceLine(user, ModSounds.JOSEPH_OH_NO.get(), null, 1, 1, 0, true); // nah
////                }
//                break;
            default:
                break;
            }
            
            if (user.level.isClientSide() && user instanceof PlayerEntity) {
                setAnim((PlayerEntity) user, nextPhase);
            }
        }
        
        
        @Override
        public boolean cancelIncomingDamage(DamageSource dmgSource, float dmgAmount) {
            LivingEntity meleeAttacker = DamageUtil.getMeleeAttacker(dmgSource);
            boolean canCounterStand = isUsingHermitPurple(user);
            
            if (getPhase() == Phase.PERFORM) {
                return true;
            }
            
            if (meleeAttacker != null && DamageUtil.isShieldBlockAngle(user, dmgSource)
                    && (canCounterStand || !(dmgSource instanceof IStandDamageSource))) {
                boolean counterTiming = isCounterTiming();
                LivingEntity dealDamageTo = (LivingEntity) dmgSource.getDirectEntity();
                HamonRebuffOverdrive hamonAction = getAction();
                
                float energyCost = hamonAction.getEnergyCost(playerPower, new ActionTarget(dealDamageTo));
                float efficiency = userHamon.getActionEfficiency(energyCost, true, hamonAction.getUnlockingSkill());
                boolean tooMuchDamage = !(efficiency == 1 || efficiency >= dmgAmount / user.getMaxHealth());
                
                if (counterTiming && !tooMuchDamage) {
                    if (canCounterStand && dealDamageTo instanceof StandEntity && ((StandEntity) dealDamageTo).transfersDamage()) {
                        dealDamageTo = StandUtil.getStandUser(dealDamageTo);
                    }
                    if (doCounterAttack(dealDamageTo)) {
                        setPhase(Phase.PERFORM, true);
                        return true;
                    }
                }
                else {
                    if (!counterTiming) {
                        JojoModUtil.sayVoiceLine(user, ModSounds.JOSEPH_OH_NO.get(), null, 1, 1, 0, true);
                    }
                    else {
                        swing();
                    }
                    stopAction();
                    return false;
                }
            }
            else {
                this.reduceDamage = dmgSource;
            }
            
            return super.cancelIncomingDamage(dmgSource, dmgAmount);
        }
        
        public float reduceDamageAmount(DamageSource dmgSource, float dmgAmount) {
            if (this.reduceDamage == dmgSource) {
                float amount = ModHamonActions.HAMON_PROTECTION.get().reduceDamageAmount(playerPower, user, dmgSource, dmgAmount);
                reduceDamage = null;
                return amount;
            }
            
            return dmgAmount;
        }
        
        public boolean isCounterTiming() {
            return getPhase() == Phase.WINDUP && getTick() >= WINDUP_TICKS - COUNTER_TIMING_WINDOW;
        }
        
        public boolean addSparksThisTick() {
            return getPhase() == Phase.WINDUP && getTick() >= WINDUP_TICKS - COUNTER_TIMING_WINDOW + 1;
        }
        
        private boolean doCounterAttack(LivingEntity target) {
            if (!didAttack && getPhase() == Phase.WINDUP) {
                this.counterTarget = target;
                JojoModUtil.sayVoiceLine(user, ModSounds.JOSEPH_REBUFF_OVERDRIVE.get());
                punch(counterTarget, true);
                return true;
            }
            
            return false;
        }
        
        
        private void punch(LivingEntity target, boolean properCounter) {
            if (!canAttack) return;
            
            if (!user.level.isClientSide()) {
                HamonRebuffOverdrive hamonAction = getAction();
                if (hamonAction.checkHeldItems(user, playerPower).isPositive()) {
                    float energyCost = ((NonStandAction) action).getEnergyCost(playerPower, new ActionTarget(target));
                    float efficiency = userHamon.getActionEfficiency(energyCost, true, hamonAction.getUnlockingSkill());
                    
                    float damage = properCounter ? 10.0f : 6.0f;
                    float knockback = properCounter ? 2.0f : 1.0f;
                    boolean canShock = properCounter;
                    
                    if (DamageUtil.dealHamonDamage(target, damage * efficiency, user, null)) {
                        if (properCounter) {
                            target.level.playSound(null, target.getX(), target.getEyeY(), target.getZ(), 
                                    ModSounds.HAMON_REBUFF_PUNCH.get(), target.getSoundSource(), 1.0f, 1.0f);
                        }
                        target.knockback(knockback, user.getX() - target.getX(), user.getZ() - target.getZ());
                        if (canShock && userHamon.isSkillLearned(ModHamonSkills.HAMON_SHOCK.get())) {
                            target.addEffect(new EffectInstance(ModStatusEffects.HAMON_SHOCK.get(), 50, 0, false, false, true));
                        }
                        
                        playerPower.consumeEnergy(energyCost);
                        userHamon.hamonPointsFromAction(HamonStat.STRENGTH, energyCost * efficiency);
                    }
                }
                
                HamonSunlightYellowOverdrive.doMeleeAttack(user, target);
                swing();
            }
            
            if (user instanceof PlayerEntity) {
                ((PlayerEntity) user).resetAttackStrengthTicker();
            }
            didAttack = true;
            actionCooldown = properCounter ? 0 : actionCooldown / 2;
        }
        
        private boolean didSwing = false;
        private void swing() {
            if (!didSwing && !user.level.isClientSide()) {
                user.level.playSound(null, user.getX(), user.getEyeY(), user.getZ(), 
                        ModSounds.HAMON_SYO_SWING.get(), user.getSoundSource(), 1.0f, 0.5f);
                user.swing(Hand.MAIN_HAND, true);
                didSwing = true;
            }
        }
        
        @Override
        public boolean updateTarget() {
            return true;
        }
        
        
        public boolean canCancel() {
            return getPhase() == Phase.WINDUP;
        }
        
        public void cancel() {
            if (getPhase() == Phase.WINDUP) {
                float cdRatio = (float) getTick() / Instance.WINDUP_TICKS;
                cdRatio *= cdRatio;
                actionCooldown = (int) (actionCooldown * cdRatio);
                stopAction();
            }
        }
        
        @Override
        public float getWalkSpeed() {
            return 0;
        }
        
        private void setAnim(PlayerEntity abstrClientPlayer, Phase phase) {
            switch (phase) {
            case WINDUP:
                ModPlayerAnimations.rebuffOverdrive.setWindupAnim((PlayerEntity) user);
                break;
            case PERFORM:
                ModPlayerAnimations.rebuffOverdrive.setAttackAnim((PlayerEntity) user);
                break;
            default:
                break;
            }
        }
        
    }
    
    private static boolean isUsingHermitPurple(LivingEntity user) {
        return IStandPower.getStandPowerOptional(user).map(power -> {
            if (power.hasPower() && power.isActive()) {
                return /*power.getType() == ModStands.HERMIT_PURPLE.get() || */
                        power.getType().getRegistryName().getPath().equals("hermito_purple");
            }
            return false;
        }).orElse(false);
    }
    
    public static void onWASDInput(LivingEntity user) {
        getCurRebuff(user).ifPresent(rebuff -> {
            if (user.level.isClientSide()) {
                if (rebuff.didAttack || rebuff.getPhase() == Phase.RECOVERY) {
                    rebuff.stopAction();
                    PacketManager.sendToServer(new ClStopRebuffPacket());
                }
            }
            else {
                rebuff.stopAction();
            }
        });
    }
    
}
