package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.ActionsOverlayGui;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.IPowerType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrSyncPowerTypePacket<P extends IPower<T>, T extends IPowerType<P, T>> {
    private final int entityId;
    private final PowerClassification classification;
    private final T powerType;

    public TrSyncPowerTypePacket(int entityId, PowerClassification classification, T powerType) {
        this.entityId = entityId;
        this.classification = classification;
        this.powerType = powerType;
    }
    
    public static <P extends IPower<T>, T extends IPowerType<P, T>> TrSyncPowerTypePacket<P, T> noPowerType(int entityId, PowerClassification classification) {
        return new TrSyncPowerTypePacket<>(entityId, classification, null);
    }

    public static <P extends IPower<T>, T extends IPowerType<P, T>> void encode(TrSyncPowerTypePacket<P, T> msg, PacketBuffer buf) {
        boolean noPowerType = msg.powerType == null;
        buf.writeBoolean(noPowerType);
        buf.writeInt(msg.entityId);
        buf.writeEnum(msg.classification);
        if (!noPowerType) buf.writeRegistryId(msg.powerType);
    }

    public static <P extends IPower<T>, T extends IPowerType<P, T>> TrSyncPowerTypePacket<P, T> decode(PacketBuffer buf) {
        boolean noPowerType = buf.readBoolean();
        if (noPowerType) {
            return noPowerType(buf.readInt(), buf.readEnum(PowerClassification.class));
        }
        return new TrSyncPowerTypePacket<P, T>(buf.readInt(), buf.readEnum(PowerClassification.class), buf.readRegistryId());
    }

    public static <P extends IPower<T>, T extends IPowerType<P, T>> void handle(TrSyncPowerTypePacket<P, T> msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                IPower.getPowerOptional((LivingEntity) entity, msg.classification).ifPresent(power -> {
                    if (msg.powerType == null) {
                        if (entity == ClientUtil.getClientPlayer()) {
                            ActionsOverlayGui overlay = ActionsOverlayGui.getInstance();
                            if (msg.classification == overlay.getCurrentPower()) {
                                overlay.setMode(null);
                            }
                        }
                        power.clear();
                    }
                    else {
                        IPower.castAndGivePower(power, msg.powerType, msg.classification);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
