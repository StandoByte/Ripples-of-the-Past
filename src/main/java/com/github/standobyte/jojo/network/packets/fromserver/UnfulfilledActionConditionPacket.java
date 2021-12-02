package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ui.ActionsOverlayGui;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class UnfulfilledActionConditionPacket {
    private ITextComponent message;
    
    public UnfulfilledActionConditionPacket(ITextComponent message) {
        this.message = message;
    }

    public static void encode(UnfulfilledActionConditionPacket msg, PacketBuffer buf) {
        buf.writeComponent(msg.message);
    }

    public static UnfulfilledActionConditionPacket decode(PacketBuffer buf) {
        return new UnfulfilledActionConditionPacket(buf.readComponent());
    }

    public static void handle(UnfulfilledActionConditionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ActionsOverlayGui.getInstance().setUnfulfilledConditionText(msg.message);
        });
        ctx.get().setPacketHandled(true);
    }
}
