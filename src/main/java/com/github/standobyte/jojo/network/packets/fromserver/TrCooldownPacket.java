package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.ModCommonRegistries;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrCooldownPacket {
    private final int entityId;
    private final PowerClassification classification;
    private final boolean resetAll;
    private final Action<?> action;
    private final int value;
    private final int totalCooldown;

    public TrCooldownPacket(int entityId, PowerClassification classification, Action<?> action, int value) {
        this(entityId, classification, action, value, value);
    }

    public TrCooldownPacket(int entityId, PowerClassification classification, Action<?> action, int value, int totalCooldown) {
        this(entityId, classification, action, value, totalCooldown, false);
    }
    
    public static TrCooldownPacket resetAll(int entityId, PowerClassification classification) {
        return new TrCooldownPacket(entityId, classification, null, 0, 0, true);
    }
    
    private TrCooldownPacket(int entityId, PowerClassification classification, Action<?> action, int value, int totalCooldown, boolean resetAll) {
        this.entityId = entityId;
        this.classification = classification;
        this.resetAll = resetAll;
        this.action = action;
        this.value = value;
        this.totalCooldown = totalCooldown;
    }

    public static void encode(TrCooldownPacket msg, PacketBuffer buf) {
        buf.writeBoolean(msg.resetAll);
        buf.writeInt(msg.entityId);
        buf.writeEnum(msg.classification);
        if (!msg.resetAll) {
            buf.writeRegistryIdUnsafe(ModCommonRegistries.ACTIONS.getRegistry(), msg.action);
            buf.writeVarInt(msg.value);
            buf.writeVarInt(msg.totalCooldown);
        }
    }

    public static TrCooldownPacket decode(PacketBuffer buf) {
        boolean resetAll = buf.readBoolean();
        if (resetAll) {
            return resetAll(buf.readInt(), buf.readEnum(PowerClassification.class));
        }
        return new TrCooldownPacket(buf.readInt(), buf.readEnum(PowerClassification.class), 
                buf.readRegistryIdUnsafe(ModCommonRegistries.ACTIONS.getRegistry()), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(TrCooldownPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                IPower.getPowerOptional((LivingEntity) entity, msg.classification).ifPresent(power -> {
                    if (msg.resetAll) {
                        power.getCooldowns().resetCooldowns();
                    }
                    else {
                        power.updateCooldownTimer(msg.action, msg.value, msg.totalCooldown);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
