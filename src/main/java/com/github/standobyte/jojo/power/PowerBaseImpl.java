package com.github.standobyte.jojo.power;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.HeldActionData;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap.OneTimeNotification;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.command.JojoControlsCommand;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.SyncLeapCooldownPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncManaLimitFactorPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncManaPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncManaRegenPointsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSyncCooldownPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSyncHeldActionPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrSyncPowerTypePacket;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;

public abstract class PowerBaseImpl<T extends IPowerType<T>> implements IPower<T> {
    @Nonnull
    protected final LivingEntity user;
    protected final Optional<ServerPlayerEntity> serverPlayerUser;
    protected List<Action> attacks = new ArrayList<Action>();
    protected List<Action> abilities = new ArrayList<Action>();
    private float mana = 0;
    protected float manaRegenPoints = 1;
    protected float manaLimitFactor = 1;
    private ActionCooldownTracker cooldowns = new ActionCooldownTracker();
    private int leapCooldown;
    protected HeldActionData heldActionData;

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
        mana = 0;
        onTypeInit(type);
        if (user instanceof PlayerEntity && ((PlayerEntity) user).abilities.instabuild) {
            mana = getMaxMana();
        }
        leapCooldown = getLeapCooldownPeriod();

        serverPlayerUser.ifPresent(player -> {
            PacketManager.sendToClientsTrackingAndSelf(new TrSyncPowerTypePacket<T>(player.getId(), getPowerClassification(), getType()), player);
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.sendNotification(OneTimeNotification.POWER_CONTROLS, 
                        new TranslationTextComponent("chat.controls.message", 
                                new StringTextComponent("/" + JojoControlsCommand.LITERAL)
                                .withStyle((style) -> style
                                        .withColor(TextFormatting.GREEN)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + JojoControlsCommand.LITERAL))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("chat.controls.tooltip"))))));
            });
        });
        return true;
    }

    protected abstract void onTypeInit(T type);

    @Override
    public boolean clear() {
        if (!hasPower()) {
            return false;
        }
        mana = 0;
        attacks = new ArrayList<Action>();
        abilities = new ArrayList<Action>();
        serverPlayerUser.ifPresent(player -> {
            PacketManager.sendToClientsTrackingAndSelf(TrSyncPowerTypePacket.noPowerType(player.getId(), getPowerClassification()), player);
        });
        return true;
    }

    @Override
    public final LivingEntity getUser() {
        return user;
    }

    @Override
    public final void tick() {
        if (hasPower()) {
            tickMana();
            tickHeldAction();
            tickCooldown();
            if (leapCooldown > 0) {
                leapCooldown--;
            }
            getType().tickUser(getUser(), this);
        }
    }
    
    protected void tickMana() {
        if (getType().canTickMana(getUser(), this)) {
            mana = MathHelper.clamp(mana + manaRegenPoints, 0, getMaxMana());
        }
    }

    @Override
    public List<Action> getAttacks() {
        return attacks;
    }

    @Override
    public List<Action> getAbilities() {
        return abilities;
    }

    @Override
    public final float getMana() {
        return mana;
    }

    @Override
    public float getMaxMana() {
        return 1000 * manaLimitFactor;
    }
    
    @Override
    public final boolean hasMana(float mana) {
        return getMana() >= mana || infiniteMana();
    }

    @Override
    public final void addMana(float amount) {
        setMana(MathHelper.clamp(mana + amount, 0, getMaxMana()));
    }

    @Override
    public final boolean consumeMana(float amount) {
        if (infiniteMana()) {
            return true;
        }
        if (mana >= amount) {
            amount = reduceManaConsumed(amount);
            setMana(mana - amount);
            return true;
        }
        return false;
    }
    
    protected float reduceManaConsumed(float amount) {
        return amount;
    }

    @Override
    public boolean infiniteMana() {
        return user instanceof PlayerEntity && ((PlayerEntity) user).abilities.instabuild;
    }

    @Override
    public final void setMana(float amount) {
        amount = MathHelper.clamp(amount, 0, getMaxMana());
        boolean send = mana != amount;
        mana = amount;
        if (send) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClient(new SyncManaPacket(getPowerClassification(), getMana()), player);
            });
        }
    }

    @Override
    public final float getManaRegenPoints() {
        return manaRegenPoints;
    }

    @Override
    public final void setManaRegenPoints(float points) {
        boolean send = points != manaRegenPoints;
        manaRegenPoints = points;
        if (send) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClient(new SyncManaRegenPointsPacket(getPowerClassification(), getManaRegenPoints()), player);
            });
        }
    }

    @Override
    public final float getManaLimitFactor() {
        return manaLimitFactor;
    }

    @Override
    public final void setManaLimitFactor(float factor) {
        manaLimitFactor = Math.max(factor, 1);
    }
    
    @Override
    public final boolean isActionOnCooldown(Action action) {
        return cooldowns.isOnCooldown(action);
    }

    @Override
    public final float getCooldownRatio(Action action, float partialTick) {
        return cooldowns.getCooldownPercent(action, partialTick);
    }

    private void setCooldownTimer(Action action, int value) {
        if (value > 0) {
            setCooldownTimer(action, value, value);
            if (!user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrSyncCooldownPacket(user.getId(), getPowerClassification(), action, value), user);
            }
        }
    }

    @Override
    public final void setCooldownTimer(Action action, int value, int totalCooldown) {
        cooldowns.addCooldown(action, value, totalCooldown);
    }

    private void tickCooldown() {
        cooldowns.tick();
    }



    @Override
    public final boolean onClickAction(ActionType type, int index, boolean shift, ActionTarget target) {
        if (heldActionData == null) {
            List<Action> actions = getActions(type);
            if (index >= actions.size()) {
                return false;
            }
            Action action = actions.get(index);
            if (shift && isActionUnlocked(action.getShiftVariationIfPresent())) {
                action = action.getShiftVariationIfPresent();
            }
            boolean wasActive = isActive();
            action.updatePerformer(user.level, user, this);
            ActionConditionResult result = checkRequirements(action, action.getPerformer(user, this), target, true);
            serverPlayerUser.ifPresent(player -> {
                player.resetLastActionTime();
            });
            if (action.getHoldDurationMax() > 0) {
                action.onStartedHolding(user.level, user, this, target, result.isPositive());
                if (result.isPositive() || !result.shouldStopHeldAction()) {
                    if (!user.level.isClientSide()) {
                        action.playVoiceLine(user, this, target, wasActive, shift);
                    }
                    setHeldAction(action);
                    setHeldActionTarget(target);
                    return true;
                }
                else {
                    sendMessage(result);
                    return false;
                }
            }
            else {
                if (result.isPositive()) {
                    if (!user.level.isClientSide()) {
                        action.playVoiceLine(user, this, target, wasActive, shift);
                    }
                    performAction(action, target);
                    return true;
                }
                else {
                    sendMessage(result);
                    return false;
                }
            }
        }
        return false;
    }

    private void sendMessage(ActionConditionResult result) {
        if (!user.level.isClientSide()) {
            ITextComponent message = result.getWarning();
            if (message != null) {
                user.sendMessage(message, Util.NIL_UUID);
            }
        }
    }

    @Override
    public ActionConditionResult checkRequirements(Action action, LivingEntity performer, ActionTarget target, boolean checkTargetType) {
        if (user.isSpectator() || heldActionData != null && heldActionData.action != action) {
            return ActionConditionResult.NEGATIVE;
        }

        if (isActionOnCooldown(action)) {
            return ActionConditionResult.NEGATIVE;
        }

        if (!infiniteMana()) {
            if (getMana() < action.getManaNeeded(getHeldActionTicks(), this)) {
                serverPlayerUser.ifPresent(player -> {
                    PacketManager.sendToClient(new SyncManaPacket(getPowerClassification(), getMana()), player);
                });
                ITextComponent message = new TranslationTextComponent("chat.message.no_mana_" + getType().getManaString());
                return ActionConditionResult.createNegative(message);
            }
        }

        if (!action.ignoresPerformerStun() && performer != null && performer.getEffect(ModEffects.STUN.get()) != null) {
            return ActionConditionResult.createNegative(new TranslationTextComponent("chat.message.stun"));
        }

        if (checkTargetType) {
            ActionConditionResult targetCheckResult = checkTargetType(action, performer, target);
            if (!targetCheckResult.isPositive()) {
                return targetCheckResult;
            }
        }

        ActionConditionResult condition = action.checkConditions(user, performer, this, target);
        if (!condition.isPositive()) {
            return condition;
        }

        if (!isActionUnlocked(action)) {
            return ActionConditionResult.createNegative(new TranslationTextComponent("chat.message.not_unlocked"));
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    public ActionConditionResult checkTargetType(Action action, LivingEntity performer, ActionTarget target) {
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

        if (!action.appropriateTarget(target.getType())) {
            if (targetTooFar) {
                return ActionConditionResult.createNegativeContinueHold(new TranslationTextComponent("chat.message.target_too_far"));
            }
            return ActionConditionResult.NEGATIVE_CONTINUE_HOLD;
        }
        return ActionConditionResult.POSITIVE;
    }
    
    protected void performAction(Action action, ActionTarget target) {
        if (!action.holdOnly()) {
            World world = user.level;
            action.perform(world, user, this, target);
            if (!world.isClientSide()) {
                consumeMana(action.getManaCost());
                setCooldownTimer(action, action.getCooldown(this, -1));
            }
        }
    }

    @Override
    public void setHeldAction(Action action) {
        heldActionData = new HeldActionData(action);
        if (!user.level.isClientSide() && action.isHeldSentToTracking()) {
            PacketManager.sendToClientsTracking(new TrSyncHeldActionPacket(user.getId(), getPowerClassification(), action, false), user);
        }
    }

    @Override
    public Action getHeldAction(boolean checkRequirements) {
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
            Action heldAction = heldActionData.action;
            World world = user.level;
            heldActionData.incTicks();
            if (user.level.isClientSide()) {
                heldAction.onHoldTickClientEffect(user, this, heldActionData.getTicks(), heldActionData.lastTickWentOff(), false);
            }
            if (!world.isClientSide() || user.is(ClientUtil.getClientPlayer())) {
                if (user.isSpectator()) {
                    stopHeldAction(false);
                    return;
                }
                if (heldActionData.getTicks() >= heldAction.getHoldDurationMax()) {
                    stopHeldAction(true);
                    return;
                }
                ActionTarget target = heldActionData.getActionTarget();
                ActionConditionResult result = checkRequirements(heldActionData.action, heldAction.getPerformer(user, this), target, true);
                if (!result.isPositive() && result.shouldStopHeldAction()) {
                    stopHeldAction(false);
                    sendMessage(result);
                    return;
                }
                if (result.isPositive()) {
                    consumeMana(heldAction.getHeldTickManaCost());
                }
                heldAction.onHoldTickUser(world, user, this, heldActionData.getTicks(), target, result.isPositive());
                if (!world.isClientSide()) {
                    refreshHeldActionTickState(result.isPositive());
                }
            }
        }
    }
    
    @Override
    public void refreshHeldActionTickState(boolean requirementsFulfilled) {
        if (heldActionData != null && heldActionData.refreshConditionCheckTick(requirementsFulfilled)) {
            Action heldAction = heldActionData.action;
            if (user.level.isClientSide()) {
                heldAction.onHoldTickClientEffect(user, this, heldActionData.getTicks(), requirementsFulfilled, true);
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
            Action heldAction = heldActionData.action;
            ActionTarget target = heldActionData.getActionTarget();
            int ticksHeld = getHeldActionTicks();
            if (!heldAction.holdOnly()) {
                if (shouldFire && heldActionData.getTicks() >= heldAction.getHoldDurationToFire(this) && 
                        checkRequirements(heldAction, heldAction.getPerformer(user, this), target, true).isPositive()) {
                    performAction(heldAction, target);
                }
            }
            else {
                setCooldownTimer(heldAction, heldAction.getCooldown(this, ticksHeld));
            }
            heldAction.onStoppedHolding(user.level, user, this, ticksHeld);
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
    public boolean canLeap() {
        return hasPower() && user.isOnGround() && user.xRot < 0 && hasMana(getLeapManaCost()) && getLeapCooldown() == 0 && isLeapUnlocked() && leapStrength() > 0;
    }
    
    @Override
    public void onLeap() {
        consumeMana(getLeapManaCost());
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
    
    abstract protected float getLeapManaCost();
    
    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT cnbt = new CompoundNBT();
        cnbt.putFloat("Mana", getMana());
        cnbt.putFloat("ManaRegen", getManaRegenPoints());
        cnbt.putFloat("ManaLimitFactor", getManaLimitFactor());
        cnbt.put("Cooldowns", cooldowns.writeNBT());
        cnbt.putInt("LeapCd", leapCooldown);
        return cnbt;
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
        mana = nbt.getFloat("Mana");
        manaRegenPoints = nbt.getFloat("ManaRegen");
        manaLimitFactor = nbt.getFloat("ManaLimitFactor");
        cooldowns = new ActionCooldownTracker(nbt.getCompound("Cooldowns"));
        leapCooldown = nbt.getInt("LeapCd");
    }

    @Override
    public void onClone(IPower<T> oldPower, boolean wasDeath) {
        if (oldPower.hasPower()) {
            onTypeInit(oldPower.getType());
            if (!wasDeath) {
                mana = oldPower.getMana();
                leapCooldown = oldPower.getLeapCooldown();
            }
            manaRegenPoints = oldPower.getManaRegenPoints();
            manaLimitFactor = oldPower.getManaLimitFactor();
            cooldowns = ((PowerBaseImpl<T>) oldPower).cooldowns;
        }      
    }

    @Override
    public void syncWithUserOnly() {
        serverPlayerUser.ifPresent(player -> {
            if (hasPower()) {
                syncWithTrackingOrUser(player);
                PacketManager.sendToClient(new SyncManaPacket(getPowerClassification(), getMana()), player);
                PacketManager.sendToClient(new SyncManaRegenPointsPacket(getPowerClassification(), getManaRegenPoints()), player);
                PacketManager.sendToClient(new SyncManaLimitFactorPacket(getPowerClassification(), getManaLimitFactor()), player);
            }
        });
    }

    @Override
    public void syncWithTrackingOrUser(ServerPlayerEntity player) {
        if (hasPower()) {
            LivingEntity user = getUser();
            if (user != null) {
                PacketManager.sendToClient(new TrSyncPowerTypePacket<T>(user.getId(), getPowerClassification(), getType()), player);
                cooldowns.syncWithTrackingOrUser(user.getId(), getPowerClassification(), player);
            }
        }
    }
}
