package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.List;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.ActionHotbarLayout;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ActionsFullLayoutPacket {
    private final PowerClassification classification;
    private final ActionType hotbar;
    private final ActionHotbarLayout<?> layoutIn;
    private final List<ActionHotbarLayout.ActionSwitch<?>> layoutOut;

    public ActionsFullLayoutPacket(PowerClassification classification, ActionType hotbar, 
            ActionHotbarLayout<?> layout) {
        this(classification, hotbar, layout, null);
    }

    private ActionsFullLayoutPacket(PowerClassification classification, ActionType hotbar, 
            ActionHotbarLayout<?> layoutFromSrv, List<ActionHotbarLayout.ActionSwitch<?>> layoutOnCl) {
        this.classification = classification;
        this.hotbar = hotbar;
        this.layoutIn = layoutFromSrv;
        this.layoutOut = layoutOnCl;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ActionsFullLayoutPacket> {
        
        public void encode(ActionsFullLayoutPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.classification);
            buf.writeEnum(msg.hotbar);
            msg.layoutIn.toBuf(buf);
        }
        
        public ActionsFullLayoutPacket decode(PacketBuffer buf) {
            return new ActionsFullLayoutPacket(buf.readEnum(PowerClassification.class), 
                    buf.readEnum(ActionType.class), null, ActionHotbarLayout.fromBuf(buf));
        }
        
        public void handle(ActionsFullLayoutPacket msg, Supplier<NetworkEvent.Context> ctx) {
            IPower.getPowerOptional(ClientUtil.getClientPlayer(), msg.classification).ifPresent(power -> {
                power.getActions(msg.hotbar).setFromPacket(msg.layoutOut, true);
            });
        }
        
        @Override
        public Class<ActionsFullLayoutPacket> getPacketClass() {
            return ActionsFullLayoutPacket.class;
        }
    }
}
