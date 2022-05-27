package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrCooldownPacket {
    private final int entityId;
    private final PowerClassification classification;
    private final Action<?> action;
    private final int value;
    private final int totalCooldown;

    public TrCooldownPacket(int entityId, PowerClassification classification, Action<?> action, int value) {
        this(entityId, classification, action, value, value);
    }

    public TrCooldownPacket(int entityId, PowerClassification classification, Action<?> action, int value, int totalCooldown) {
        this.entityId = entityId;
        this.classification = classification;
        this.action = action;
        this.value = value;
        this.totalCooldown = totalCooldown;
    }

    public static void encode(TrCooldownPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
        buf.writeEnum(msg.classification);
        buf.writeRegistryIdUnsafe(ModActions.Registry.getRegistry(), msg.action);
        buf.writeVarInt(msg.value);
        buf.writeVarInt(msg.totalCooldown);
    }

    public static TrCooldownPacket decode(PacketBuffer buf) {
        return new TrCooldownPacket(buf.readInt(), buf.readEnum(PowerClassification.class), 
                buf.readRegistryIdUnsafe(ModActions.Registry.getRegistry()), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(TrCooldownPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                IPower.getPowerOptional((LivingEntity) entity, msg.classification).ifPresent(power -> 
                power.updateCooldownTimer(msg.action, msg.value, msg.totalCooldown));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
