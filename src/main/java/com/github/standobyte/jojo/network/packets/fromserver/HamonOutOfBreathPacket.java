package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class HamonOutOfBreathPacket {
    
    public HamonOutOfBreathPacket() {}
    
    
    
    public static class Handler implements IModPacketHandler<HamonOutOfBreathPacket> {

        @Override
        public void encode(HamonOutOfBreathPacket msg, PacketBuffer buf) {
        }

        @Override
        public HamonOutOfBreathPacket decode(PacketBuffer buf) {
            return new HamonOutOfBreathPacket();
        }

        @Override
        public void handle(HamonOutOfBreathPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ActionsOverlayGui.getInstance().setOutOfBreath(false);
        }

        @Override
        public Class<HamonOutOfBreathPacket> getPacketClass() {
            return HamonOutOfBreathPacket.class;
        }
    }
}
