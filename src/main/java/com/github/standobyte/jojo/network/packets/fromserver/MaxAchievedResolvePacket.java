package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class MaxAchievedResolvePacket {
    private final float value;
    
    public MaxAchievedResolvePacket(float value) {
        this.value = value;
    }
    
    
    
    public static class Handler implements IModPacketHandler<MaxAchievedResolvePacket> {

        @Override
        public void encode(MaxAchievedResolvePacket msg, PacketBuffer buf) {
            buf.writeFloat(msg.value);
        }

        @Override
        public MaxAchievedResolvePacket decode(PacketBuffer buf) {
            return new MaxAchievedResolvePacket(buf.readFloat());
        }

        @Override
        public void handle(MaxAchievedResolvePacket msg, Supplier<NetworkEvent.Context> ctx) {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.getResolveCounter().setMaxAchievedValue(msg.value);
            });
        }

        @Override
        public Class<MaxAchievedResolvePacket> getPacketClass() {
            return MaxAchievedResolvePacket.class;
        }
    }
}
