package com.github.standobyte.jojo.mechanics.speechbubble.clowning;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class SpeechChatTypingPlayersPacket {
    //private RegistryKey<World> dimension;
    private Collection<UUID> players;
    
    public SpeechChatTypingPlayersPacket(RegistryKey<World> dimension, Collection<UUID> players) {
        //this.dimension = dimension;
        this.players = players;
    }
    
    
    
    public static class Handler implements IModPacketHandler<SpeechChatTypingPlayersPacket> {

        @Override
        public void encode(SpeechChatTypingPlayersPacket msg, PacketBuffer buf) {
            NetworkUtil.writeCollection(buf, msg.players, elem -> buf.writeUUID(elem), false);
        }

        @Override
        public SpeechChatTypingPlayersPacket decode(PacketBuffer buf) {
            RegistryKey<World> dimension = null;
            Collection<UUID> players = NetworkUtil.readCollection(buf, PacketBuffer::readUUID);
            return new SpeechChatTypingPlayersPacket(dimension, players);
        }
        
        @Override
        public void handle(SpeechChatTypingPlayersPacket msg, Supplier<NetworkEvent.Context> ctx) {
            World level = ClientUtil.getClientWorld();
            WorldTypingPlayers.clientSideReceivedState(level, msg.players);
        }

        @Override
        public Class<SpeechChatTypingPlayersPacket> getPacketClass() {
            return SpeechChatTypingPlayersPacket.class;
        }
    }
}
