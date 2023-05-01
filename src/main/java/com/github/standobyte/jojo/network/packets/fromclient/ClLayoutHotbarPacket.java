package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.function.Supplier;

import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.ActionHotbarData;
import com.github.standobyte.jojo.power.ActionHotbarData.ActionHotbarLayout;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClLayoutHotbarPacket {
    private final PowerClassification classification;
    private final ActionType hotbar;
    private final boolean reset;
    private final ActionHotbarLayout<?> layout;
    
    public static ClLayoutHotbarPacket resetLayout(PowerClassification classification) {
        return new ClLayoutHotbarPacket(classification, null, true, null);
    }
    
    public static ClLayoutHotbarPacket withLayout(PowerClassification classification, 
            ActionType hotbar, ActionHotbarLayout<?> layout) {
        return new ClLayoutHotbarPacket(classification, hotbar, false, layout);
    }

    private ClLayoutHotbarPacket(PowerClassification classification, 
            ActionType hotbar, boolean reset, ActionHotbarLayout<?> layout) {
        this.classification = classification;
        this.hotbar = hotbar;
        this.reset = reset;
        this.layout = layout;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClLayoutHotbarPacket> {
    
        @Override
        public void encode(ClLayoutHotbarPacket msg, PacketBuffer buf) {
            buf.writeBoolean(msg.reset);
            buf.writeEnum(msg.classification);
            if (!msg.reset) {
                buf.writeEnum(msg.hotbar);
                msg.layout.toBuf(buf);
            }
        }

        @Override
        public ClLayoutHotbarPacket decode(PacketBuffer buf) {
            boolean reset = buf.readBoolean();
            if (reset) {
                return resetLayout(buf.readEnum(PowerClassification.class));
            }
            else {
                return withLayout(buf.readEnum(PowerClassification.class), buf.readEnum(ActionType.class), 
                        ActionHotbarLayout.fromBuf(buf));
            }
        }

        @Override
        public void handle(ClLayoutHotbarPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            IPower.getPowerOptional(player, msg.classification).ifPresent(power -> {
                if (msg.reset) {
                    power.getActionsLayout().resetLayout();
                }
                else {
                    // FIXME (layout editing) more guarding
                    setLayout(power.getActions(msg.hotbar), msg.layout);
                }
            });
        }
        
        private <P extends IPower<P, ?>> void setLayout(ActionHotbarData<P> actions, ActionHotbarLayout<?> layout) {
            actions.setLayout((ActionHotbarLayout<P>) layout);
        }

        @Override
        public Class<ClLayoutHotbarPacket> getPacketClass() {
            return ClLayoutHotbarPacket.class;
        }
    }
}
