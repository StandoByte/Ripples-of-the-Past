package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrHamonEnergyTicksPacket {
    private final int entityId;
    private final int ticks;
    
    public TrHamonEnergyTicksPacket(int entityId, int ticks) {
        this.entityId = entityId;
        this.ticks = ticks;
    }
    
    public int getTicks() {
        return ticks;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrHamonEnergyTicksPacket> {
    
        public void encode(TrHamonEnergyTicksPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeShort(msg.ticks);
        }
        
        public TrHamonEnergyTicksPacket decode(PacketBuffer buf) {
            return new TrHamonEnergyTicksPacket(buf.readInt(), buf.readShort());
        }
    
        public void handle(TrHamonEnergyTicksPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        hamon.setNoEnergyDecayTicks(msg);
                    });
                });
            }
        }

        @Override
        public Class<TrHamonEnergyTicksPacket> getPacketClass() {
            return TrHamonEnergyTicksPacket.class;
        }
    }
}
