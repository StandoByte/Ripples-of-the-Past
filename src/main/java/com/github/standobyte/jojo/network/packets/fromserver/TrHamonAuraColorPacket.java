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

public class TrHamonAuraColorPacket {
    private final int entityId;
    private final HamonAuraColor color;
    
    public TrHamonAuraColorPacket(int entityId, HamonAuraColor color) {
        this.entityId = entityId;
        this.color = color;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrHamonAuraColorPacket> {

        @Override
        public void encode(TrHamonAuraColorPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeEnum(msg.color);
        }

        @Override
        public TrHamonAuraColorPacket decode(PacketBuffer buf) {
            return new TrHamonAuraColorPacket(buf.readInt(), buf.readEnum(HamonAuraColor.class));
        }

        @Override
        public void handle(TrHamonAuraColorPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                INonStandPower.getNonStandPowerOptional((LivingEntity) entity).ifPresent(power -> {
                    power.getTypeSpecificData(ModPowers.HAMON.get()).ifPresent(hamon -> {
                        hamon.setAuraColor(msg.color);
                    });
                });
            }
        }

        @Override
        public Class<TrHamonAuraColorPacket> getPacketClass() {
            return TrHamonAuraColorPacket.class;
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
