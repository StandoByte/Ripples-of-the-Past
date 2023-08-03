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

public class TrHamonBreathStabilityPacket {
    private final int entityId;
    private final float breathStability;
    private final int noIncTicks;
    
    public TrHamonBreathStabilityPacket(int entityId, float breathStability, int noIncTicks) {
        this.entityId = entityId;
        this.breathStability = breathStability;
        this.noIncTicks = noIncTicks;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrHamonBreathStabilityPacket> {

        @Override
        public void encode(TrHamonBreathStabilityPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeFloat(msg.breathStability);
            buf.writeVarInt(msg.noIncTicks);
        }

        @Override
        public TrHamonBreathStabilityPacket decode(PacketBuffer buf) {
            return new TrHamonBreathStabilityPacket(buf.readInt(), buf.readFloat(), buf.readVarInt());
        }

        @Override
        public void handle(TrHamonBreathStabilityPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        hamon.setBreathStability(msg.breathStability, msg.noIncTicks);
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
