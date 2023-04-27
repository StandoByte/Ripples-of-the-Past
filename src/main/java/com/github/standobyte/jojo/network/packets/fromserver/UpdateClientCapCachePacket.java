package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.SoulController;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class UpdateClientCapCachePacket {
    
    
    
    public static class Handler implements IModPacketHandler<UpdateClientCapCachePacket> {

        @Override
        public void encode(UpdateClientCapCachePacket msg, PacketBuffer buf) {}

        @Override
        public UpdateClientCapCachePacket decode(PacketBuffer buf) {
            return new UpdateClientCapCachePacket();
        }

        @Override
        public void handle(UpdateClientCapCachePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ActionsOverlayGui.getInstance().updatePowersCache();
            InputHandler.getInstance().updatePowersCache();
            SoulController.getInstance().updateStandCache();
        }

        @Override
        public Class<UpdateClientCapCachePacket> getPacketClass() {
            return UpdateClientCapCachePacket.class;
        }
    }
}
