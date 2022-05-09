package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.actions.TimeStop;
import com.github.standobyte.jojo.capability.world.TimeStopInstance;
import com.github.standobyte.jojo.client.ClientEventHandler;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.util.TimeUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncWorldTimeStopPacket {
    private final int timeStopTicks;
    private final ChunkPos chunkPos;
    private final boolean canSee;
    private final boolean canMove;
    private final int timeStopperId;
    private final TimeStop action;
    private final int instanceId;
    
    public static SyncWorldTimeStopPacket timeResumed(int instanceId) {
        return new SyncWorldTimeStopPacket(0, instanceId, null, true, true, -1, null);
    }
    
    public SyncWorldTimeStopPacket(int timeStopTicks, int instanceId, ChunkPos chunkPos, 
            boolean canSee, boolean canMove, int timeStopperId, TimeStop action) {
        this.timeStopTicks = timeStopTicks;
        this.chunkPos = chunkPos;
        this.canSee = canSee;
        this.canMove = canMove;
        this.timeStopperId = timeStopperId;
        this.action = action;
        this.instanceId = instanceId;
    }
    
    public static void encode(SyncWorldTimeStopPacket msg, PacketBuffer buf) {
        buf.writeVarInt(msg.timeStopTicks);
        buf.writeInt(msg.instanceId);
        
        if (msg.timeStopTicks > 0) {
            byte flags = 0;
            if (msg.timeStopTicks > 0 && msg.canSee) {
                flags |= 1;
                if (msg.canMove) {
                    flags |= 2;
                }
            }
            buf.writeByte(flags);
            
            buf.writeInt(msg.chunkPos.x);
            buf.writeInt(msg.chunkPos.z);
            buf.writeInt(msg.timeStopperId);
            buf.writeBoolean(msg.action != null);
            if (msg.action != null) {
                buf.writeRegistryId(msg.action);
            }
        }
    }
    
    public static SyncWorldTimeStopPacket decode(PacketBuffer buf) {
        int ticks = buf.readVarInt();
        int id = buf.readInt();
        if (ticks > 0) {
            byte flags = buf.readByte();
            ChunkPos pos = new ChunkPos(buf.readInt(), buf.readInt());
            int timeStopperId = buf.readInt();
            Action<?> action = buf.readBoolean() ? buf.readRegistryIdSafe(Action.class) : null;
            TimeStop timeStop = action instanceof TimeStop ? (TimeStop) action : null;
            return new SyncWorldTimeStopPacket(ticks, id, pos, 
                    (flags & 1) > 0, (flags & 2) > 0, timeStopperId, timeStop);
        }
        else {
            return SyncWorldTimeStopPacket.timeResumed(id);
        }
    }

    public static void handle(SyncWorldTimeStopPacket msg, Supplier<NetworkEvent.Context> ctx) {
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
            ClientEventHandler.getInstance().setTimeStopClientState(msg.timeStopTicks, msg.chunkPos, msg.canSee, msg.canMove);
        });
        ctx.get().setPacketHandled(true);
    }
}
