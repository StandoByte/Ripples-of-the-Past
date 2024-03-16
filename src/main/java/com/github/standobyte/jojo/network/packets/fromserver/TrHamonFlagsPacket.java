package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrHamonFlagsPacket {
    private final int entityId;
    private final boolean protectionEnabled;
    
    public TrHamonFlagsPacket(int entityId, HamonData hamonData) {
        this(entityId, hamonData.isProtectionEnabled());
    }
    
    public TrHamonFlagsPacket(int entityId, boolean protectionEnabled) {
        this.entityId = entityId;
        this.protectionEnabled = protectionEnabled;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrHamonFlagsPacket> {

        @Override
        public void encode(TrHamonFlagsPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeBoolean(msg.protectionEnabled);
        }

        @Override
        public TrHamonFlagsPacket decode(PacketBuffer buf) {
            return new TrHamonFlagsPacket(buf.readInt(), buf.readBoolean());
        }

        @Override
        public void handle(TrHamonFlagsPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        hamon.setHamonProtection(msg.protectionEnabled);
                    });
                });
            }
        }

        @Override
        public Class<TrHamonFlagsPacket> getPacketClass() {
            return TrHamonFlagsPacket.class;
        }
    }
    
    public static enum HamonAuraColor {
        ORANGE,
        BLUE,
        YELLOW,
        RED,
        SILVER
    }
}
