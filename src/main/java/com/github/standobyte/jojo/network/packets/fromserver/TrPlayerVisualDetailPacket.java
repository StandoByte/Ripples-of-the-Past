package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrPlayerVisualDetailPacket {
    private final int entityId;
    private final int inkPastaTicks;
    
    public TrPlayerVisualDetailPacket(int entityId, int inkPastaTicks) {
        this.entityId = entityId;
        this.inkPastaTicks = inkPastaTicks;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrPlayerVisualDetailPacket> {

        @Override
        public void encode(TrPlayerVisualDetailPacket msg, PacketBuffer buf) {
            buf.writeInt(msg.entityId);
            buf.writeInt(msg.inkPastaTicks);
        }

        @Override
        public TrPlayerVisualDetailPacket decode(PacketBuffer buf) {
            return new TrPlayerVisualDetailPacket(buf.readInt(), buf.readInt());
        }

        @Override
        public void handle(TrPlayerVisualDetailPacket msg, Supplier<NetworkEvent.Context> ctx) {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof PlayerEntity) {
                entity.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.setInkPastaVisuals(msg.inkPastaTicks));
            }
        }

        @Override
        public Class<TrPlayerVisualDetailPacket> getPacketClass() {
            return TrPlayerVisualDetailPacket.class;
        }
    }
}
