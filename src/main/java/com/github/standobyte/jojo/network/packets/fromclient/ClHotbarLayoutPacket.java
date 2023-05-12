package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.ActionHotbarLayout;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClHotbarLayoutPacket {
    private final PowerClassification classification;
    
    private final boolean reset;
    
    private final Optional<Action<?>> quickAccessAction;

    private final ActionType hotbar;
    private final ActionHotbarLayout<?> layoutIn;
    private final List<ActionHotbarLayout.ActionSwitch<?>> layoutOut;
    
    public static ClHotbarLayoutPacket resetLayout(PowerClassification classification) {
        return new ClHotbarLayoutPacket(classification, true, Optional.empty(), null, null, null);
    }
    
    public static ClHotbarLayoutPacket quickAccessAction(PowerClassification classification, 
            Action<?> action) {
        return new ClHotbarLayoutPacket(classification, false, Optional.of(action), null, null, null);
    }
    
    public static ClHotbarLayoutPacket withLayout(PowerClassification classification, 
            ActionType hotbar, ActionHotbarLayout<?> layout) {
        return new ClHotbarLayoutPacket(classification, false, Optional.empty(), hotbar, layout, null);
    }

    private ClHotbarLayoutPacket(PowerClassification classification, boolean reset, Optional<Action<?>> quickAccessAction, 
            ActionType hotbar, ActionHotbarLayout<?> layoutFromCl, List<ActionHotbarLayout.ActionSwitch<?>> layoutOnSrv) {
        this.classification = classification;
        this.reset = reset;
        this.quickAccessAction = quickAccessAction;
        this.hotbar = hotbar;
        this.layoutIn = layoutFromCl;
        this.layoutOut = layoutOnSrv;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClHotbarLayoutPacket> {
        
        @Override
        public void encode(ClHotbarLayoutPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.classification);
            
            buf.writeBoolean(msg.reset);
            if (msg.reset) return;
            
            NetworkUtil.writeOptional(buf, msg.quickAccessAction, (buffer, action) -> buffer.writeRegistryId(action));
            if (msg.quickAccessAction.isPresent()) return;
            
            buf.writeEnum(msg.hotbar);
            msg.layoutIn.toBuf(buf);
        }
        
        @Override
        public ClHotbarLayoutPacket decode(PacketBuffer buf) {
            PowerClassification power = buf.readEnum(PowerClassification.class);
            
            boolean reset = buf.readBoolean();
            if (reset) {
                return resetLayout(power);
            }
            
            Optional<Action<?>> quickAccess = NetworkUtil.readOptional(buf, buffer -> buffer.readRegistryIdSafe(Action.class));
            if (quickAccess.isPresent()) {
                return quickAccessAction(power, quickAccess.get());
            }
            
            return new ClHotbarLayoutPacket(
                    power, false, Optional.empty(), 
                    buf.readEnum(ActionType.class), null, ActionHotbarLayout.fromBuf(buf));
        }
        
        @Override
        public void handle(ClHotbarLayoutPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            IPower.getPowerOptional(player, msg.classification).ifPresent(power -> {
                if (msg.reset) {
                    power.getActionsLayout().resetLayout();
                }
                else if (msg.quickAccessAction.isPresent()) {
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
        public Class<ClHotbarLayoutPacket> getPacketClass() {
            return ClHotbarLayoutPacket.class;
        }
    }
}
