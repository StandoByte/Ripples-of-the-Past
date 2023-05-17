package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.layout.ActionHotbarLayout;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ActionsLayoutPacket {
    private final PowerClassification classification;
    
    private final Optional<Action<?>> quickAccessAction;
    
    private final Optional<Field> fieldType;
    private final boolean booleanVal;
    
    private final ActionType hotbar;
    private final ActionHotbarLayout<?> layoutIn;
    private final List<ActionHotbarLayout.ActionSwitch<?>> layoutOut;

    public static ActionsLayoutPacket withLayout(PowerClassification classification, 
            ActionType hotbar, ActionHotbarLayout<?> layout) {
        return new ActionsLayoutPacket(classification, Optional.empty(), 
                Optional.empty(), false,
                hotbar, layout, null);
    }

    public static ActionsLayoutPacket quickAccessAction(PowerClassification classification, 
            Action<?> action) {
        return new ActionsLayoutPacket(classification, Optional.of(action), 
                Optional.empty(), false,
                null, null, null);
    }
    
    public static ActionsLayoutPacket quickAccessRenderInHud(PowerClassification classification, 
            boolean isRendered) {
        return new ActionsLayoutPacket(classification, Field.QUICK_ACCESS_HUD_RENDER, isRendered);
    }
    
    private ActionsLayoutPacket(PowerClassification classification, @Nonnull Field fieldType, boolean fieldValue) {
        this(classification, Optional.empty(), 
                Optional.of(fieldType), fieldValue,
                null, null, null);
    }

    private ActionsLayoutPacket(PowerClassification classification, Optional<Action<?>> quickAccessAction,
            Optional<Field> fieldType, boolean booleanVal,
            ActionType hotbar, ActionHotbarLayout<?> layoutFromSrv, List<ActionHotbarLayout.ActionSwitch<?>> layoutOnCl) {
        this.classification = classification;
        
        this.quickAccessAction = quickAccessAction;
        
        this.fieldType = fieldType;
        this.booleanVal = booleanVal;
        
        this.hotbar = hotbar;
        this.layoutIn = layoutFromSrv;
        this.layoutOut = layoutOnCl;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ActionsLayoutPacket> {
        
        @Override
        public void encode(ActionsLayoutPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.classification);
            
            NetworkUtil.writeOptional(buf, msg.quickAccessAction, action -> buf.writeRegistryId(action));
            if (msg.quickAccessAction.isPresent()) return;
            
            NetworkUtil.writeOptional(buf, msg.fieldType, type -> buf.writeEnum(type));
            if (msg.fieldType.isPresent()) {
                buf.writeBoolean(msg.booleanVal);
                return;
            }
            
            buf.writeEnum(msg.hotbar);
            msg.layoutIn.toBuf(buf);
        }
        
        @Override
        public ActionsLayoutPacket decode(PacketBuffer buf) {
            PowerClassification power = buf.readEnum(PowerClassification.class);
            
            Optional<Action<?>> quickAccess = NetworkUtil.readOptional(buf, () -> buf.readRegistryIdSafe(Action.class));
            if (quickAccess.isPresent()) {
                return quickAccessAction(power, quickAccess.get());
            }
            
            Optional<Field> fieldType = NetworkUtil.readOptional(buf, () -> buf.readEnum(Field.class));
            if (fieldType.isPresent()) {
                return new ActionsLayoutPacket(power, fieldType.get(), buf.readBoolean());
            }
            
            return new ActionsLayoutPacket(
                    power, Optional.empty(), 
                    Optional.empty(), false,
                    buf.readEnum(ActionType.class), null, ActionHotbarLayout.fromBuf(buf));
        }
        
        @Override
        public void handle(ActionsLayoutPacket msg, Supplier<NetworkEvent.Context> ctx) {

            IPower.getPowerOptional(ClientUtil.getClientPlayer(), msg.classification).ifPresent(power -> {
                if (msg.quickAccessAction.isPresent()) {
                    setQuickAccess(power, msg.quickAccessAction.get());
                }
                else if (msg.fieldType.isPresent()) {
//                    switch (msg.fieldType.get()) {
//                    case QUICK_ACCESS_HUD_RENDER:
                        power.getActionsLayout().setMmbActionHudVisibility(msg.booleanVal);
//                        break;
//                    }
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
        public Class<ActionsLayoutPacket> getPacketClass() {
            return ActionsLayoutPacket.class;
        }
    }
    
    public static enum Field {
        QUICK_ACCESS_HUD_RENDER
    }
}
