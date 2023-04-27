package com.github.standobyte.jojo.power;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap.OneTimeNotification;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.command.JojoControlsCommand;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.LeapCooldownPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrCooldownPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrHeldActionPacket;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.general.Container;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;

public abstract class PowerBaseImpl<P extends IPower<P, T>, T extends IPowerType<P, T>> implements IPower<P, T> {
    @Nonnull
    protected final LivingEntity user;
    protected final Optional<ServerPlayerEntity> serverPlayerUser;
    protected List<Action<P>> attacks = new ArrayList<>();
    protected List<Action<P>> abilities = new ArrayList<>();
    private ActionCooldownTracker cooldowns = new ActionCooldownTracker();
    private int leapCooldown;
    protected HeldActionData<P> heldActionData;
    private long lastTickedDay = -1;

    public PowerBaseImpl(LivingEntity user) {
        this.user = user;
        this.serverPlayerUser = user instanceof ServerPlayerEntity ? Optional.of((ServerPlayerEntity) user) : Optional.empty();
    }
    
    protected boolean canGetPower(T type) {
        return type != null && (!hasPower() || getType().isReplaceableWith(type));
    }

    protected void onNewPowerGiven(T type) {
        leapCooldown = getLeapCooldownPeriod();
        serverPlayerUser.ifPresent(player -> {
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.sendNotification(OneTimeNotification.POWER_CONTROLS, 
                        new TranslationTextComponent("jojo.chat.controls.message", 
                                new StringTextComponent("/" + JojoControlsCommand.LITERAL)
                                .withStyle((style) -> style
                                        .withColor(TextFormatting.GREEN)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + JojoControlsCommand.LITERAL))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("jojo.chat.controls.tooltip"))))));
            });
            ModCriteriaTriggers.GET_POWER.get().trigger(player, getPowerClassification(), this);
        });
    }
    
    protected void onPowerSet(T type) {
        if (type != null) {
            attacks = new ArrayList<>(Arrays.asList(type.getAttacks()));
            abilities = new ArrayList<>(Arrays.asList(type.getAbilities()));
        }
        else {
            attacks = Collections.emptyList();
            abilities = Collections.emptyList();
        }
    }

    @Override
    public boolean clear() {
        if (!hasPower()) {
            return false;
        }
        return true;
    }
    
    @Override
    public final LivingEntity getUser() {
        return user;
    }

    @Override
    public boolean isUserCreative() {
        return user instanceof PlayerEntity && ((PlayerEntity) user).abilities.instabuild;
    }

    @Override
    public void tick() {
        if (hasPower()) {
            tickHeldAction();
            tickCooldown();
            if (leapCooldown > 0) {
                leapCooldown--;
            }
            getType().tickUser(getUser(), getThis());
        }
        newDayCheck();
    }

    private void newDayCheck() {
    	if (user != null) {
	        long day = user.level.getDayTime() / 24000;
	        long prevDay = lastTickedDay;
	        lastTickedDay = day;
	        if (prevDay == -1) {
	        	return;
	        }
	        // FIXME (?) ticks for offline players on servers?
	        if (prevDay != day) {
	        	onNewDay(prevDay, day);
	        }
    	}
    }
    
    protected void onNewDay(long prevDay, long day) {
    	if (hasPower()) {
    		getType().onNewDay(user, getThis(), prevDay, day);
    	}
    }
    
    @Override
    public List<Action<P>> getAttacks() {
        return attacks;
    }

    @Override
    public List<Action<P>> getAbilities() {
        return abilities;
    }
    
    @Override
    public final boolean isActionOnCooldown(Action<?> action) {
        return cooldowns.isOnCooldown(action);
    }

    @Override
    public final float getCooldownRatio(Action<?> action, float partialTick) {
        return cooldowns.getCooldownPercent(action, partialTick);
    }

    @Override
    public void setCooldownTimer(Action<?> action, int value) {
        updateCooldownTimer(action, value, value);
        if (!user.level.isClientSide()) {
            PacketManager.sendToClientsTrackingAndSelf(new TrCooldownPacket(user.getId(), getPowerClassification(), action, value), user);
        }
    }

    @Override
    public final void updateCooldownTimer(Action<?> action, int value, int totalCooldown) {
        cooldowns.addCooldown(action, value, totalCooldown);
    }
    
    @Override
    public void resetCooldowns() {
        cooldowns.resetCooldowns();
        if (!user.level.isClientSide()) {
            PacketManager.sendToClientsTrackingAndSelf(TrCooldownPacket.resetAll(user.getId(), getPowerClassification()), user);
        }
    }
    
    @Override
    public ActionCooldownTracker getCooldowns() {
        return cooldowns;
    }

    private void tickCooldown() {
        cooldowns.tick();
    }

    

    @Nullable
    @Override
    public final Action<P> getAction(ActionType type, int index, boolean shift) {
        List<Action<P>> actions = getActions(type);
        if (index < 0 || index >= actions.size()) {
            return null;
        }
        Action<P> action = actions.get(index).getVisibleAction(getThis());
        Action<P> held = getHeldAction();
        if (action == held) {
            return action;
        }
        if (action != null && action.hasShiftVariation()) {
            Action<P> shiftVar = action.getShiftVariationIfPresent().getVisibleAction(getThis());
            if (shiftVar != null && (shift || shiftVar == held)) {
                action = shiftVar;
            }
        }
        return action;
    }
    
    @Override
    public final boolean clickAction(Action<P> action, boolean shift, ActionTarget target) {
        boolean res = onClickAction(action, shift, target);
        action.afterClick(user.level, user, getThis(), res);
        return res;
    }
    
    private boolean onClickAction(Action<P> action, boolean shift, ActionTarget target) {
        if (action == null || getHeldAction() == action) return false;
        boolean wasActive = isActive();
        action.onClick(user.level, user, getThis());
        Container<ActionTarget> targetContainer = new Container<>(target);
        ActionConditionResult result = checkRequirements(action, targetContainer, true);
        target = targetContainer.get();
        serverPlayerUser.ifPresent(player -> {
            player.resetLastActionTime();
        });
        if (action.getHoldDurationMax(getThis()) > 0) {
            action.startedHolding(user.level, user, getThis(), target, result.isPositive());
            if (!result.isPositive()) {
                sendMessage(action, result);
            }
            if (result.isPositive()) {
                if (!user.level.isClientSide()) {
                    action.playVoiceLine(user, getThis(), target, wasActive, shift);
                }
                setHeldAction(action);
                setHeldActionTarget(target);
                return true;
            }
            else {
                return false;
            }
        }
        else {
            if (result.isPositive()) {
                if (!user.level.isClientSide()) {
                    action.playVoiceLine(user, getThis(), target, wasActive, shift);
                }
                performAction(action, target);
            	stopHeldAction(false);
                return true;
            }
            else {
                sendMessage(action, result);
                return false;
            }
        }
    }
    
    private void sendMessage(Action<P> action, ActionConditionResult result) {
        if (!user.level.isClientSide() && action.sendsConditionMessage()) {
            ITextComponent message = result.getWarning();
            
            if (message != null) {
                serverPlayerUser.ifPresent(player -> {
                    player.displayClientMessage(message, true);
                });
            }
        }
    }

    @Override
    public ActionConditionResult checkRequirements(Action<P> action, Container<ActionTarget> targetContainer, boolean checkTarget) {
        if (!canUsePower()) {
            return ActionConditionResult.NEGATIVE;
        }
        
        if (heldActionData != null && !heldActionData.action.heldAllowsOtherActions(getThis()) && heldActionData.action != action) {
            return ActionConditionResult.NEGATIVE;
        }

        if (isActionOnCooldown(action)) {
            return ActionConditionResult.NEGATIVE;
        }

        LivingEntity performer = action.getPerformer(user, getThis());
        if (!action.ignoresPerformerStun() && performer != null && performer.getEffect(ModEffects.STUN.get()) != null) {
            return ActionConditionResult.createNegative(new TranslationTextComponent("jojo.message.action_condition.stun"));
        }

        if (checkTarget) {
            ActionConditionResult targetCheckResult = checkTarget(action, targetContainer);
            if (!targetCheckResult.isPositive()) {
                return targetCheckResult;
            }
        }

        ActionConditionResult condition = action.checkConditions(user, getThis(), targetContainer.get());
        if (!condition.isPositive()) {
            return condition;
        }

        if (!action.isUnlocked(getThis())) {
            return ActionConditionResult.createNegative(new TranslationTextComponent("jojo.message.action_condition.not_unlocked"));
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override

    public ActionConditionResult checkTarget(Action<P> action, Container<ActionTarget> targetContainer) {
        ActionTarget targetInitial = targetContainer.get();
        if (targetInitial.getType() == TargetType.ENTITY && targetInitial.getEntity() == null 
                || targetInitial.getType() == TargetType.BLOCK && targetInitial.getBlockPos() == null
                || !action.getTargetRequirement().checkTargetType(targetContainer.get().getType())) {
            targetContainer.set(ActionTarget.EMPTY);
            if (!action.getTargetRequirement().checkTargetType(TargetType.EMPTY)) {
                return ActionConditionResult.NEGATIVE_CONTINUE_HOLD;
            }
        }
        
        P power = getThis();
        ActionConditionResult preResult = action.checkRangeAndTarget(targetContainer.get(), user, power);

        if (!preResult.isPositive()) {
            targetContainer.set(ActionTarget.EMPTY);
            return action.getTargetRequirement().checkTargetType(TargetType.EMPTY) ? ActionConditionResult.POSITIVE : preResult;
        }
        else {
            return preResult;
        }
    }
    
    @Override
    public boolean canUsePower() {
        return !user.isSpectator();
    }

    @Override
    public float getLearningProgressPoints(Action<P> action) {
        return action.isUnlocked(getThis()) ? 1 : -1;
    }
    
    @Override
    public float getLearningProgressRatio(Action<P> action) {
        return getLearningProgressPoints(action) / action.getMaxTrainingPoints(getThis());
    }
    
    protected void performAction(Action<P> action, ActionTarget target) {
        if (!action.holdOnly()) {
            World world = user.level;
            target = action.targetBeforePerform(world, user, getThis(), target);
            action.onPerform(world, user, getThis(), target);
            if (!world.isClientSide()) {
                int cooldown = action.getCooldown(getThis(), -1);
                if (cooldown > 0) {
                    setCooldownTimer(action, cooldown);
                }
            }
        }
    }

    @Override
    public void setHeldAction(Action<P> action) {
        heldActionData = new HeldActionData<P>(action);
        if (!user.level.isClientSide() && action.isHeldSentToTracking()) {
            PacketManager.sendToClientsTracking(new TrHeldActionPacket(user.getId(), getPowerClassification(), action, false), user);
        }
    }

    @Override
    public Action<P> getHeldAction(boolean checkRequirements) {
        if (heldActionData == null || checkRequirements && !heldActionData.lastTickWentOff()) {
            return null;
        }
        return heldActionData.action;
    }


    @Override
    public void setHeldActionTarget(ActionTarget target) {
        if (heldActionData != null) {
            heldActionData.setActionTarget(target);
        }
    }

    private void tickHeldAction() {
        if (heldActionData != null) {
            Action<P> heldAction = heldActionData.action;
            World world = user.level;
            heldActionData.incTicks();
            if (user.level.isClientSide()) {
                heldAction.onHoldTickClientEffect(user, getThis(), heldActionData.getTicks(), heldActionData.lastTickWentOff(), false);
            }
            if (!world.isClientSide() || user.is(ClientUtil.getClientPlayer())) {
                if (!canUsePower()) {
                    stopHeldAction(false);
                    return;
                }
                if (heldActionData.getTicks() >= heldAction.getHoldDurationMax(getThis())) {
                    stopHeldAction(true);
                    return;
                }
                ActionTarget target = heldActionData.getActionTarget();
                Container<ActionTarget> targetContainer = new Container<>(target);
                ActionConditionResult result = checkRequirements(heldActionData.action, targetContainer, true);
                target = targetContainer.get();
                if (!result.isPositive() && result.shouldStopHeldAction()) {
                    stopHeldAction(false);
                    sendMessage(heldAction, result);
                    return;
                }
                heldAction.onHoldTick(world, user, getThis(), heldActionData.getTicks(), target, result.isPositive());
                if (!world.isClientSide()) {
                    refreshHeldActionTickState(result.isPositive());
                }
            }
        }
    }
    
    @Override
    public void refreshHeldActionTickState(boolean requirementsFulfilled) {
        if (heldActionData != null && heldActionData.refreshConditionCheckTick(requirementsFulfilled)) {
            Action<P> heldAction = heldActionData.action;
            if (user.level.isClientSide()) {
                heldAction.onHoldTickClientEffect(user, getThis(), heldActionData.getTicks(), requirementsFulfilled, true);
            }
            else {
                TrHeldActionPacket packet = new TrHeldActionPacket(user.getId(), getPowerClassification(), heldAction, requirementsFulfilled);
                if (heldAction.isHeldSentToTracking()) {
                    PacketManager.sendToClientsTrackingAndSelf(packet, user);
                }
                else {
                    serverPlayerUser.ifPresent(player -> PacketManager.sendToClient(packet, player));
                }
            }
        }
    }

    @Override
    public int getHeldActionTicks() {
        return heldActionData == null ? 0 : heldActionData.getTicks();
    }

    @Override
    public void stopHeldAction(boolean shouldFire) {
        if (heldActionData != null) {
            Action<P> heldAction = heldActionData.action;
            ActionTarget target = heldActionData.getActionTarget();
            int ticksHeld = getHeldActionTicks();
            
            
            
            if (heldAction.holdOnly()) {
                heldAction.stoppedHolding(user.level, user, getThis(), ticksHeld, false);
                
                int cooldown = heldAction.getCooldown(getThis(), ticksHeld);
                if (cooldown > 0) {
                    setCooldownTimer(heldAction, cooldown);
                }
            }
            else {
                Container<ActionTarget> targetContainer = new Container<>(target);
                boolean fire = shouldFire && heldActionData.getTicks() >= heldAction.getHoldDurationToFire(getThis()) && 
                        checkRequirements(heldAction, targetContainer, true).isPositive();

                heldAction.stoppedHolding(user.level, user, getThis(), ticksHeld, fire);
                
                if (fire) {
                    target = targetContainer.get();
                    performAction(heldAction, target);
                }
            }
            heldActionData = null;
            if (!user.level.isClientSide()) {
                TrHeldActionPacket packet = TrHeldActionPacket.actionStopped(user.getId(), getPowerClassification());
                if (heldAction.isHeldSentToTracking()) {
                    PacketManager.sendToClientsTrackingAndSelf(packet, user);
                }
                else {
                    serverPlayerUser.ifPresent(player -> PacketManager.sendToClient(packet, player));
                }
            }
        }
    }
    
    @Override
    public void onUserGettingAttacked(DamageSource dmgSource, float dmgAmount) {
        if (dmgSource.getDirectEntity() != null) {
            Action<P> heldAction = getHeldAction();
            if (heldAction != null && heldAction.cancelHeldOnGettingAttacked(getThis(), dmgSource, dmgAmount)) {
                stopHeldAction(false);
            }
        }
    }
    
    @Override
    public float getTargetResolveMultiplier(IStandPower attackingStand) {
        return hasPower() ? getType().getTargetResolveMultiplier(getThis(), attackingStand) : 1F;
    }
    
    @Override
    public boolean canLeap() {
        return hasPower() && getLeapCooldown() == 0 && isLeapUnlocked() && leapStrength() > 0;
    }
    
    @Override
    public void onLeap() {
    	if (!isUserCreative()) {
    		setLeapCooldown(getLeapCooldownPeriod());
    	}
    }
    
    @Override
    public int getLeapCooldown() {
        return leapCooldown;
    }
    
    @Override
    public void setLeapCooldown(int cooldown) {
        boolean send = this.leapCooldown != cooldown;
        this.leapCooldown = cooldown;
        if (send) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClient(new LeapCooldownPacket(getPowerClassification(), leapCooldown), player);
            });
        }
    }
    
    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT cnbt = new CompoundNBT();
        cnbt.putLong("LastDay", lastTickedDay);
        cnbt.put("Cooldowns", cooldowns.writeNBT());
        cnbt.putInt("LeapCd", leapCooldown);
        return cnbt;
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
    	lastTickedDay = nbt.getLong("LastDay");
        cooldowns = new ActionCooldownTracker(nbt.getCompound("Cooldowns"));
        leapCooldown = nbt.getInt("LeapCd");
    }

    @Override
    public void onClone(P oldPower, boolean wasDeath) {
        if (oldPower.hasPower() && (!wasDeath || oldPower.getType().keepOnDeath(oldPower))) {
            keepPower(oldPower, wasDeath);
        }
    }

    protected void keepPower(P oldPower, boolean wasDeath) {
        this.leapCooldown = oldPower.getLeapCooldown();
        this.cooldowns = oldPower.getCooldowns();
    }
    
    @Override
    public void syncWithUserOnly() {
        serverPlayerUser.ifPresent(player -> {
            if (hasPower()) {
                PacketManager.sendToClient(new LeapCooldownPacket(getPowerClassification(), leapCooldown), player);
                ModCriteriaTriggers.GET_POWER.get().trigger(player, getPowerClassification(), this);
                syncWithTrackingOrUser(player);
            }
        });
    }

    @Override
    public void syncWithTrackingOrUser(ServerPlayerEntity player) {
        if (hasPower() && user != null) {
            cooldowns.syncWithTrackingOrUser(user.getId(), getPowerClassification(), player);
        }
    }
    
    private P getThis() {
        return (P) this;
    }
}
