package com.github.standobyte.jojo.power;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTargetContainer;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap.OneTimeNotification;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.command.JojoControlsCommand;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.SyncInputBufferPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncLeapCooldownPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSyncCooldownPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSyncHeldActionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSyncPowerTypePacket;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
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
    protected T type;
    protected List<Action<P>> attacks = new ArrayList<>();
    protected List<Action<P>> abilities = new ArrayList<>();
    private ActionCooldownTracker cooldowns = new ActionCooldownTracker();
    private int leapCooldown;
    protected HeldActionData<P> heldActionData;
    private Action<P> inputBuffer;

    public PowerBaseImpl(LivingEntity user) {
        this.user = user;
        this.serverPlayerUser = user instanceof ServerPlayerEntity ? Optional.of((ServerPlayerEntity) user) : Optional.empty();
    }

    @Override
    public boolean hasPower() {
        return getType() != null;
    }

    @Override
    public boolean givePower(T type) {
        if (type == null || hasPower() && !getType().isReplaceableWith(type)) {
            return false;
        }
        setType(type);
        leapCooldown = getLeapCooldownPeriod();

        serverPlayerUser.ifPresent(player -> {
            PacketManager.sendToClientsTrackingAndSelf(new TrSyncPowerTypePacket<P, T>(player.getId(), getPowerClassification(), getType()), player);
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.sendNotification(OneTimeNotification.POWER_CONTROLS, 
                        new TranslationTextComponent("jojo.chat.controls.message", 
                                new StringTextComponent("/" + JojoControlsCommand.LITERAL)
                                .withStyle((style) -> style
                                        .withColor(TextFormatting.GREEN)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + JojoControlsCommand.LITERAL))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("jojo.chat.controls.tooltip"))))));
            });
        });
        return true;
    }

    protected abstract void afterTypeInit(T type);
    
    protected void setType(T type) {
        this.type = type;
        serverPlayerUser.ifPresent(player -> {
            ModCriteriaTriggers.GET_POWER.get().trigger(player, getPowerClassification(), this);
        });
        afterTypeInit(type);
    }

    @Override
    public boolean clear() {
        if (!hasPower()) {
            return false;
        }
        attacks = new ArrayList<>();
        abilities = new ArrayList<>();
        serverPlayerUser.ifPresent(player -> {
            PacketManager.sendToClientsTrackingAndSelf(TrSyncPowerTypePacket.noPowerType(player.getId(), getPowerClassification()), player);
        });
        return true;
    }

    @Override
    public T getType() {
        return type;
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

    private void setCooldownTimer(Action<?> action, int value) {
        if (value > 0) {
            setCooldownTimer(action, value, value);
            if (!user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrSyncCooldownPacket(user.getId(), getPowerClassification(), action, value), user);
            }
        }
    }

    @Override
    public final void setCooldownTimer(Action<?> action, int value, int totalCooldown) {
        cooldowns.addCooldown(action, value, totalCooldown);
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
        Action<P> action = actions.get(index);
        if (shift && action.getShiftVariationIfPresent().isUnlocked(getThis())) {
            action = action.getShiftVariationIfPresent();
        }
        return action;
    }
    
    @Override
    public final boolean onClickAction(Action<P> action, boolean shift, ActionTarget target) {
        if (heldActionData == null) {
            boolean wasActive = isActive();
            action.onClick(user.level, user, getThis());
            ActionTargetContainer targetContainer = new ActionTargetContainer(target);
            ActionConditionResult result = checkRequirements(action, targetContainer, true);
            target = targetContainer.getTarget();
            serverPlayerUser.ifPresent(player -> {
                player.resetLastActionTime();
            });
            if (action.getHoldDurationMax(getThis()) > 0) {
                action.startedHolding(user.level, user, getThis(), target, result.isPositive());
                if (result.isPositive() || !result.shouldStopHeldAction()) {
                    if (!user.level.isClientSide()) {
                        action.playVoiceLine(user, getThis(), target, wasActive, shift);
                    }
                    setHeldAction(action);
                    setHeldActionTarget(target);
                    return true;
                }
                else {
                    sendMessage(action, result);
                    return false;
                }
            }
            else {
                if (result.isPositive()) {
                    if (!user.level.isClientSide()) {
                        action.playVoiceLine(user, getThis(), target, wasActive, shift);
                    }
                    performAction(action, target);
                    return true;
                }
                else if (result.queueInput()) {
                    queueNextAction(action);
                }
                else {
                    sendMessage(action, result);
                    return false;
                }
            }
        }
        return false;
    }
    
    @Override
    public void queueNextAction(Action<P> action) {
        this.inputBuffer = action;
        serverPlayerUser.ifPresent(player -> {
            PacketManager.sendToClient(new SyncInputBufferPacket(getPowerClassification(), action), player);
        });
    }
    
    @Override
    public boolean clickQueuedAction() {
        if (inputBuffer != null) {
            boolean ret = onClickAction(inputBuffer, user != null && user.isShiftKeyDown(), ActionTarget.EMPTY);
            inputBuffer = null;
            return ret;
        }
        return false;
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
    public ActionConditionResult checkRequirements(Action<P> action, ActionTargetContainer targetContainer, boolean checkTargetType) {
        if (!canUsePower() || heldActionData != null && heldActionData.action != action) {
            return ActionConditionResult.NEGATIVE;
        }

        if (isActionOnCooldown(action)) {
            return ActionConditionResult.NEGATIVE;
        }

        LivingEntity performer = action.getPerformer(user, getThis());
        if (!action.ignoresPerformerStun() && performer != null && performer.getEffect(ModEffects.STUN.get()) != null) {
            return ActionConditionResult.createNegative(new TranslationTextComponent("jojo.message.action_condition.stun"));
        }

        if (checkTargetType) {
            ActionConditionResult targetCheckResult = checkTargetType(action, targetContainer);
            if (!targetCheckResult.isPositive()) {
                return targetCheckResult;
            }
        }

        ActionConditionResult condition = action.checkConditions(user, getThis(), targetContainer.getTarget());
        if (!condition.isPositive()) {
            return condition;
        }

        if (!action.isUnlocked(getThis())) {
            return ActionConditionResult.createNegative(new TranslationTextComponent("jojo.message.action_condition.not_unlocked"));
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    public ActionConditionResult checkTargetType(Action<P> action, ActionTargetContainer targetContainer) {
        ActionTarget target = targetContainer.getTarget();
        LivingEntity performer = action.getPerformer(user, getThis());
        boolean targetTooFar = false;
        switch (target.getType()) {
        case ENTITY:
            Entity targetEntity = target.getEntity(user.level);
            if (targetEntity == null) {
                target = ActionTarget.EMPTY;
            }
            else {
                double rangeSq = action.getMaxRangeSqEntityTarget();
                if (!performer.canSee(targetEntity)) {
                    rangeSq /= 4.0D;
                }
                if (performer.distanceToSqr(targetEntity) > rangeSq) {
                    target = ActionTarget.EMPTY;
                    targetTooFar = true;
                }
            }
            break;
        case BLOCK:
            BlockPos targetPos = target.getBlockPos();
            int buildLimit = 256;
            boolean validPos = false;
            if (targetPos.getY() < buildLimit - 1 || target.getFace() != Direction.UP && targetPos.getY() < buildLimit) {
                double distSq = action.getMaxRangeSqBlockTarget();
                if (user.level.getBlockState(targetPos).getBlock() != Blocks.AIR && 
                        performer.distanceToSqr((double)targetPos.getX() + 0.5D, (double)targetPos.getY() + 0.5D, (double)targetPos.getZ() + 0.5D) < distSq) {
                    validPos = true;
                }
            }
            if (!validPos) {
                target = ActionTarget.EMPTY;
                targetTooFar = true;
            }
            break;
        default:
            break;
        }

        targetContainer.setNewTarget(target);
        if (!action.appropriateTarget(target.getType())) {
            if (targetTooFar) {
                return ActionConditionResult.createNegativeContinueHold(new TranslationTextComponent("jojo.message.action_condition.target_too_far"));
            }
            return ActionConditionResult.NEGATIVE_CONTINUE_HOLD;
        }
        return ActionConditionResult.POSITIVE;
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
            action.onPerform(world, user, getThis(), target);
            if (!world.isClientSide()) {
                setCooldownTimer(action, action.getCooldown(getThis(), -1));
            }
        }
    }

    @Override
    public void setHeldAction(Action<P> action) {
        heldActionData = new HeldActionData<P>(action);
        if (!user.level.isClientSide() && action.isHeldSentToTracking()) {
            PacketManager.sendToClientsTracking(new TrSyncHeldActionPacket(user.getId(), getPowerClassification(), action, false), user);
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
                ActionTargetContainer targetContainer = new ActionTargetContainer(target);
                ActionConditionResult result = checkRequirements(heldActionData.action, targetContainer, true);
                target = targetContainer.getTarget();
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
                TrSyncHeldActionPacket packet = new TrSyncHeldActionPacket(user.getId(), getPowerClassification(), heldAction, requirementsFulfilled);
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
            heldAction.stoppedHolding(user.level, user, getThis(), ticksHeld);
            if (!heldAction.holdOnly()) {
                ActionTargetContainer targetContainer = new ActionTargetContainer(target);
                if (shouldFire && heldActionData.getTicks() >= heldAction.getHoldDurationToFire(getThis()) && 
                        checkRequirements(heldAction, targetContainer, true).isPositive()) {
                    target = targetContainer.getTarget();
                    performAction(heldAction, target);
                }
            }
            else {
                setCooldownTimer(heldAction, heldAction.getCooldown(getThis(), ticksHeld));
            }
            heldActionData = null;
            if (!user.level.isClientSide()) {
                TrSyncHeldActionPacket packet = TrSyncHeldActionPacket.actionStopped(user.getId(), getPowerClassification());
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
            if (heldAction != null && heldAction.cancelHeldOnGettingAttacked(getThis())) {
                stopHeldAction(false);
            }
        }
    }
    
    @Override
    public float getTargetResolveMultiplier() {
        return hasPower() ? getType().getTargetResolveMultiplier(getThis()) : 1F;
    }
    
    @Override
    public boolean canLeap() {
        return hasPower() && user.isOnGround() && getLeapCooldown() == 0 && isLeapUnlocked() && leapStrength() > 0;
    }
    
    @Override
    public void onLeap() {
        setLeapCooldown(getLeapCooldownPeriod());
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
                PacketManager.sendToClient(new SyncLeapCooldownPacket(getPowerClassification(), leapCooldown), player);
            });
        }
    }
    
    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT cnbt = new CompoundNBT();
        cnbt.put("Cooldowns", cooldowns.writeNBT());
        cnbt.putInt("LeapCd", leapCooldown);
        return cnbt;
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
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
        setType(oldPower.getType());
        this.leapCooldown = oldPower.getLeapCooldown();
        this.cooldowns = oldPower.getCooldowns();
    }

    @Override
    public void syncWithUserOnly() {
        serverPlayerUser.ifPresent(player -> {
            if (hasPower()) {
                PacketManager.sendToClient(new SyncLeapCooldownPacket(getPowerClassification(), leapCooldown), player);
                syncWithTrackingOrUser(player);
            }
        });
    }

    @Override
    public void syncWithTrackingOrUser(ServerPlayerEntity player) {
        if (hasPower()) {
            LivingEntity user = getUser();
            if (user != null) {
                PacketManager.sendToClient(new TrSyncPowerTypePacket<P, T>(user.getId(), getPowerClassification(), getType()), player);
                cooldowns.syncWithTrackingOrUser(user.getId(), getPowerClassification(), player);
            }
        }
    }
    
    private P getThis() {
        return (P) this;
    }
}
