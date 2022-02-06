package com.github.standobyte.jojo.entity.stand;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.actions.StandEntityAction;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraftforge.registries.DataSerializerEntry;

public class StandEntityTask {
    @Nonnull
    private final StandEntityAction action;
    private int startingTicks;
    private int ticksLeft;
    @Nonnull
    private StandEntityAction.Phase phase;
    @Nullable
    private StandRelativeOffset offsetFromUser; // isn't synced to clients
    
    StandEntityTask(StandEntityAction action, int ticks, StandEntityAction.Phase phase, boolean armsOnlyMode) {
        this.action = action;
        this.startingTicks = Math.max(ticks, 1);
        this.ticksLeft = this.startingTicks;
        this.phase = phase;
        this.offsetFromUser = action.getOffsetFromUser(armsOnlyMode);
    }
    
    void tick(IStandPower standPower, StandEntity standEntity) {
        if (phase == StandEntityAction.Phase.PERFORM && ticksLeft == startingTicks) {
            action.standPerform(standEntity.level, standEntity, standPower, standEntity.getTaskTarget());
        }
        switch (phase) {
        case BUTTON_HOLD:
            action.standTickButtonHold(standEntity.level, standEntity, 
                    startingTicks - ticksLeft, standPower, standEntity.getTaskTarget());
            break;
        case WINDUP:
            action.standTickWindup(standEntity.level, standEntity, 
                    startingTicks - ticksLeft, standPower, standEntity.getTaskTarget());
            break;
        case PERFORM:
            action.standTickPerform(standEntity.level, standEntity, 
                    startingTicks - ticksLeft, standPower, standEntity.getTaskTarget());
            break;
        case RECOVERY:
            break;
        }
        
        ticksLeft--;
        if (ticksLeft <= 0) {
            switch (phase) {
            case WINDUP:
                this.phase = StandEntityAction.Phase.PERFORM;
                this.startingTicks = action.getStandActionTicks(standPower, standEntity);
                this.ticksLeft = startingTicks;
                break;
            case PERFORM:
                int recoveryTicks = action.getStandRecoveryTicks(standPower, standEntity);
                if (recoveryTicks > 0) {
                    this.phase = StandEntityAction.Phase.RECOVERY;
                    this.startingTicks = recoveryTicks;
                    this.ticksLeft = startingTicks;
                }
                else {
                    standEntity.stopTask();
                }
                break;
            case RECOVERY:
                standEntity.stopTask();
                break;
            default:
                break;
            }
        }
    }
    
    public StandEntityAction getAction() {
        return action;
    }
    
    public int getTicksLeft() {
        return ticksLeft;
    }
    
    public float getTaskCompletion(float partialTick) {
        return 1F - ((float) ticksLeft - partialTick) / (float) startingTicks;
    }
    
    public StandEntityAction.Phase getPhase() {
        return phase;
    }
    
    void setOffsetFromUser(StandRelativeOffset offset) {
        this.offsetFromUser = offset;
    }
    
    // FIXME (!) glitches when too close to the task target
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

            return Optional.of(new StandEntityTask((StandEntityAction) action, ticks, phase, false));
        }

        @Override
        public Optional<StandEntityTask> copy(Optional<StandEntityTask> value) {
            if (value.isPresent()) {
                StandEntityTask task = value.get();
                StandEntityTask taskNew = new StandEntityTask(task.action, task.startingTicks, task.phase, false);
                taskNew.ticksLeft = task.ticksLeft;
                return Optional.of(taskNew);
            }
            return Optional.empty();
        }
    });
}
