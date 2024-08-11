package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.network.packets.fromserver.SyncMotionAnimStatePacket;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClSyncMotionAnimPacket {
    public final boolean isMoving;
    public final double movementUp;
    public final double movementLeft;
    public final float speed;

    public ClSyncMotionAnimPacket(boolean isMoving, double movementUp, double movementLeft, float speed) {
        this.isMoving = isMoving;
        this.movementUp = movementUp;
        this.movementLeft = movementLeft;
        this.speed = speed;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClSyncMotionAnimPacket> {

        @Override
        public void encode(ClSyncMotionAnimPacket msg, PacketBuffer buf) {
            buf.writeBoolean(msg.isMoving);
            buf.writeDouble(msg.movementUp);
            buf.writeDouble(msg.movementLeft);
            buf.writeFloat(msg.speed);
        }

        @Override
        public ClSyncMotionAnimPacket decode(PacketBuffer buf) {
            return new ClSyncMotionAnimPacket(buf.readBoolean(), buf.readDouble(), buf.readDouble(), buf.readFloat());
        }
    
        @Override
        public void handle(ClSyncMotionAnimPacket msg, Supplier<NetworkEvent.Context> ctx) {
            if (Double.isFinite(msg.movementUp) && Double.isFinite(msg.movementLeft) && Float.isFinite(msg.speed)) {
                PlayerEntity player = ctx.get().getSender();
                PacketManager.sendToClientsTracking(new SyncMotionAnimStatePacket(player.getId(), msg), player);
            }
        }

        @Override
        public Class<ClSyncMotionAnimPacket> getPacketClass() {
            return ClSyncMotionAnimPacket.class;
        }
    }

}
