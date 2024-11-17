package com.github.standobyte.jojo.capability.entity.player;

import java.util.Optional;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientModSettings;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClBroadcastedModSettingsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrPlayerModSettingsPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.HandSide;

public class PlayerClientBroadcastedSettings {
    public HandSide standSide = HandSide.RIGHT;
    
    
    public void toBuf(PacketBuffer buf) {
        buf.writeEnum(standSide);
    }
    
    public void fromBuf(PacketBuffer buf) {
        standSide = buf.readEnum(HandSide.class);
    }
    
    
    public void broadcastToServer() {
        if (Minecraft.getInstance().getConnection() != null) {
            PacketManager.sendToServer(new ClBroadcastedModSettingsPacket(this));
        }
    }
    
    public void syncToAll(PlayerEntity player) {
        PacketManager.sendToClientsTracking(new TrPlayerModSettingsPacket(player.getId(), this), player);
    }
    
    public void syncToTracking(PlayerEntity player, ServerPlayerEntity tracking) {
        PacketManager.sendToClient(new TrPlayerModSettingsPacket(player.getId(), this), tracking);
    }
    
    public static Optional<PlayerClientBroadcastedSettings> getPlayerSettings(PlayerEntity player) {
        if (player.isLocalPlayer()) {
            return Optional.of(ClientModSettings.getSettingsReadOnly().broadcasted);
        }
        return player.getCapability(PlayerUtilCapProvider.CAPABILITY).map(PlayerUtilCap::getBroadcastedSettings);
    }
}
