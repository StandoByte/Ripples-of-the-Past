package com.github.standobyte.jojo.network.packets.fromclient;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.network.packets.fromserver.ActionsLayoutPacket.Field;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.layout.ActionHotbarLayout;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClActionsLayoutPacket {
    private final PowerClassification classification;
    
    private final boolean reset;
    
    private final Optional<Action<?>> quickAccessAction;
    
    private final Optional<Field> fieldType;
    private final boolean booleanVal;

    private final ActionType hotbar;
    private final ActionHotbarLayout<?> layoutIn;
    private final List<ActionHotbarLayout.ActionSwitch<?>> layoutOut;
    
    public static ClActionsLayoutPacket resetLayout(PowerClassification classification) {
        return new ClActionsLayoutPacket(classification, true, Optional.empty(), 
                Optional.empty(), false, 
                null, null, null);
    }
    
    public static ClActionsLayoutPacket quickAccessAction(PowerClassification classification, 
            Action<?> action) {
        return new ClActionsLayoutPacket(classification, false, Optional.of(action), 
                Optional.empty(), false, 
                null, null, null);
    }
    
    public static ClActionsLayoutPacket withLayout(PowerClassification classification, 
            ActionType hotbar, ActionHotbarLayout<?> layout) {
        return new ClActionsLayoutPacket(classification, false, Optional.empty(), 
                Optional.empty(), false, 
                hotbar, layout, null);
    }
    
    public static ClActionsLayoutPacket saveQuickAccessVisibility(PowerClassification classification, boolean isVisible) {
        return new ClActionsLayoutPacket(classification, false, Optional.empty(), 
                Optional.of(Field.QUICK_ACCESS_HUD_RENDER), isVisible, 
                null, null, null);
    }
    
    private ClActionsLayoutPacket(PowerClassification classification, @Nonnull Field fieldType, boolean fieldValue) {
        this(classification, false, Optional.empty(), 
                Optional.of(fieldType), fieldValue,
                null, null, null);
    }

    private ClActionsLayoutPacket(PowerClassification classification, boolean reset, Optional<Action<?>> quickAccessAction, 
            Optional<Field> fieldType, boolean booleanVal,
            ActionType hotbar, ActionHotbarLayout<?> layoutFromCl, List<ActionHotbarLayout.ActionSwitch<?>> layoutOnSrv) {
        this.classification = classification;
        
        this.reset = reset;
        
        this.quickAccessAction = quickAccessAction;
        
        this.fieldType = fieldType;
        this.booleanVal = booleanVal;
        
        this.hotbar = hotbar;
        this.layoutIn = layoutFromCl;
        this.layoutOut = layoutOnSrv;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClActionsLayoutPacket> {
        
        @Override
        public void encode(ClActionsLayoutPacket msg, PacketBuffer buf) {
            buf.writeEnum(msg.classification);
            
            buf.writeBoolean(msg.reset);
            if (msg.reset) return;
            
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
        public ClActionsLayoutPacket decode(PacketBuffer buf) {
            PowerClassification power = buf.readEnum(PowerClassification.class);
            
            boolean reset = buf.readBoolean();
            if (reset) {
                return resetLayout(power);
            }
            
            Optional<Action<?>> quickAccess = NetworkUtil.readOptional(buf, () -> buf.readRegistryIdSafe(Action.class));
            if (quickAccess.isPresent()) {
                return quickAccessAction(power, quickAccess.get());
            }
            
            Optional<Field> fieldType = NetworkUtil.readOptional(buf, () -> buf.readEnum(Field.class));
            if (fieldType.isPresent()) {
                return new ClActionsLayoutPacket(power, fieldType.get(), buf.readBoolean());
            }
            
            return new ClActionsLayoutPacket(
                    power, false, Optional.empty(), 
                    Optional.empty(), false, 
                    buf.readEnum(ActionType.class), null, ActionHotbarLayout.fromBuf(buf));
        }
        
        @Override
        public void handle(ClActionsLayoutPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            IPower.getPowerOptional(player, msg.classification).ifPresent(power -> {
                if (msg.reset) {
                    power.getActionsLayout().resetLayout();
                }
                else if (msg.quickAccessAction.isPresent()) {
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
        public Class<ClActionsLayoutPacket> getPacketClass() {
            return ClActionsLayoutPacket.class;
        }
    }
}
