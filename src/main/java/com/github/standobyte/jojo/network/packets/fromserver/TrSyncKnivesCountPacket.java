package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrSyncKnivesCountPacket {
    private final int entityId;
    private final int knives;

    public TrSyncKnivesCountPacket(int entityId, int knives) {
        this.entityId = entityId;
        this.knives = knives;
    }

    public static void encode(TrSyncKnivesCountPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
        buf.writeVarInt(msg.knives);
    }

    public static TrSyncKnivesCountPacket decode(PacketBuffer buf) {
        return new TrSyncKnivesCountPacket(buf.readInt(), buf.readVarInt());
    }

    public static void handle(TrSyncKnivesCountPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            entity.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> cap.setKnives(msg.knives));
        });
        ctx.get().setPacketHandled(true);
    }

}
