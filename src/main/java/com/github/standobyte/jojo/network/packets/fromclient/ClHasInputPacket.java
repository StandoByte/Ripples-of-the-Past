package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClHasInputPacket {
    private final boolean hasInput;
    
    public ClHasInputPacket(boolean hasInput) {
        this.hasInput = hasInput;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClHasInputPacket> {
    
        @Override
        public void encode(ClHasInputPacket msg, PacketBuffer buf) {
            buf.writeBoolean(msg.hasInput);
        }

        @Override
        public ClHasInputPacket decode(PacketBuffer buf) {
            return new ClHasInputPacket(buf.readBoolean());
        }

        @Override
        public void handle(ClHasInputPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.setHasClientInput(msg.hasInput));
        }

        @Override
        public Class<ClHasInputPacket> getPacketClass() {
            return ClHasInputPacket.class;
        }
    }
}
