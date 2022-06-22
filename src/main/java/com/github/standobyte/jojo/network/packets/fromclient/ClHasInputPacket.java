package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClHasInputPacket {
    private final boolean hasInput;
    
    public ClHasInputPacket(boolean hasInput) {
        this.hasInput = hasInput;
    }
    
    public static void encode(ClHasInputPacket msg, PacketBuffer buf) {
        buf.writeBoolean(msg.hasInput);
    }
    
    public static ClHasInputPacket decode(PacketBuffer buf) {
        return new ClHasInputPacket(buf.readBoolean());
    }
    
    public static void handle(ClHasInputPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.setHasClientInput(msg.hasInput));
        });
        ctx.get().setPacketHandled(true);
    }
}
