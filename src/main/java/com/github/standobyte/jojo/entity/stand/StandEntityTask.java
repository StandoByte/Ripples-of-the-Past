package com.github.standobyte.jojo.entity.stand;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.stand.IStandPhasedAction;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.action.stand.StandEntityActionModifier;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrStandEntityTaskModifierPacket;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.StacksTHC;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraftforge.registries.DataSerializerEntry;

public class StandEntityTask {
    @Nonnull
    private final StandEntityAction action;
    @Nonnull
    private ActionTarget target = ActionTarget.EMPTY;
    private int startingTicks;
    private int ticksLeft;
    @Nonnull
    private StandEntityAction.Phase phase;
    private Set<StandEntityActionModifier> taskModifiers = new HashSet<>();
    @Nullable
    private StandRelativeOffset offsetFromUser;
    private StacksTHC additionalData = new StacksTHC();
    
    private StandEntityTask(StandEntityAction action, int ticks, 
            StandEntityAction.Phase phase, boolean armsOnlyMode, ActionTarget target, 
            StandRelativeOffset offset) {
        this.action = action;
        this.startingTicks = Math.max(ticks, 1);
        this.ticksLeft = this.startingTicks;
        this.phase = phase;
        this.offsetFromUser = offset;
        this.target = target;
    }
    
    static StandEntityTask makeServerSideTask(StandEntity standEntity, IStandPower standPower, StandEntityAction action, int ticks, 
            StandEntityAction.Phase phase, boolean armsOnlyMode, ActionTarget target) {
        StandRelativeOffset offset = standEntity.hasEffect(ModEffects.STUN.get()) || !standEntity.hasUser() ? 
                null
                : action.getOffsetFromUser(standPower, standEntity, target);
        if (!keepTarget(standEntity, target, standPower, action)) {
            target = ActionTarget.EMPTY;
        }
        
        StandEntityTask task = new StandEntityTask(action, ticks, phase, armsOnlyMode, target, offset);
        
        return task;
    }
    
    boolean setTarget(StandEntity standEntity, ActionTarget target, IStandPower standPower) {
        if (keepTarget(standEntity, target, standPower, action)) {
            boolean targetChanged = !target.sameTarget(this.target);
            this.target = target;
            return targetChanged;
        }
        return false;
    }

    private static boolean keepTarget(StandEntity standEntity, ActionTarget target, IStandPower standPower, StandEntityAction action) {
        if (target.getType() == TargetType.EMPTY || standEntity.level.isClientSide()) return true;
        if (target.getType() == TargetType.ENTITY) {
            Entity targetEntity = target.getEntity();
            if (targetEntity == null || targetEntity.is(standEntity)
                    || !targetEntity.isAlive() && !(targetEntity instanceof LivingEntity && ((LivingEntity) targetEntity).deathTime < 20)) {
                return false;
            }
        }
        return action.checkRangeAndTarget(target, standPower.getUser(), standPower).isPositive();
    }
    
    public void addModifierAction(StandEntityActionModifier action, StandEntity standEntity) {
        taskModifiers.add(action);
        if (!standEntity.level.isClientSide()) {
            PacketManager.sendToClientsTracking(new TrStandEntityTaskModifierPacket(standEntity.getId(), action), standEntity);
        }
    }
    
    public Stream<StandEntityActionModifier> getModifierActions() {
        return taskModifiers.stream();
    }
    
    void tick(IStandPower standPower, StandEntity standEntity) {
//        if (target.getType() != TargetType.EMPTY) {
//            if (!standEntity.level.isClientSide() && ticksLeft < startingTicks) {
//                double angleCos = standEntity.getLookAngle().dot(target.getTargetPos().subtract(standEntity.getEyePosition(1.0F)).normalize());
//            }
//            if (!standEntity.isManuallyControlled() && target.getType() == TargetType.BLOCK) {
//                setTarget(ActionTarget.EMPTY);
//            }
//        }
        if (!standEntity.level.isClientSide()) {
            if (!keepTarget(standEntity, target, standPower, action)) {
                standEntity.setTaskTarget(ActionTarget.EMPTY);
            }
        }
        
        tickAction(standPower, standEntity, action);
        taskModifiers.forEach(modifier -> tickAction(standPower, standEntity, modifier));
        
        ticksLeft--;
        if (ticksLeft <= 0 && phase != StandEntityAction.Phase.BUTTON_HOLD) {
            moveToPhase(phase.getNextPhase(), standPower, standEntity);
        }
    }
    
    private void tickAction(IStandPower standPower, StandEntity standEntity, IStandPhasedAction action) {
        int phaseTicks = startingTicks - ticksLeft;
        switch (phase) {
        case BUTTON_HOLD:
            action.standTickButtonHold(standEntity.level, standEntity, standPower, this);
            break;
        case WINDUP:
            action.standTickWindup(standEntity.level, standEntity, standPower, this);
            break;
        case PERFORM:
            if (phaseTicks == 0 && action.standCanPerform(standEntity.level, standEntity, standPower, this)) {
                action.standPerform(standEntity.level, standEntity, standPower, this);
                if (!standEntity.level.isClientSide()) {
                    standPower.consumeStamina(action.getStaminaCost(standPower));
                }
            }

            if (action.standCanTick(standEntity.level, standEntity, standPower, this)) {
                action.standTickPerform(standEntity.level, standEntity, standPower, this);
                if (!standEntity.level.isClientSide()) {
                    standPower.consumeStamina(action.getStaminaCostTicking(standPower));
                }
            }
            break;
        case RECOVERY:
            action.standTickRecovery(standEntity.level, standEntity, standPower, this);
            break;
        }
    }

