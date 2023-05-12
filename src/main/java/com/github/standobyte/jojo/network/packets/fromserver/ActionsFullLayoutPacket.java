package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.ActionHotbarLayout;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ActionsFullLayoutPacket {
    private final PowerClassification classification;
    
    private final Optional<Action<?>> quickAccessAction;
    
    private final ActionType hotbar;
    private final ActionHotbarLayout<?> layoutIn;
    private final List<ActionHotbarLayout.ActionSwitch<?>> layoutOut;

    public static ActionsFullLayoutPacket withLayout(PowerClassification classification, 
            ActionType hotbar, ActionHotbarLayout<?> layout) {
        return new ActionsFullLayoutPacket(classification, Optional.empty(), hotbar, layout, null);
    }

    public static ActionsFullLayoutPacket quickAccessAction(PowerClassification classification, 
            Action<?> action) {
        return new ActionsFullLayoutPacket(classification, Optional.of(action), null, null, null);
    }

    private ActionsFullLayoutPacket(PowerClassification classification, Optional<Action<?>> quickAccessAction,
            ActionType hotbar, ActionHotbarLayout<?> layoutFromSrv, List<ActionHotbarLayout.ActionSwitch<?>> layoutOnCl) {
        this.classification = classification;
        this.quickAccessAction = quickAccessAction;
        this.hotbar = hotbar;
        this.layoutIn = layoutFromSrv;
        this.layoutOut = layoutOnCl;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ActionsFullLayoutPacket> {
        
        @Override
        public void encode(ActionsFullLayoutPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.classification);
            
            NetworkUtil.writeOptional(buf, msg.quickAccessAction, (buffer, action) -> buffer.writeRegistryId(action));
            if (msg.quickAccessAction.isPresent()) return;
            
            buf.writeEnum(msg.hotbar);
            msg.layoutIn.toBuf(buf);
        }
        
        @Override
        public ActionsFullLayoutPacket decode(PacketBuffer buf) {
            PowerClassification power = buf.readEnum(PowerClassification.class);
            
            Optional<Action<?>> quickAccess = NetworkUtil.readOptional(buf, buffer -> buffer.readRegistryIdSafe(Action.class));
            if (quickAccess.isPresent()) {
                return quickAccessAction(power, quickAccess.get());
            }
            
            return new ActionsFullLayoutPacket(
                    power, Optional.empty(), 
                    buf.readEnum(ActionType.class), null, ActionHotbarLayout.fromBuf(buf));
        }
        
        @Override
        public void handle(ActionsFullLayoutPacket msg, Supplier<NetworkEvent.Context> ctx) {

            IPower.getPowerOptional(ClientUtil.getClientPlayer(), msg.classification).ifPresent(power -> {
                if (msg.quickAccessAction.isPresent()) {
                    setQuickAccess(power, msg.quickAccessAction.get());
                }
                else {
                    power.getActions(msg.hotbar).setFromPacket(msg.layoutOut, false);
                }
            });
        }
        
        private <P extends IPower<P, ?>> void setQuickAccess(IPower<?, ?> power, Action<P> action) {
            ((P) power).getActionsLayout().setQuickAccessAction(action);
        }
        
        @Override
        public Class<ActionsFullLayoutPacket> getPacketClass() {
            return ActionsFullLayoutPacket.class;
        }
    }
}
