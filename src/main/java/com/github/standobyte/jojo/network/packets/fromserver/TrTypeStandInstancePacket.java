package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class TrTypeStandInstancePacket {
    private final int entityId;
    private final StandInstance standInstance;
    private final int resolveLevel;

    public TrTypeStandInstancePacket(int entityId, StandInstance standInstance, int resolveLevel) {
        this.entityId = entityId;
        this.standInstance = standInstance;
        this.resolveLevel = resolveLevel;
    }
    
    public static TrTypeStandInstancePacket noStand(int entityId) {
        return new TrTypeStandInstancePacket(entityId, null, 0);
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrTypeStandInstancePacket> {

        @Override
        public void encode(TrTypeStandInstancePacket msg, PacketBuffer buf) {
            boolean noStand = msg.standInstance == null;
            buf.writeBoolean(noStand);
            buf.writeInt(msg.entityId);
            if (!noStand) {
                msg.standInstance.toBuf(buf);
                boolean setResolveLevel = msg.resolveLevel >= 0;
                buf.writeBoolean(setResolveLevel);
                if (setResolveLevel) {
                    buf.writeVarInt(msg.resolveLevel);
                }
            }
        }

        @Override
        public TrTypeStandInstancePacket decode(PacketBuffer buf) {
            boolean noStand = buf.readBoolean();
            if (noStand) {
                return noStand(buf.readInt());
            }
            return new TrTypeStandInstancePacket(buf.readInt(), StandInstance.fromBuf(buf), buf.readBoolean() ? buf.readVarInt() : -1);
        }

        @Override
        public void handle(TrTypeStandInstancePacket msg, Supplier<NetworkEvent.Context> ctx) {
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
                        stand.setStandInstance(msg.standInstance);
                        if (msg.resolveLevel >= 0) {
                            stand.setResolveLevel(msg.resolveLevel);
                        }
                    }
                });
            }
        }

        @Override
        public Class<TrTypeStandInstancePacket> getPacketClass() {
            return TrTypeStandInstancePacket.class;
        }
    }

}
