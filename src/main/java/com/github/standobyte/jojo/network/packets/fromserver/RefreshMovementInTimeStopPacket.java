package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.client.ClientTimeStopHandler;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class RefreshMovementInTimeStopPacket {
    private final int entityId;
    private final ChunkPos chunkPos;
    private final boolean canMove;
    
    public RefreshMovementInTimeStopPacket(int entityId, ChunkPos chunkPos, boolean canMove) {
        this.entityId = entityId;
        this.chunkPos = chunkPos;
        this.canMove = canMove;
    }
    
    
    
    public static class Handler implements IModPacketHandler<RefreshMovementInTimeStopPacket> {

        @Override
        public void encode(RefreshMovementInTimeStopPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeInt(msg.chunkPos.x);
            buf.writeInt(msg.chunkPos.z);;
            buf.writeBoolean(msg.canMove);
        }

        @Override
        public RefreshMovementInTimeStopPacket decode(PacketBuffer buf) {
            return new RefreshMovementInTimeStopPacket(buf.readInt(), new ChunkPos(buf.readInt(), buf.readInt()), buf.readBoolean());
        }

        @Override
        public void handle(RefreshMovementInTimeStopPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity != null) {
                entity.level.getCapability(WorldUtilCapProvider.CAPABILITY).orElseThrow(() -> new IllegalStateException("World util capability is empty."))
                .getTimeStopHandler().updateEntityTimeStop(entity, msg.canMove, false);
                if (entity.is(ClientUtil.getClientPlayer())) {
                    ClientTimeStopHandler.getInstance().updateCanMoveInStoppedTime(msg.canMove, msg.chunkPos);
                }
            }
        }

        @Override
        public Class<RefreshMovementInTimeStopPacket> getPacketClass() {
            return RefreshMovementInTimeStopPacket.class;
        }
    }
}
