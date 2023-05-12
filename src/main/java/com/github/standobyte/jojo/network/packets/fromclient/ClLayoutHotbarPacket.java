package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.List;
import java.util.function.Supplier;

import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.ActionHotbarLayout;
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
    private final ActionHotbarLayout<?> layoutIn;
    private final List<ActionHotbarLayout.ActionSwitch<?>> layoutOut;
    
    public static ClLayoutHotbarPacket resetLayout(PowerClassification classification) {
        return new ClLayoutHotbarPacket(classification, null, true, null, null);
    }
    
    public static ClLayoutHotbarPacket withLayout(PowerClassification classification, 
            ActionType hotbar, ActionHotbarLayout<?> layout) {
        return new ClLayoutHotbarPacket(classification, hotbar, false, layout, null);
    }

    private ClLayoutHotbarPacket(PowerClassification classification, ActionType hotbar, boolean reset, 
            ActionHotbarLayout<?> layoutFromCl, List<ActionHotbarLayout.ActionSwitch<?>> layoutOnSrv) {
        this.classification = classification;
        this.hotbar = hotbar;
        this.reset = reset;
        this.layoutIn = layoutFromCl;
        this.layoutOut = layoutOnSrv;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClLayoutHotbarPacket> {
        
        @Override
        public void encode(ClLayoutHotbarPacket msg, PacketBuffer buf) {
            buf.writeBoolean(msg.reset);
            buf.writeEnum(msg.classification);
            if (!msg.reset) {
                buf.writeEnum(msg.hotbar);
                msg.layoutIn.toBuf(buf);
            }
        }
        
        @Override
        public ClLayoutHotbarPacket decode(PacketBuffer buf) {
            boolean reset = buf.readBoolean();
            if (reset) {
                return resetLayout(buf.readEnum(PowerClassification.class));
            }
            else {
                return new ClLayoutHotbarPacket(
                        buf.readEnum(PowerClassification.class), buf.readEnum(ActionType.class), false, 
                        null, ActionHotbarLayout.fromBuf(buf));
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
                    power.getActions(msg.hotbar).setFromPacket(msg.layoutOut, false);
                }
            });
        }
        
        @Override
        public Class<ClLayoutHotbarPacket> getPacketClass() {
            return ClLayoutHotbarPacket.class;
        }
    }
}
