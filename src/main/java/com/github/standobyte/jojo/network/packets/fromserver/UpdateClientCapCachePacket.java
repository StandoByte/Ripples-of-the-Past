package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.SoulController;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class UpdateClientCapCachePacket {

    public static void encode(UpdateClientCapCachePacket msg, PacketBuffer buf) {}

    public static UpdateClientCapCachePacket decode(PacketBuffer buf) {
        return new UpdateClientCapCachePacket();
    }

    public static void handle(UpdateClientCapCachePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ActionsOverlayGui.getInstance().updatePowersCache();
            InputHandler.getInstance().updatePowersCache();
            SoulController.getInstance().updateStandCache();
        });
        ctx.get().setPacketHandled(true);
    }
}
