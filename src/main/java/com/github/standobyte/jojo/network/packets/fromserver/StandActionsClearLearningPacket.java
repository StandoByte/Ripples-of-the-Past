package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class StandActionsClearLearningPacket {
    
    public StandActionsClearLearningPacket() {
    }
    
    public static void encode(StandActionsClearLearningPacket msg, PacketBuffer buf) {
    }

    public static StandActionsClearLearningPacket decode(PacketBuffer buf) {
        return new StandActionsClearLearningPacket();
    }

    public static void handle(StandActionsClearLearningPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.clearActionLearning();
            });
        });
        ctx.get().setPacketHandled(true);
    }    
}
