package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SkippedStandProgressionPacket {
    
    public SkippedStandProgressionPacket() {
    }
    
    public static void encode(SkippedStandProgressionPacket msg, PacketBuffer buf) {
    }
    
    public static SkippedStandProgressionPacket decode(PacketBuffer buf) {
        return new SkippedStandProgressionPacket();
    }

    public static void handle(SkippedStandProgressionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.setProgressionSkipped();
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
