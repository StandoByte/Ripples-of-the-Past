package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrHamonBreathStabilityPacket {
    private final int entityId;
    private final float breathStability;
    
    public TrHamonBreathStabilityPacket(int entityId, float breathStability) {
        this.entityId = entityId;
        this.breathStability = breathStability;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrHamonBreathStabilityPacket> {

        @Override
        public void encode(TrHamonBreathStabilityPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeFloat(msg.breathStability);
        }

        @Override
        public TrHamonBreathStabilityPacket decode(PacketBuffer buf) {
            return new TrHamonBreathStabilityPacket(buf.readInt(), buf.readFloat());
        }

        @Override
        public void handle(TrHamonBreathStabilityPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        hamon.setBreathStability(msg.breathStability);
                    });
                });
            }
        }

        @Override
        public Class<TrHamonBreathStabilityPacket> getPacketClass() {
            return TrHamonBreathStabilityPacket.class;
        }
    }
}
