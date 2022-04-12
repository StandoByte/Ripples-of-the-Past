package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandRelativeOffset;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrSetStandOffsetPacket {
    private final int standEntityId;
    private final StandRelativeOffset offset;

    public TrSetStandOffsetPacket(int standEntityId, StandRelativeOffset offset) {
        this.standEntityId = standEntityId;
        this.offset = offset;
    }

    public static void encode(TrSetStandOffsetPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.standEntityId);
        msg.offset.writeToBuf(buf);
    }

    public static TrSetStandOffsetPacket decode(PacketBuffer buf) {
        return new TrSetStandOffsetPacket(buf.readInt(), StandRelativeOffset.readFromBuf(buf));
    }

    public static void handle(TrSetStandOffsetPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtil.getEntityById(msg.standEntityId);
            if (entity instanceof StandEntity) {
                StandEntity standEntity = (StandEntity) entity;
                standEntity.setTaskPosOffset(msg.offset, false);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
