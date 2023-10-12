package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.TimeStop;
import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.capability.world.TimeStopInstance;
import com.github.standobyte.jojo.client.ClientTimeStopHandler;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.render.world.shader.ShaderEffectApplier;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class TimeStopInstancePacket {
    private final PacketType packetType;
    private final int timeStopTicks;
    private final ChunkPos chunkPos;
    private final int timeStopperId;
    private final TimeStop action;
    private final int instanceId;
    
    public static TimeStopInstancePacket timeResumed(int instanceId) {
        return new TimeStopInstancePacket(PacketType.RESUME_TIME, 0, instanceId, null, -1, null);
    }
    
    public static TimeStopInstancePacket setTicks(int instanceId, int ticks) {
        return new TimeStopInstancePacket(PacketType.SET_TICKS, ticks, instanceId, null, -1, null);
    }
    
    public TimeStopInstancePacket(int timeStopTicks, int instanceId, 
            ChunkPos chunkPos, int timeStopperId, TimeStop action) {
        this(PacketType.NEW_INSTANCE, timeStopTicks, instanceId, chunkPos, timeStopperId, action);
    }
    
    private TimeStopInstancePacket(PacketType packetType, int timeStopTicks, 
            int instanceId, ChunkPos chunkPos, int timeStopperId, TimeStop action) {
        this.packetType = packetType;
        this.timeStopTicks = timeStopTicks;
        this.chunkPos = chunkPos;
        this.timeStopperId = timeStopperId;
        this.action = action;
        this.instanceId = instanceId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TimeStopInstancePacket> {

        @Override
        public void encode(TimeStopInstancePacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.packetType);
            buf.writeInt(msg.instanceId);
            switch (msg.packetType) {
            case NEW_INSTANCE:
                buf.writeVarInt(msg.timeStopTicks);
                buf.writeInt(msg.chunkPos.x);
                buf.writeInt(msg.chunkPos.z);
                buf.writeInt(msg.timeStopperId);
                buf.writeBoolean(msg.action != null);
                if (msg.action != null) {
                    buf.writeRegistryId(msg.action);
                }
                break;
            case SET_TICKS:
                buf.writeVarInt(msg.timeStopTicks);
                break;
            case RESUME_TIME:
                break;
            }
        }

        @Override
        public TimeStopInstancePacket decode(PacketBuffer buf) {
            PacketType packetType = buf.readEnum(PacketType.class);
            int id = buf.readInt();
            int ticks;
            switch (packetType) {
            case NEW_INSTANCE:
                ticks = buf.readVarInt();
                ChunkPos pos = new ChunkPos(buf.readInt(), buf.readInt());
                int timeStopperId = buf.readInt();
                Action<?> action = buf.readBoolean() ? buf.readRegistryIdSafe(Action.class) : null;
                TimeStop timeStop = action instanceof TimeStop ? (TimeStop) action : null;
                return new TimeStopInstancePacket(ticks, id, pos, timeStopperId, timeStop);
            case SET_TICKS:
                ticks = buf.readVarInt();
                return TimeStopInstancePacket.setTicks(id, ticks);
            case RESUME_TIME:
                return TimeStopInstancePacket.timeResumed(id);
            }
            throw new NoSuchElementException("Unknown TimeStopInstancePacket type");
        }

        @Override
        public void handle(TimeStopInstancePacket msg, Supplier<NetworkEvent.Context> ctx) {
            World world = ClientUtil.getClientWorld();
            switch (msg.packetType) {
            case NEW_INSTANCE:
                if (msg.timeStopTicks > 0) {
                    Entity entity = ClientUtil.getEntityById(msg.timeStopperId);
                    TimeStopHandler.stopTime(world, new TimeStopInstance(world, msg.timeStopTicks, msg.chunkPos, 
                            JojoModConfig.getCommonConfigInstance(true).timeStopChunkRange.get(), 
                            entity instanceof LivingEntity ? (LivingEntity) entity : null, msg.action,
                                    msg.instanceId));
                    ClientTimeStopHandler.getInstance().updateTimeStopTicksLeft();
                    ShaderEffectApplier.getInstance().setTimeStopVisuals(entity, msg.action);
                }
                break;
            case SET_TICKS:
                TimeStopInstance timeStop = TimeStopHandler.getTimeStopInstance(world, msg.instanceId);
                if (timeStop != null) {
                    timeStop.setTicksLeft(msg.timeStopTicks);
                }
                ClientTimeStopHandler.getInstance().updateTimeStopTicksLeft();
                break;
            case RESUME_TIME:
                TimeStopHandler.resumeTime(world, msg.instanceId);
                break;
            }
        }

        @Override
        public Class<TimeStopInstancePacket> getPacketClass() {
            return TimeStopInstancePacket.class;
        }
    }
    
    private enum PacketType {
        NEW_INSTANCE,
        SET_TICKS,
        RESUME_TIME
    }
}
