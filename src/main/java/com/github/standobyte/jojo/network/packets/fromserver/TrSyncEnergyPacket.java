package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrSyncEnergyPacket {
    private final int entityId;
    private final float energy;
    
    public TrSyncEnergyPacket(int entityId, float energy) {
        this.entityId = entityId;
        this.energy = energy;
    }
    
    public static void encode(TrSyncEnergyPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
        buf.writeFloat(msg.energy);
    }
    
    public static TrSyncEnergyPacket decode(PacketBuffer buf) {
        return new TrSyncEnergyPacket(buf.readInt(), buf.readFloat());
    }

    public static void handle(TrSyncEnergyPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                    power.setEnergy(msg.energy);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
