package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.Collection;
import java.util.function.Supplier;

import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.network.packets.fromserver.MobAggroCategoryPacket;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClRequestMobAggroPacket {
    private final Collection<EntityType<?>> entityTypes;
    
    public ClRequestMobAggroPacket(Collection<EntityType<?>> entityTypes) {
        this.entityTypes = entityTypes;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClRequestMobAggroPacket> {
    
        @Override
        public void encode(ClRequestMobAggroPacket msg, PacketBuffer buf) {
            NetworkUtil.writeCollection(buf, msg.entityTypes, type -> buf.writeRegistryId(type), false);
        }

        @Override
        public ClRequestMobAggroPacket decode(PacketBuffer buf) {
            Collection<EntityType<?>> entityTypes = NetworkUtil.readCollection(buf, () -> buf.readRegistryIdSafe(EntityType.class));
            return new ClRequestMobAggroPacket(entityTypes);
        }

        @Override
        public void handle(ClRequestMobAggroPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            PacketManager.sendToClient(MobAggroCategoryPacket.createFrom(msg.entityTypes), player);
        }

        @Override
        public Class<ClRequestMobAggroPacket> getPacketClass() {
            return ClRequestMobAggroPacket.class;
        }
    }

}
