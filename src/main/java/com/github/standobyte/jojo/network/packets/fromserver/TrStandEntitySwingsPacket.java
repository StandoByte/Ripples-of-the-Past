package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrStandEntitySwingsPacket {
    private final int standEntityId;
    private final int swings;
    private final int handSideBits;
    
    public TrStandEntitySwingsPacket(int standEntityId, int swings, int handSideBits) {
        this.standEntityId = standEntityId;
        this.swings = swings;
        this.handSideBits = handSideBits;
    }
    
    public static void encode(TrStandEntitySwingsPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.standEntityId);
        buf.writeVarInt(msg.swings);
        buf.writeVarInt(msg.handSideBits);
    }
    
    public static TrStandEntitySwingsPacket decode(PacketBuffer buf) {
        return new TrStandEntitySwingsPacket(buf.readInt(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(TrStandEntitySwingsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtil.getEntityById(msg.standEntityId);
            if (entity instanceof StandEntity) {
                ((StandEntity) entity).getAdditionalSwings().clAddValues(msg.swings, msg.handSideBits);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
