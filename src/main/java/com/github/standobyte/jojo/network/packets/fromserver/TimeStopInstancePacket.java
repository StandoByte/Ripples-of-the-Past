package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.TimeStop;
import com.github.standobyte.jojo.capability.world.TimeStopInstance;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.util.utils.TimeUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class TimeStopInstancePacket {
    private final int timeStopTicks;
    private final ChunkPos chunkPos;
    private final int timeStopperId;
    private final TimeStop action;
    private final int instanceId;
    
    public static TimeStopInstancePacket timeResumed(int instanceId) {
        return new TimeStopInstancePacket(0, instanceId, null, -1, null);
    }
    
    public TimeStopInstancePacket(int timeStopTicks, int instanceId, ChunkPos chunkPos, int timeStopperId, TimeStop action) {
        this.timeStopTicks = timeStopTicks;
        this.chunkPos = chunkPos;
        this.timeStopperId = timeStopperId;
        this.action = action;
        this.instanceId = instanceId;
    }
    
    public static void encode(TimeStopInstancePacket msg, PacketBuffer buf) {
        buf.writeVarInt(msg.timeStopTicks);
        buf.writeInt(msg.instanceId);
        
        if (msg.timeStopTicks > 0) {
            buf.writeInt(msg.chunkPos.x);
            buf.writeInt(msg.chunkPos.z);
            buf.writeInt(msg.timeStopperId);
            buf.writeBoolean(msg.action != null);
            if (msg.action != null) {
                buf.writeRegistryId(msg.action);
            }
        }
    }
    
    public static TimeStopInstancePacket decode(PacketBuffer buf) {
        int ticks = buf.readVarInt();
        int id = buf.readInt();
        if (ticks > 0) {
            ChunkPos pos = new ChunkPos(buf.readInt(), buf.readInt());
            int timeStopperId = buf.readInt();
            Action<?> action = buf.readBoolean() ? buf.readRegistryIdSafe(Action.class) : null;
            TimeStop timeStop = action instanceof TimeStop ? (TimeStop) action : null;
            return new TimeStopInstancePacket(ticks, id, pos, timeStopperId, timeStop);
        }
        else {
            return TimeStopInstancePacket.timeResumed(id);
        }
    }

    public static void handle(TimeStopInstancePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World world = ClientUtil.getClientWorld();
            if (msg.timeStopTicks > 0) {
                Entity entity = ClientUtil.getEntityById(msg.timeStopperId);
                TimeUtil.stopTime(world, new TimeStopInstance(world, msg.timeStopTicks, msg.chunkPos, 
                        JojoModConfig.getCommonConfigInstance(true).timeStopChunkRange.get(), 
                        entity instanceof LivingEntity ? (LivingEntity) entity : null, msg.action,
                                msg.instanceId));
            }
            else {
        		TimeUtil.resumeTime(world, msg.instanceId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
