package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncEnergyPacket {
    private final float energy;
    
    public SyncEnergyPacket(float energy) {
        this.energy = energy;
    }
    
    public static void encode(SyncEnergyPacket msg, PacketBuffer buf) {
        buf.writeFloat(msg.energy);
    }
    
    public static SyncEnergyPacket decode(PacketBuffer buf) {
        return new SyncEnergyPacket(buf.readFloat());
    }

    public static void handle(SyncEnergyPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            INonStandPower.getNonStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                power.setEnergy(msg.energy);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
