package com.github.standobyte.jojo.mechanics.speechbubble.clowning;

import java.util.function.Supplier;

import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClSpeechChatStatePacket {
    private final boolean state;
    
    public static final ClSpeechChatStatePacket TRUE = new ClSpeechChatStatePacket(true);
    public static final ClSpeechChatStatePacket FALSE = new ClSpeechChatStatePacket(false);
    
    private ClSpeechChatStatePacket(boolean state) {
        this.state = state;
    }
    
    public static ClSpeechChatStatePacket of(boolean state) {
        return state ? TRUE : FALSE;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClSpeechChatStatePacket> {

        @Override
        public void encode(ClSpeechChatStatePacket msg, PacketBuffer buf) {
            buf.writeBoolean(msg.state);
        }

        @Override
        public ClSpeechChatStatePacket decode(PacketBuffer buf) {
            return of(buf.readBoolean());
        }
        
        @Override
        public void handle(ClSpeechChatStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity senderPlayer = ctx.get().getSender();
            WorldTypingPlayers.serverSideStatePacket(senderPlayer, msg.state);
        }

        @Override
        public Class<ClSpeechChatStatePacket> getPacketClass() {
            return ClSpeechChatStatePacket.class;
        }
    }
}
