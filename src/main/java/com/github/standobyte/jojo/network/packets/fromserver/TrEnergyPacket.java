package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrEnergyPacket {
    private final int entityId;
    private final float energy;
    
    public TrEnergyPacket(int entityId, float energy) {
        this.entityId = entityId;
        this.energy = energy;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrEnergyPacket> {

        @Override
        public void encode(TrEnergyPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeFloat(msg.energy);
        }

        @Override
        public TrEnergyPacket decode(PacketBuffer buf) {
            return new TrEnergyPacket(buf.readInt(), buf.readFloat());
        }

        @Override
        public void handle(TrEnergyPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                    power.setEnergy(msg.energy);
                });
            }
        }

        @Override
        public Class<TrEnergyPacket> getPacketClass() {
            return TrEnergyPacket.class;
        }
    }
}
