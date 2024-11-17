package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.player.PlayerClientBroadcastedSettings;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrPlayerModSettingsPacket {
    private final int entityId;
    private PlayerClientBroadcastedSettings settings;
    private PacketBuffer settingsData;
    
    public TrPlayerModSettingsPacket(int entityId, PlayerClientBroadcastedSettings settings) {
        this.entityId = entityId;
        this.settings = settings;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrPlayerModSettingsPacket> {

        @Override
        public void encode(TrPlayerModSettingsPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            msg.settings.toBuf(buf);
        }

        @Override
        public TrPlayerModSettingsPacket decode(PacketBuffer buf) {
            TrPlayerModSettingsPacket packet = new TrPlayerModSettingsPacket(buf.readInt(), null);
            packet.settingsData = buf;
            return packet;
        }

        @Override
        public void handle(TrPlayerModSettingsPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof PlayerEntity) {
                PlayerClientBroadcastedSettings.getPlayerSettings((PlayerEntity) entity).ifPresent(serverSettings -> {
                    serverSettings.fromBuf(msg.settingsData);
                });
            }
        }

        @Override
        public Class<TrPlayerModSettingsPacket> getPacketClass() {
            return TrPlayerModSettingsPacket.class;
        }
    }
}