    public void moveToPhase(@Nullable StandEntityAction.Phase phase, IStandPower standPower, StandEntity standEntity) {
        if (phase == null) {
            action.phaseTransition(standEntity.level, standEntity, standPower, this.phase, null, this, 0);
            standEntity.stopTask();
            return;
        }
        
        int ticks;
        switch (phase) {
        case BUTTON_HOLD:
        	return;
        case WINDUP:
            ticks = action.getStandWindupTicks(standPower, standEntity);
            break;
        case PERFORM:
            ticks = action.getStandActionTicks(standPower, standEntity);
            break;
        case RECOVERY:
            ticks = action.getStandRecoveryTicks(standPower, standEntity);
            break;
        default:
            return;
        }
        StandEntityAction.Phase prevPhase = this.phase;
        if (setPhase(phase, ticks)) {
            action.playSound(standEntity, standPower, phase, this);
            action.phaseTransition(standEntity.level, standEntity, standPower, prevPhase, phase, this, ticks);
        }
        else {
            moveToPhase(phase.getNextPhase(), standPower, standEntity);
        }
    }
    
    private boolean setPhase(StandEntityAction.Phase phase, int ticks) {
        if (ticks > 0) {
            this.phase = phase;
            this.startingTicks = ticks;
            this.ticksLeft = startingTicks;
            return true;
        }
        return false;        
    }
    
    public void addTicksToPhase(int ticks) {
        this.startingTicks += ticks;
    }
    
    boolean rotateStand(StandEntity standEntity) {
        if (target.getType() != TargetType.EMPTY && !standEntity.isManuallyControlled()) {
            action.rotateStandTowardsTarget(standEntity, target, this);
    		return true;
        }
        return false;
    }
    
    public StandEntityAction getAction() {
        return action;
    }
    
    public int getTicksLeft() {
        return ticksLeft;
    }
    
    public int getStartingTicks() {
        return startingTicks;
    }
    
    public int getTick() {
    	return startingTicks - ticksLeft;
    }
    
    public float getPhaseCompletion(float partialTick) {
        return Math.min(1F - ((float) ticksLeft - partialTick) / (float) startingTicks, 1F);
    }
    
    public StandEntityAction.Phase getPhase() {
        return phase;
    }
    
    public ActionTarget getTarget() {
    	return target;
    }
    
    public void setOffsetFromUser(StandRelativeOffset offset) {
        this.offsetFromUser = offset;
    }
    
    @Nullable
    public StandRelativeOffset getOffsetFromUser() {
        return offsetFromUser;
    }
    
    
    
    public StacksTHC getAdditionalData() {
    	return additionalData;
    }
    
    

    public static final Supplier<DataSerializerEntry> SERIALIZER = () -> new DataSerializerEntry(
            new IDataSerializer<Optional<StandEntityTask>>() {

        @Override
        public void write(PacketBuffer buf, Optional<StandEntityTask> value) {
            boolean taskNotEmplty = value.isPresent();
            buf.writeBoolean(taskNotEmplty);
            if (taskNotEmplty) {
                StandEntityTask task = value.get();
                
                buf.writeRegistryIdUnsafe(ModActions.Registry.getRegistry(), task.action);
                
                buf.writeVarInt(task.startingTicks);
                buf.writeEnum(task.phase);
                
                task.target.writeToBuf(buf);
                
                buf.writeBoolean(task.offsetFromUser != null);
                if (task.offsetFromUser != null) {
                    task.offsetFromUser.writeToBuf(buf);
                }
                
                NetworkUtil.writeCollection(buf, task.taskModifiers, (buffer, action) -> buffer.writeRegistryId(action));
            }
        }

        @Override
        public Optional<StandEntityTask> read(PacketBuffer buf) {
            if (!buf.readBoolean()) {
                return Optional.empty();
            }
            
            Action<?> action = buf.readRegistryIdUnsafe(ModActions.Registry.getRegistry());
            if (!(action instanceof StandEntityAction)) {
                return Optional.empty();
            }
            
            int ticks = buf.readVarInt();
            StandEntityAction.Phase phase = buf.readEnum(StandEntityAction.Phase.class);
            
            ActionTarget target = ActionTarget.readFromBuf(buf);
            
            StandRelativeOffset offset = null;
            if (buf.readBoolean()) {
                offset = StandRelativeOffset.readFromBuf(buf);
            }
            
            StandEntityTask task = new StandEntityTask((StandEntityAction) action, ticks, phase, false, target, offset);
            
            NetworkUtil.readCollection(buf, buffer -> buffer.readRegistryIdSafe(Action.class)).forEach(modifier -> {
                if (modifier instanceof StandEntityActionModifier) {
                    task.taskModifiers.add((StandEntityActionModifier) modifier);
                }
            });
            return Optional.of(task);
        }

        @Override
        public Optional<StandEntityTask> copy(Optional<StandEntityTask> value) {
            if (value.isPresent()) {
                StandEntityTask task = value.get();
                StandEntityTask taskNew = new StandEntityTask(task.action, task.startingTicks, task.phase, false, task.target.copy(), task.offsetFromUser);
                taskNew.ticksLeft = task.ticksLeft;
                taskNew.offsetFromUser = task.offsetFromUser;
                taskNew.taskModifiers = task.taskModifiers;
                return Optional.of(taskNew);
            }
            return Optional.empty();
        }
    });
}
