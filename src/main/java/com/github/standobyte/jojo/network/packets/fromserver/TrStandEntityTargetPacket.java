package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrStandEntityTargetPacket {
    private final int standEntityId;
    private final ActionTarget target;

    public TrStandEntityTargetPacket(int standEntityId, ActionTarget target) {
        this.standEntityId = standEntityId;
        this.target = target;
    }

    public static void encode(TrStandEntityTargetPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.standEntityId);
        msg.target.writeToBuf(buf);
    }

    public static TrStandEntityTargetPacket decode(PacketBuffer buf) {
        return new TrStandEntityTargetPacket(buf.readInt(), ActionTarget.readFromBuf(buf));
    }

    public static void handle(TrStandEntityTargetPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtil.getEntityById(msg.standEntityId);
            if (entity instanceof StandEntity) {
                StandEntity standEntity = (StandEntity) entity;
                standEntity.setTaskTarget(msg.target);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
