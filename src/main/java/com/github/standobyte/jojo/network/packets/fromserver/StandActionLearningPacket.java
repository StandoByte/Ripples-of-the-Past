package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.toasts.ActionToast;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandActionLearningProgress.StandActionLearningEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class StandActionLearningPacket {
    public final StandAction action;
    public final StandActionLearningEntry entry;
    private final boolean showToast;
    
    public StandActionLearningPacket(StandAction action, StandActionLearningEntry entry, boolean showToast) {
        this.action = action;
        this.entry = entry;
        this.showToast = showToast;
    }
    
    
    
    public static class Handler implements IModPacketHandler<StandActionLearningPacket> {

        @Override
        public void encode(StandActionLearningPacket msg, PacketBuffer buf) {
            buf.writeRegistryId(msg.action);
            msg.entry.toBuf(buf);
            buf.writeBoolean(msg.showToast);
        }

        @Override
        public StandActionLearningPacket decode(PacketBuffer buf) {
            StandAction action = (StandAction) buf.readRegistryIdSafe(Action.class);
            return new StandActionLearningPacket(action, 
                    StandActionLearningEntry.fromBuf(buf, action), buf.readBoolean());
        }

        @Override
        public void handle(StandActionLearningPacket msg, Supplier<NetworkEvent.Context> ctx) {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                boolean showToast = msg.showToast && !msg.action.isUnlocked(power);
                power.setLearningFromPacket(msg);
                ActionType actionType = msg.action.getActionType(power);
                if (showToast && actionType != null) {
                    ToastGui toastGui = Minecraft.getInstance().getToasts();
                    ActionToast.addOrUpdate(toastGui, 
                            ActionToast.Type.getToastType(
                                    power.getPowerClassification(), actionType, msg.action.isShiftVariation()), 
                            msg.action, power.getType());
                }
            });
        }

        @Override
        public Class<StandActionLearningPacket> getPacketClass() {
            return StandActionLearningPacket.class;
        }    
    }
}
