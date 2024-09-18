package com.github.standobyte.jojo.action.non_stand;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
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
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonSkills;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClStopRebuffPacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.util.general.LazySupplier;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

// TODO first person animation
// TODO counter polish
    /*  
     *  a few ticks of the target entity stunned
     *  "!" particles
     */
// FIXME hand swing messes up the player animation
public class HamonRebuffOverdrive extends HamonAction implements IPlayerAction<HamonRebuffOverdrive.Instance, INonStandPower> {
    
    public HamonRebuffOverdrive(HamonAction.Builder builder) {
        super(builder);
    }
    
    @Override
    protected ActionConditionResult checkHeldItems(LivingEntity user, INonStandPower power) {
        if (!(MCUtil.isHandFree(user, Hand.MAIN_HAND) && MCUtil.isHandFree(user, Hand.OFF_HAND))) {
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
                if (rebuff.getPhase() == Phase.WINDUP) {
                    float cdRatio = (float) rebuff.getTick() / Instance.WINDUP_TICKS;
                    cdRatio *= cdRatio;
                    rebuff.actionCooldown = (int) (rebuff.actionCooldown * cdRatio);
                }
                rebuff.stopAction();
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
    public String getTranslationKey(INonStandPower power, ActionTarget target) {
        String key = super.getTranslationKey(power, target);
        if (getCurRebuff(power.getUser()).isPresent()) {
            key += ".cancel";
        }
        return key;
    }
    
    private final LazySupplier<ResourceLocation> cancelTex = 
            new LazySupplier<>(() -> makeIconVariant(this, "_cancel"));
    @Override
    public ResourceLocation getIconTexturePath(@Nullable INonStandPower power) {
        if (power != null && getCurRebuff(power.getUser()).isPresent()) {
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
    public void setCooldownOnUse(INonStandPower power) {} // cooldown is set at the end of the continuous action instance
    
    
    public static class Instance extends ContinuousActionInstance<Instance, INonStandPower> {
        private LivingEntity counterTarget;
        private boolean didAttack = false;
//        private boolean manualEarlyCounter = false;
        private Optional<HamonData> userHamon;
        private int actionCooldown;
        
        public Instance(LivingEntity user, PlayerUtilCap userCap, INonStandPower playerPower,
                IPlayerAction<Instance, INonStandPower> action) {
            super(user, userCap, playerPower, action);
            setPhase(Phase.WINDUP);
            
            if (user.level.isClientSide()) {
                ClientTickingSoundsHelper.playStoppableEntitySound(user, ModSounds.HAMON_SYO_CHARGE.get(), 
                        1.0F, 1.0F, false, 
                        entity -> ContinuousActionInstance.getCurrentAction(user).map(
                                playerAction -> playerAction == this && playerAction.getPhase() != Phase.RECOVERY).orElse(false),
                        PERFORM_TICKS);
            }
            
            userHamon = INonStandPower.getNonStandPowerOptional(user)
                    .resolve().flatMap(power -> power.getTypeSpecificData(ModPowers.HAMON.get()));
            actionCooldown = ((Action<INonStandPower>) action).getCooldown(playerPower, -1);
        }
        
        private static final int WINDUP_TICKS = 14;
        public static final int COUNTER_TIMING_WINDOW = 6;
//        public static final int MANUAL_TIMING_WINDOW = 4;
        private static final int PERFORM_TICKS = 10;
        private static final int RECOVERY_TICKS = 16;
        
        @Override
        public void playerTick() {
            switch (getPhase()) {
            case WINDUP:
                if (getTick() >= WINDUP_TICKS) {
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
            if (!user.level.isClientSide()) {
                if (nextPhase == Phase.PERFORM && !didAttack) {
                    doRegularAttack();
                }
                else if (nextPhase == Phase.RECOVERY && !didAttack) {
                    JojoModUtil.sayVoiceLine(user, ModSounds.JOSEPH_OH_NO.get(), null, 1, 1, 0, true);
                }
            }
            else if (user instanceof PlayerEntity) {
                setAnim((PlayerEntity) user, nextPhase);
            }
        }
        
//        public boolean setEarlyManual() {
//            if (getPhase() == Phase.WINDUP && !manualEarlyCounter) {
//                manualEarlyCounter = true;
//                tick = WINDUP_TICKS - MANUAL_TIMING_WINDOW;
//                
//                if (!user.level.isClientSide()) {
//                    PacketManager.sendToClientsTrackingAndSelf(TrPlayerContinuousActionPacket.specialPacket(user.getId(), 
//                            TrPlayerContinuousActionPacket.PacketType.REBUFF_MANUAL), user);
//                }
//                else if (user instanceof PlayerEntity) {
//                    ModPlayerAnimations.rebuffOverdrive.setManualCounterAnim((PlayerEntity) user);
//                }
//                
//                return true;
//            }
//            return false;
//        }
        
        @Override
        public boolean cancelIncomingDamage(DamageSource dmgSource, float dmgAmount) {
            boolean canCounter = DamageUtil.isMeleeAttack(dmgSource) && dmgSource.getDirectEntity() instanceof LivingEntity;
            if (canCounter) {
                if (doCounterAttack((LivingEntity) dmgSource.getDirectEntity())) {
                    setPhase(Phase.PERFORM, true);
                    return true;
                }
            }
            else {
                // TODO protection
            }
            return this.getPhase() == Phase.PERFORM;
        }
        
        public boolean isCounterTiming() {
            return getPhase() == Phase.WINDUP && getTick() >= WINDUP_TICKS - COUNTER_TIMING_WINDOW;
        }
        
        private boolean doCounterAttack(LivingEntity target) {
            if (!didAttack && getPhase() == Phase.WINDUP) {
                if (!isCounterTiming()) {
                    JojoModUtil.sayVoiceLine(user, ModSounds.JOSEPH_OH_NO.get(), null, 1, 1, 0, true);
                    stopAction();
                    return false;
                }
                
                this.counterTarget = target;
                
                JojoModUtil.sayVoiceLine(user, ModSounds.JOSEPH_REBUFF_OVERDRIVE.get());
                
                punch(counterTarget, true);
                didAttack = true;
                return true;
            }
            
            return false;
        }
        
        private boolean doRegularAttack() {
            // TODO if there is a melee mob that is REALLY close (doesn't have to be aimed at, just in a counter angle), 
            //    full counter it anyway, their ai is really f-ing annoying
            // TODO also full counter if a mob is just trying to hit but can't deal damage (you can clearly see the f-ing zombie hand swing)
            LivingEntity fullCounterAnyway = null;
            if (fullCounterAnyway != null) {
                return doCounterAttack(fullCounterAnyway);
            }
            
            // TODO aim
            LivingEntity aimTarget = null;
            if (aimTarget != null) {
                punch(aimTarget, false);
                didAttack = true;
                return true;
            }
            
            user.swing(Hand.MAIN_HAND, true);
            return false;
        }
        
        private void punch(LivingEntity target, boolean properCounter) {
            float damage = properCounter ? 6.0f : 3.0f;
            float knockback = properCounter ? 2.0f : 0.5f;
            boolean canShock = properCounter;
            float soundVolume = properCounter ? 1.0f : 0.5f;
            
            if (DamageUtil.dealHamonDamage(counterTarget, damage, user, null)) {
                counterTarget.level.playSound(null, counterTarget.getX(), counterTarget.getEyeY(), counterTarget.getZ(), 
                        ModSounds.HAMON_REBUFF_PUNCH.get(), counterTarget.getSoundSource(), soundVolume, 1.0f);
                counterTarget.knockback(knockback, user.getX() - counterTarget.getX(), user.getZ() - counterTarget.getZ());
                if (canShock && userHamon.map(hamon -> hamon.isSkillLearned(ModHamonSkills.HAMON_SHOCK.get())).orElse(false)) {
                    counterTarget.addEffect(new EffectInstance(ModStatusEffects.HAMON_SHOCK.get(), 50, 0, false, false, true));
                }
            }
            if (!properCounter) {
                // TODO low pitch swing sound
            }

            user.swing(Hand.MAIN_HAND, true);
            actionCooldown = properCounter ? 0 : actionCooldown / 2;
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
        
        @Override
        public float getWalkSpeed() {
            return 0;
        }
        
        @Override
        public boolean stopAction() {
            if (super.stopAction()) {
                if (!user.level.isClientSide()) {
                    if (actionCooldown > 0) {
                        playerPower.setCooldownTimer((Action<INonStandPower>) action, actionCooldown);
                    }
                }
                else if (user instanceof PlayerEntity) {
                    ModPlayerAnimations.rebuffOverdrive.stopAnim((PlayerEntity) user);
                }
                return true;
            }
            
            return false;
        }
        
        @Override
        protected Instance getThis() {
            return this;
        }
        
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
