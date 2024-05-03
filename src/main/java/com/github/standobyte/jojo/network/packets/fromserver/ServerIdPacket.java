package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.UUID;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientEventHandler;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ServerIdPacket {
    public final UUID serverId;
    
    public ServerIdPacket(UUID serverId) {
        this.serverId = serverId;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ServerIdPacket> {

        @Override
        public void encode(ServerIdPacket msg, PacketBuffer buf) {
            buf.writeUUID(msg.serverId);
        }

        @Override
        public ServerIdPacket decode(PacketBuffer buf) {
            UUID serverId = buf.readUUID();
            return new ServerIdPacket(serverId);
        }

        @Override
        public void handle(ServerIdPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ClientEventHandler.getInstance().setServerId(msg);
        }

        @Override
        public Class<ServerIdPacket> getPacketClass() {
            return ServerIdPacket.class;
        }
    }
}
