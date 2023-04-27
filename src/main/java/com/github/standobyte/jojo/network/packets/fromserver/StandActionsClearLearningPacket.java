package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class StandActionsClearLearningPacket {
    
    public StandActionsClearLearningPacket() {
    }
    
    
    
    public static class Handler implements IModPacketHandler<StandActionsClearLearningPacket> {
    
        public void encode(StandActionsClearLearningPacket msg, PacketBuffer buf) {
        }
    
        public StandActionsClearLearningPacket decode(PacketBuffer buf) {
            return new StandActionsClearLearningPacket();
        }
    
        public void handle(StandActionsClearLearningPacket msg, Supplier<NetworkEvent.Context> ctx) {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.clearActionLearning();
            });
        }

        @Override
        public Class<StandActionsClearLearningPacket> getPacketClass() {
            return StandActionsClearLearningPacket.class;
        }
    }
}
