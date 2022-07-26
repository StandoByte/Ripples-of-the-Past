package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandInstance;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrTypeStandInstancePacket {
    private final int entityId;
    private final StandInstance standInstance;

    public TrTypeStandInstancePacket(int entityId, StandInstance standInstance) {
        this.entityId = entityId;
        this.standInstance = standInstance;
    }
    
    public static TrTypeStandInstancePacket noStand(int entityId) {
        return new TrTypeStandInstancePacket(entityId, null);
    }

    public static void encode(TrTypeStandInstancePacket msg, PacketBuffer buf) {
        boolean noStand = msg.standInstance == null;
        buf.writeBoolean(noStand);
        buf.writeInt(msg.entityId);
        if (!noStand) {
            msg.standInstance.toBuf(buf);
        }
    }

    public static TrTypeStandInstancePacket decode(PacketBuffer buf) {
        boolean noStand = buf.readBoolean();
        if (noStand) {
            return noStand(buf.readInt());
        }
        return new TrTypeStandInstancePacket(buf.readInt(), StandInstance.fromBuf(buf));
    }

    public static void handle(TrTypeStandInstancePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = ClientUtil.getEntityById(msg.entityId);
            if (entity instanceof LivingEntity) {
                IStandPower.getStandPowerOptional((LivingEntity) entity).ifPresent(stand -> {
                    if (msg.standInstance == null) {
                        if (entity == ClientUtil.getClientPlayer()) {
                            ActionsOverlayGui overlay = ActionsOverlayGui.getInstance();
                            if (PowerClassification.STAND == overlay.getCurrentMode()) {
                                overlay.setMode(null);
                            }
                        }
                        stand.clear();
                    }
                    else {
                        stand.giveStand(msg.standInstance, true);
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
