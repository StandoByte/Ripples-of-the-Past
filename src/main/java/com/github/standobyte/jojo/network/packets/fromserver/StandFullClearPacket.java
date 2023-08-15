package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class StandFullClearPacket {
    
    public StandFullClearPacket() {
    }
    
    
    
    public static class Handler implements IModPacketHandler<StandFullClearPacket> {
    
        public void encode(StandFullClearPacket msg, PacketBuffer buf) {
        }
    
        public StandFullClearPacket decode(PacketBuffer buf) {
            return new StandFullClearPacket();
        }
    
        public void handle(StandFullClearPacket msg, Supplier<NetworkEvent.Context> ctx) {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.fullStandClear();
            });
        }

        @Override
        public Class<StandFullClearPacket> getPacketClass() {
            return StandFullClearPacket.class;
        }
    }
}
