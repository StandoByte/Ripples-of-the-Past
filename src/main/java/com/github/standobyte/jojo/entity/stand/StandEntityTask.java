package com.github.standobyte.jojo.entity.stand;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

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
    StandEntityAction.Phase phase;
    
    StandEntityTask(StandEntityAction action, int ticks, StandEntityAction.Phase phase) {
        this.action = action;
        this.startingTicks = Math.max(ticks, 1);
        this.ticksLeft = this.startingTicks;
        this.phase = phase;
    }
    
    void tick(IStandPower standPower, StandEntity standEntity) {
        if (startingTicks == 1 && phase == StandEntityAction.Phase.PERFORM) {
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
        }
        
        ticksLeft--;
        if (ticksLeft <= 0) {
            switch (phase) {
            case WINDUP:
                phase = StandEntityAction.Phase.PERFORM;
                this.startingTicks = action.getStandActionTicks(standPower, standEntity);
                this.ticksLeft = startingTicks;
                break;
            case PERFORM:
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
    
    public float getTaskCompletion() {
        return getTaskCompletion(0);
    }
    
    public float getTaskCompletion(float partialTick) {
        return 1F - ((float) ticksLeft - partialTick) / (float) startingTicks;
    }
    
    public StandEntityAction.Phase getPhase() {
        return phase;
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
            
            int ticks = buf.readInt();
            StandEntityAction.Phase phase = buf.readEnum(StandEntityAction.Phase.class);

            return Optional.of(new StandEntityTask((StandEntityAction) action, ticks, phase));
        }

        @Override
        public Optional<StandEntityTask> copy(Optional<StandEntityTask> value) {
            if (value.isPresent()) {
                StandEntityTask task = value.get();
                StandEntityTask taskNew = new StandEntityTask(task.action, task.startingTicks, task.phase);
                taskNew.ticksLeft = task.ticksLeft;
                return Optional.of(taskNew);
            }
            return Optional.empty();
        }
    });
}
