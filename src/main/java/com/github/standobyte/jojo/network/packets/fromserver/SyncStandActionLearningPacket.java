package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.actions.StandAction;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModActions;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncStandActionLearningPacket {
    private final Action<?> action;
    private final float progress;
    private final boolean showToast;
    
    public SyncStandActionLearningPacket(Action<?> action, float progress, boolean showToast) {
        this.action = action;
        this.progress = progress;
        this.showToast = showToast;
    }
    
    public static void encode(SyncStandActionLearningPacket msg, PacketBuffer buf) {
        buf.writeRegistryIdUnsafe(ModActions.Registry.getRegistry(), msg.action);
        buf.writeFloat(msg.progress);
        buf.writeBoolean(msg.showToast);
    }

    public static SyncStandActionLearningPacket decode(PacketBuffer buf) {
        return new SyncStandActionLearningPacket(buf.readRegistryIdUnsafe(ModActions.Registry.getRegistry()), 
                buf.readFloat(), buf.readBoolean());
    }

    public static void handle(SyncStandActionLearningPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (msg.action instanceof StandAction) {
                Action<IStandPower> standAction = (StandAction) msg.action;
                IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                    boolean showToast = msg.showToast && !standAction.isUnlocked(power) /*always false btw*/;
                    power.setLearningProgressPoints(standAction, msg.progress, false);
                    if (showToast) {
                        // FIXME (!) new stand action toast
//                        ToastGui toastGui = Minecraft.getInstance().getToasts();
//                        ActionToast.addOrUpdate(toastGui, 
//                                type == ActionType.ATTACK ? ActionToast.Type.STAND_ATTACK_VARIATION : ActionToast.Type.STAND_ABILITY_VARIATION, 
//                                        action, power.getType());
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }    
}
