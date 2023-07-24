package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class HamonSyncOnLoadPacket {
    public final int ticksMaskWithNoHamonBreath;
    
//    public HamonSyncOnLoadPacket(HamonData hamon) {
//        this();
//    }
    
    public HamonSyncOnLoadPacket(int ticksMaskWithNoHamonBreath) {
        this.ticksMaskWithNoHamonBreath = ticksMaskWithNoHamonBreath;
    }
    
    
    
    public static class Handler implements IModPacketHandler<HamonSyncOnLoadPacket> {
        
        public void encode(HamonSyncOnLoadPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.ticksMaskWithNoHamonBreath);
        }
        
        public HamonSyncOnLoadPacket decode(PacketBuffer buf) {
            return new HamonSyncOnLoadPacket(buf.readInt());
        }
        
        public void handle(HamonSyncOnLoadPacket msg, Supplier<NetworkEvent.Context> ctx) {
            INonStandPower.getNonStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                    hamon.handleSyncPacket(msg);
                });
            });
        }
        
        @Override
        public Class<HamonSyncOnLoadPacket> getPacketClass() {
            return HamonSyncOnLoadPacket.class;
        }
    }
}
