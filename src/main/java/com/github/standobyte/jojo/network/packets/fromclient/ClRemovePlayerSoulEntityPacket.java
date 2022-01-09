package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.entity.SoulEntity;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClRemovePlayerSoulEntityPacket {
    private final int soulEntityId;
    
    public ClRemovePlayerSoulEntityPacket(int soulEntityId) {
        this.soulEntityId = soulEntityId;
    }
    
    public static void encode(ClRemovePlayerSoulEntityPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.soulEntityId);
    }
    
    public static ClRemovePlayerSoulEntityPacket decode(PacketBuffer buf) {
        return new ClRemovePlayerSoulEntityPacket(buf.readInt());
    }
    
    public static void handle(ClRemovePlayerSoulEntityPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ctx.get().getSender().level.getEntity(msg.soulEntityId);
            if (entity instanceof SoulEntity) {
                ((SoulEntity) entity).skipAscension();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
