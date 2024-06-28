package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClHasInputPacket {
    private final boolean hasInput;
    private final boolean wallClimbing;
    
    public ClHasInputPacket(boolean hasInput) {
        this(hasInput, false);
    }
    
    public static ClHasInputPacket wallClimbing(boolean hasMotion) {
        return new ClHasInputPacket(hasMotion, true);
    }
    
    private ClHasInputPacket(boolean hasInput, boolean wallClimbing) {
        this.hasInput = hasInput;
        this.wallClimbing = wallClimbing;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClHasInputPacket> {
    
        @Override
        public void encode(ClHasInputPacket msg, PacketBuffer buf) {
            buf.writeBoolean(msg.hasInput);
            buf.writeBoolean(msg.wallClimbing);
        }

        @Override
        public ClHasInputPacket decode(PacketBuffer buf) {
            return new ClHasInputPacket(buf.readBoolean(), buf.readBoolean());
        }

        @Override
        public void handle(ClHasInputPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            if (msg.wallClimbing) {
                player.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.wallClimbIsMoving = msg.hasInput);
            }
            else {
                player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.setHasClientInput(msg.hasInput));
            }
        }

        @Override
        public Class<ClHasInputPacket> getPacketClass() {
            return ClHasInputPacket.class;
        }
    }
}
