package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class MaxAchievedResolvePacket {
    private final float value;
    
    public MaxAchievedResolvePacket(float value) {
        this.value = value;
    }
    
    public static void encode(MaxAchievedResolvePacket msg, PacketBuffer buf) {
        buf.writeFloat(msg.value);
    }
    
    public static MaxAchievedResolvePacket decode(PacketBuffer buf) {
        return new MaxAchievedResolvePacket(buf.readFloat());
    }

    public static void handle(MaxAchievedResolvePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.getResolveCounter().setMaxAchievedValue(msg.value);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
