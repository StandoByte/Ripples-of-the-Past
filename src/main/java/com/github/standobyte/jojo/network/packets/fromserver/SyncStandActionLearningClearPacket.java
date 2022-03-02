package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncStandActionLearningClearPacket {
    
    public SyncStandActionLearningClearPacket() {
    }
    
    public static void encode(SyncStandActionLearningClearPacket msg, PacketBuffer buf) {
    }

    public static SyncStandActionLearningClearPacket decode(PacketBuffer buf) {
        return new SyncStandActionLearningClearPacket();
    }

    public static void handle(SyncStandActionLearningClearPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.clearActionLearning();
            });
        });
        ctx.get().setPacketHandled(true);
    }    
}
