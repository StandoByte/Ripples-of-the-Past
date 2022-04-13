package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncMaxAchievedResolvePacket {
    private final float value;
    
    public SyncMaxAchievedResolvePacket(float value) {
        this.value = value;
    }
    
    public static void encode(SyncMaxAchievedResolvePacket msg, PacketBuffer buf) {
        buf.writeFloat(msg.value);
    }
    
    public static SyncMaxAchievedResolvePacket decode(PacketBuffer buf) {
        return new SyncMaxAchievedResolvePacket(buf.readFloat());
    }

    public static void handle(SyncMaxAchievedResolvePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.getResolveCounter().setMaxAchievedValue(msg.value);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
