package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.controls.ControlScheme;
import com.github.standobyte.jojo.client.ui.toasts.ActionToast;
import com.github.standobyte.jojo.client.ui.toasts.ActionToast.IActionToastType;
import com.github.standobyte.jojo.client.ui.toasts.FinisherAttackToast;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandActionLearningProgress.StandActionLearningEntry;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class StandActionLearningPacket {
    public final StandActionLearningEntry entry;
    private final boolean showToast;
    
    public StandActionLearningPacket(StandActionLearningEntry entry, boolean showToast) {
        this.entry = entry;
        this.showToast = showToast;
    }
    
    
    
    public static class Handler implements IModPacketHandler<StandActionLearningPacket> {

        @Override
        public void encode(StandActionLearningPacket msg, PacketBuffer buf) {
            msg.entry.toBuf(buf);
            buf.writeBoolean(msg.showToast);
        }

        @Override
        public StandActionLearningPacket decode(PacketBuffer buf) {
            return new StandActionLearningPacket(StandActionLearningEntry.fromBuf(buf), buf.readBoolean());
        }

        @Override
        public void handle(StandActionLearningPacket msg, Supplier<NetworkEvent.Context> ctx) {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                StandAction action = msg.entry.action;
                boolean showToast = msg.showToast && !action.isUnlocked(power) && !action.isUnlockedByDefault();
                power.setLearningFromPacket(msg);
                
                if (showToast) {
                    StandType<?> standType = power.getType();
                    
                    IActionToastType toastType = null;
                    if (standType.getStandFinisherPunch().map(finisher -> finisher == action).orElse(false)) {
                        toastType = FinisherAttackToast.SpecialToastType.FINISHER_HEAVY_ATTACK;
                    }
                    else {
                        ControlScheme.Hotbar hotbar = getActionHotbar(action, standType);
                        boolean isShiftVariation = action.isShiftVariation();
                        toastType = ActionToast.Type.getToastType(PowerClassification.STAND, hotbar, isShiftVariation);
                    }
                    
                    if (toastType != null) {
                        ToastGui toastGui = Minecraft.getInstance().getToasts();
                        ActionToast.addOrUpdate(toastGui, toastType, action, power);
                    }
                }
            });
        }
        
        @Nullable
        private ControlScheme.Hotbar getActionHotbar(StandAction action, StandType<?> standType) {
            if (action.getBaseVariation() instanceof StandAction) {
                action = (StandAction) action.getBaseVariation();
            }
            
            for (ControlScheme.Hotbar hotbar : ControlScheme.Hotbar.values()) {
                for (StandAction a : standType.getDefaultHotbar(hotbar)) {
                    if (action == a || a.hasShiftVariation() && action == a.getShiftVariationIfPresent()) {
                        return hotbar;
                    }
                }
            }
            
            return null;
        }

        @Override
        public Class<StandActionLearningPacket> getPacketClass() {
            return StandActionLearningPacket.class;
        }    
    }
}
