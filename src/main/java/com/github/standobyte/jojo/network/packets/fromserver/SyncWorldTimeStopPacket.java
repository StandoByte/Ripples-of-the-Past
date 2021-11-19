package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientEventHandler;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.util.TimeHandler;

import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncWorldTimeStopPacket {
    private final boolean timeStop;
    private final boolean canSee;
    private final boolean canMove;
    
    public SyncWorldTimeStopPacket(boolean canSee, boolean canMove) {
        this(true, canSee, canMove);
    }
    
    public static SyncWorldTimeStopPacket timeResumed() {
        return new SyncWorldTimeStopPacket(false, true, true);
    }
    
    private SyncWorldTimeStopPacket(boolean timeStop, boolean canSee, boolean canMove) {
        this.timeStop = timeStop;
        this.canSee = canSee;
        this.canMove = canMove;
    }
    
    public static void encode(SyncWorldTimeStopPacket msg, PacketBuffer buf) {
        byte flags = 0;
        if (msg.timeStop) {
            flags |= 1;
            if (msg.canSee) {
                flags |= 2;
                if (msg.canMove) {
                    flags |= 4;
                }
            }
        }
        buf.writeByte(flags);
    }
    
    public static SyncWorldTimeStopPacket decode(PacketBuffer buf) {
        byte flags = buf.readByte();
        return new SyncWorldTimeStopPacket((flags & 1) > 0, (flags & 2) > 0, (flags & 4) > 0);
    }

    public static void handle(SyncWorldTimeStopPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            World world = ClientUtil.getClientWorld();
            if (msg.timeStop) {
                TimeHandler.stopTime(world, 1);
            }
            else {
                TimeHandler.resumeTime(world, true);
            }
            ClientEventHandler.getInstance().setTimeStopClientState(msg.timeStop, msg.canSee, msg.canMove);
        });
        ctx.get().setPacketHandled(true);
    }
}
