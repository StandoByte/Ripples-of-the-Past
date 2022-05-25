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

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.util.math.vector.Vector3d;
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
        if (!action.standTakesCrosshairTarget(target, standEntity, standPower) || !canTarget(standEntity, target, standPower, action)) {
            target = ActionTarget.EMPTY;
        }
        
        StandEntityTask task = new StandEntityTask(action, ticks, phase, armsOnlyMode, target, offset);
        
        return task;
    }
    
    boolean setTarget(StandEntity standEntity, ActionTarget target, IStandPower standPower) {
        if (canTarget(standEntity, target, standPower, action)) {
            boolean targetChanged = !target.sameTarget(this.target);
            this.target = target;
            return targetChanged;
        }
        return false;
    }
    
    private static boolean canTarget(StandEntity standEntity, ActionTarget target, IStandPower standPower, StandEntityAction action) {
        return target != null && 
                (standEntity == null || target.getType() == TargetType.EMPTY || action.canStandTarget(standEntity, target, standPower));
    }
    
    ActionTarget getTarget() {
        return target;
    }

    void tick(IStandPower standPower, StandEntity standEntity) {
        if (target.getType() != TargetType.EMPTY) {
//            if (!standEntity.level.isClientSide() && ticksLeft < startingTicks) {
//                double angleCos = standEntity.getLookAngle().dot(target.getTargetPos().subtract(standEntity.getEyePosition(1.0F)).normalize());
//            }
//            if (!standEntity.isManuallyControlled() && target.getType() == TargetType.BLOCK) {
//                setTarget(ActionTarget.EMPTY);
//            }
            rotateStand(standEntity, true);
        }
        if (!standEntity.level.isClientSide() && target.getType() == TargetType.ENTITY) {
            Entity targetEntity = target.getEntity(standEntity.level);
            if (targetEntity == null || !targetEntity.isAlive() || targetEntity.is(standEntity)) {
                standEntity.setTaskTarget(ActionTarget.EMPTY);
            }
        }
        
        if (phase == StandEntityAction.Phase.PERFORM) {
            if (!standEntity.level.isClientSide()) {
                standPower.consumeStamina(action.getStaminaCostTicking(standPower));
            }
            if (ticksLeft == startingTicks) {
                action.standPerform(standEntity.level, standEntity, standPower, target);
            }
        }
        int phaseTicks = startingTicks - ticksLeft;
        switch (phase) {
        case BUTTON_HOLD:
            action.standTickButtonHold(standEntity.level, standEntity, 
                    phaseTicks, standPower, target);
            break;
        case WINDUP:
            action.standTickWindup(standEntity.level, standEntity, 
                    phaseTicks, standPower, target);
            break;
        case PERFORM:
            action.standTickPerform(standEntity.level, standEntity, 
                    phaseTicks, standPower, target);
            break;
        case RECOVERY:
            action.standTickRecovery(standEntity.level, standEntity, 
                    phaseTicks, standPower, target);
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
            action.playSound(standEntity, standPower, phase, target);
            action.onPhaseSet(standEntity.level, standEntity, standPower, phase, target, ticks);
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
        this.ticksLeft += ticks;
    }
    
    private void rotateStand(StandEntity standEntity, boolean limitBySpeed) {
    	limitBySpeed = false;
        if (!standEntity.isManuallyControlled()) {
            standEntity.rotatedTowardsTarget = true;
            Vector3d targetPos = target.getTargetPos(true);
            if (targetPos != null) {
                standEntity.rotateTowards(targetPos, limitBySpeed);
            }
        }
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
    
    public float getTaskCompletion(float partialTick) {
        return Math.min(1F - ((float) ticksLeft - partialTick) / (float) startingTicks, 1F);
    }
    
    public StandEntityAction.Phase getPhase() {
        return phase;
    }
    
    void setOffsetFromUser(StandRelativeOffset offset) {
        this.offsetFromUser = offset;
    }
    
    @Nullable
    StandRelativeOffset getOffsetFromUser() {
        return offsetFromUser;
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
                if (taskNew.target.getType() == TargetType.ENTITY) {
                    Entity entity = taskNew.target.getEntity(null);
                    if (entity != null) {
                        taskNew.target = new ActionTarget(entity.getId());
                    }
                }
                taskNew.offsetFromUser = task.offsetFromUser;
                return Optional.of(taskNew);
            }
            return Optional.empty();
        }
    });
}
