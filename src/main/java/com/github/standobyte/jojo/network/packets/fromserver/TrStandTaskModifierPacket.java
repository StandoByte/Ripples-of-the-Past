package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.StandEntityActionModifier;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.entity.stand.StandEntity;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrStandTaskModifierPacket {
    private final int standEntityId;
    private final Action<?> action;

    public TrStandTaskModifierPacket(int standEntityId, Action<?> action) {
        this.standEntityId = standEntityId;
        this.action = action;
    }

    public static void encode(TrStandTaskModifierPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.standEntityId);
        buf.writeRegistryId(msg.action);
    }

    public static TrStandTaskModifierPacket decode(PacketBuffer buf) {
        return new TrStandTaskModifierPacket(buf.readInt(), buf.readRegistryIdSafe(Action.class));
    }

    public static void handle(TrStandTaskModifierPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (msg.action instanceof StandEntityActionModifier) {
                Entity entity = ClientUtil.getEntityById(msg.standEntityId);
                if (entity instanceof StandEntity) {
                    StandEntity standEntity = (StandEntity) entity;
                    standEntity.getCurrentTask().ifPresent(task -> {
                        task.addModifierAction((StandEntityActionModifier) msg.action, standEntity);
                    });
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
