package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ResetResolveValuePacket {
    
    public ResetResolveValuePacket() {
    }
    
    public static void encode(ResetResolveValuePacket msg, PacketBuffer buf) {
    }
    
    public static ResetResolveValuePacket decode(PacketBuffer buf) {
        return new ResetResolveValuePacket();
    }

    public static void handle(ResetResolveValuePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.getResolveCounter().resetResolveValue();
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
