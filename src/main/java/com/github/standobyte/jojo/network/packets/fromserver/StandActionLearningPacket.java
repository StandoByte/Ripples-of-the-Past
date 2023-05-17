package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.toasts.ActionToast;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class StandActionLearningPacket {
    private final Action<?> action;
    private final float progress;
    private final boolean showToast;
    
    public StandActionLearningPacket(Action<?> action, float progress, boolean showToast) {
        this.action = action;
        this.progress = progress;
        this.showToast = showToast;
    }
    
    
    
    public static class Handler implements IModPacketHandler<StandActionLearningPacket> {

        @Override
        public void encode(StandActionLearningPacket msg, PacketBuffer buf) {
            buf.writeRegistryIdUnsafe(JojoCustomRegistries.ACTIONS.getRegistry(), msg.action);
            buf.writeFloat(msg.progress);
            buf.writeBoolean(msg.showToast);
        }

        @Override
        public StandActionLearningPacket decode(PacketBuffer buf) {
            return new StandActionLearningPacket(buf.readRegistryIdUnsafe(JojoCustomRegistries.ACTIONS.getRegistry()), 
                    buf.readFloat(), buf.readBoolean());
        }

        @Override
        public void handle(StandActionLearningPacket msg, Supplier<NetworkEvent.Context> ctx) {
            if (msg.action instanceof StandAction) {
                Action<IStandPower> standAction = (StandAction) msg.action;
                IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                    boolean showToast = msg.showToast && !standAction.isUnlocked(power);
                    power.setLearningProgressPoints(standAction, msg.progress, false, false);
                    ActionType actionType = standAction.getActionType(power);
                    if (showToast && actionType != null) {
                        ToastGui toastGui = Minecraft.getInstance().getToasts();
                        ActionToast.addOrUpdate(toastGui, 
                                ActionToast.Type.getToastType(
                                        power.getPowerClassification(), actionType, standAction.isShiftVariation()), 
                                standAction, power.getType());
                    }
                });
            }
        }

        @Override
        public Class<StandActionLearningPacket> getPacketClass() {
            return StandActionLearningPacket.class;
        }    
    }
}
