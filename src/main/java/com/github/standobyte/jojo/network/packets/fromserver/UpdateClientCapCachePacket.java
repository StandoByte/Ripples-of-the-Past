package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.ControllerSoul;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
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
            ControllerSoul.getInstance().updateStandCache();
            ClientUtil.updatePowersCapCache();
        }

        @Override
        public Class<UpdateClientCapCachePacket> getPacketClass() {
            return UpdateClientCapCachePacket.class;
        }
    }
}
