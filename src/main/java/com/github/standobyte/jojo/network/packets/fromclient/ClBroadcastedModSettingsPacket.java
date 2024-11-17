package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.player.PlayerClientBroadcastedSettings;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClBroadcastedModSettingsPacket {
    private PlayerClientBroadcastedSettings settings;
    private PacketBuffer settingsData;
    
    public ClBroadcastedModSettingsPacket(PlayerClientBroadcastedSettings settings) {
        this.settings = settings;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClBroadcastedModSettingsPacket> {

        @Override
        public void encode(ClBroadcastedModSettingsPacket msg, PacketBuffer buf) {
            msg.settings.toBuf(buf);
        }

        @Override
        public ClBroadcastedModSettingsPacket decode(PacketBuffer buf) {
            ClBroadcastedModSettingsPacket packet = new ClBroadcastedModSettingsPacket(null);
            packet.settingsData = buf;
            return packet;
        }

        @Override
        public void handle(ClBroadcastedModSettingsPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            PlayerClientBroadcastedSettings.getPlayerSettings(player).ifPresent(serverSettings -> {
                serverSettings.fromBuf(msg.settingsData);
                serverSettings.syncToAll(player);
            });
        }

        @Override
        public Class<ClBroadcastedModSettingsPacket> getPacketClass() {
            return ClBroadcastedModSettingsPacket.class;
        }
    }
}
