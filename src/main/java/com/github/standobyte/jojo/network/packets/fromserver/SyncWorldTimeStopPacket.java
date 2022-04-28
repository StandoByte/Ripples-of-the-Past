package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.capability.world.TimeStopInstance;
import com.github.standobyte.jojo.client.ClientEventHandler;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.util.TimeUtil;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncWorldTimeStopPacket {
    private final int timeStopTicks;
    private final ChunkPos chunkPos;
    private final boolean canSee;
    private final boolean canMove;
    private final int instanceId;
    
    public static SyncWorldTimeStopPacket timeResumed(ChunkPos chunkPos, int instanceId) {
        return new SyncWorldTimeStopPacket(0, chunkPos, true, true, instanceId);
    }
    
    public SyncWorldTimeStopPacket(int timeStopTicks, ChunkPos chunkPos, boolean canSee, boolean canMove, int instanceId) {
        this.timeStopTicks = timeStopTicks;
        this.chunkPos = chunkPos;
        this.canSee = canSee;
        this.canMove = canMove;
        this.instanceId = instanceId;
    }
    
    public static void encode(SyncWorldTimeStopPacket msg, PacketBuffer buf) {
        byte flags = 0;
        if (msg.timeStopTicks > 0 && msg.canSee) {
            flags |= 1;
            if (msg.canMove) {
                flags |= 2;
            }
        }
        buf.writeByte(flags);
        buf.writeVarInt(msg.timeStopTicks);
        buf.writeInt(msg.chunkPos.x);
        buf.writeInt(msg.chunkPos.z);
        buf.writeInt(msg.instanceId);
    }
    
    public static SyncWorldTimeStopPacket decode(PacketBuffer buf) {
        byte flags = buf.readByte();
        return new SyncWorldTimeStopPacket(buf.readVarInt(), new ChunkPos(buf.readInt(), buf.readInt()), (flags & 1) > 0, (flags & 2) > 0, buf.readInt());
    }

    public static void handle(SyncWorldTimeStopPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World world = ClientUtil.getClientWorld();
            if (msg.timeStopTicks > 0) {
                TimeUtil.stopTime(world, TimeStopInstance.withoutSounds(world, msg.timeStopTicks, msg.chunkPos, 
                        JojoModConfig.getCommonConfigInstance(true).timeStopChunkRange.get(), msg.instanceId));
            }
            else {
                TimeUtil.resumeTime(world, msg.instanceId);
            }
            ClientEventHandler.getInstance().setTimeStopClientState(msg.timeStopTicks, msg.chunkPos, msg.canSee, msg.canMove);
        });
        ctx.get().setPacketHandled(true);
    }
}
