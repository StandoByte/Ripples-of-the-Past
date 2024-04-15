package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrCosmeticItemsPacket {
    private final int entityId;
    private final DyeColor[] colors;
    
    public static TrCosmeticItemsPacket ladybugBrooch(int entityId, DyeColor[] colors) {
        return new TrCosmeticItemsPacket(entityId, colors);
    }
    
    private TrCosmeticItemsPacket(int entityId, DyeColor[] colors) {
        this.entityId = entityId;
        this.colors = colors;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrCosmeticItemsPacket> {

        @Override
        public void encode(TrCosmeticItemsPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            NetworkUtil.writeSmallEnumArray(buf, msg.colors);
        }

        @Override
        public TrCosmeticItemsPacket decode(PacketBuffer buf) {
            int entityId = buf.readInt();
            return ladybugBrooch(entityId, NetworkUtil.readSmallEnumArray(buf, DyeColor.class));
        }

        @Override
        public void handle(TrCosmeticItemsPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.clSetBrooches(msg.colors);
                });
            }
        }
        
        @Override
        public Class<TrCosmeticItemsPacket> getPacketClass() {
            return TrCosmeticItemsPacket.class;
        }
    }
}
