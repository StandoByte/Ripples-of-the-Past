package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrHamonLiquidWalkingPacket {
    private final int userId;
    private final boolean liquidWalking;
    
    public TrHamonLiquidWalkingPacket(int userId, boolean value) {
        this.userId = userId;
        this.liquidWalking = value;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrHamonLiquidWalkingPacket> {

        @Override
        public void encode(TrHamonLiquidWalkingPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.userId);
            buf.writeBoolean(msg.liquidWalking);
        }

        @Override
        public TrHamonLiquidWalkingPacket decode(PacketBuffer buf) {
            return new TrHamonLiquidWalkingPacket(buf.readInt(), buf.readBoolean());
        }

        @Override
        public void handle(TrHamonLiquidWalkingPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.userId);
            if (entity instanceof PlayerEntity) {
                entity.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.setWaterWalking(msg.liquidWalking);
                });
            }
        }

        @Override
        public Class<TrHamonLiquidWalkingPacket> getPacketClass() {
            return TrHamonLiquidWalkingPacket.class;
        }
    }
}
