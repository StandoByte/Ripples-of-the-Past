package com.github.standobyte.jojo.entity.stand;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.action.actions.StandEntityAction;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.init.ModEffects;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.StacksTHC;

import net.minecraft.entity.Entity;
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
        if (!canTarget(standEntity, target, standPower, action)) {
            target = ActionTarget.EMPTY;
        }
        
        StandEntityTask task = new StandEntityTask(action, ticks, phase, armsOnlyMode, target, offset);
        
        return task;
    }
    
    boolean setTarget(StandEntity standEntity, ActionTarget target, IStandPower standPower, boolean targetCheck) {
        if (!targetCheck || canTarget(standEntity, target, standPower, action)) {
            boolean targetChanged = !target.sameTarget(this.target);
            this.target = target;
            return targetChanged;
        }
        return false;
    }

    private static boolean canTarget(StandEntity standEntity, ActionTarget target, IStandPower standPower, StandEntityAction action) {
        return action.canStandKeepTarget(target, standEntity, standPower);
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
        if (!standEntity.level.isClientSide() && target.getType() == TargetType.ENTITY) {
            Entity targetEntity = target.getEntity();
            if (targetEntity == null || !targetEntity.isAlive() || targetEntity.is(standEntity)) {
                standEntity.setTaskTarget(ActionTarget.EMPTY);
            }
        }
        
        int phaseTicks = startingTicks - ticksLeft;
        switch (phase) {
        case BUTTON_HOLD:
            action.standTickButtonHold(standEntity.level, standEntity, standPower, this);
            break;
        case WINDUP:
            action.standTickWindup(standEntity.level, standEntity, standPower, this);
            break;
        case PERFORM:
            if (phaseTicks == 0) {
            	action.standPerform(standEntity.level, standEntity, standPower, this);
                if (!standEntity.level.isClientSide()) {
                	standPower.consumeStamina(action.getStaminaCost(standPower));
                }
            }
            
            action.standTickPerform(standEntity.level, standEntity, standPower, this);
            if (!standEntity.level.isClientSide()) {
            	standPower.consumeStamina(action.getStaminaCostTicking(standPower));
            }
            break;
        case RECOVERY:
            action.standTickRecovery(standEntity.level, standEntity, standPower, this);
            break;
        }
        
        ticksLeft--;
        if (ticksLeft <= 0) {
            moveToPhase(phase.getNextPhase(), standPower, standEntity);
        }
    }

    public void moveToPhase(@Nullable StandEntityAction.Phase phase, IStandPower standPower, StandEntity standEntity) {
        if (phase == null) {
            standEntity.stopTask();
            return;
        }
        int ticks;
        switch (phase) {
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
        if (setPhase(phase, ticks)) {
            action.playSound(standEntity, standPower, phase, this);
            action.onPhaseSet(standEntity.level, standEntity, standPower, phase, this, ticks);
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
    
    boolean rotateStand(StandEntity standEntity, boolean limitBySpeed) {
        if (target.getType() != TargetType.EMPTY && !standEntity.isManuallyControlled()) {
    		standEntity.rotateTowards(target, limitBySpeed);
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
    
    public float getTaskCompletion(float partialTick) {
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
            return Optional.of(task);
        }

        @Override
        public Optional<StandEntityTask> copy(Optional<StandEntityTask> value) {
            if (value.isPresent()) {
                StandEntityTask task = value.get();
                StandEntityTask taskNew = new StandEntityTask(task.action, task.startingTicks, task.phase, false, task.target, task.offsetFromUser);
                taskNew.ticksLeft = task.ticksLeft;
                taskNew.offsetFromUser = task.offsetFromUser;
                return Optional.of(taskNew);
            }
            return Optional.empty();
        }
    });
}
