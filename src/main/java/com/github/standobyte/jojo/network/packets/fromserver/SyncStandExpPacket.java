package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.toasts.ActionToast;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncStandExpPacket {
    private final int exp;
    private final boolean showToasts;
    
    public SyncStandExpPacket(int exp, boolean showToasts) {
        this.exp = exp;
        this.showToasts = showToasts;
    }
    
    public static void encode(SyncStandExpPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.exp);
        buf.writeBoolean(msg.showToasts);
    }
    
    public static SyncStandExpPacket decode(PacketBuffer buf) {
        return new SyncStandExpPacket(buf.readInt(), buf.readBoolean());
    }

    public static void handle(SyncStandExpPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            IStandPower.getStandPowerOptional(ClientUtil.getClientPlayer()).ifPresent(power -> {
                if (power.hasPower()) {
                    List<Action> oldAttacks = getAllUnlockedActions(power, ActionType.ATTACK);
                    List<Action> oldAbilities = getAllUnlockedActions(power, ActionType.ABILITY);
                    power.setExp(msg.exp);
                    if (msg.showToasts) {
                        ToastGui toastGui = Minecraft.getInstance().getToasts();
                        updateToasts(ActionType.ATTACK, power, oldAttacks, toastGui);
                        updateToasts(ActionType.ABILITY, power, oldAbilities, toastGui);
                    }
                    ActionsOverlayGui.getInstance().updateActionName(ActionType.ATTACK);
                    ActionsOverlayGui.getInstance().updateActionName(ActionType.ABILITY);
                }
                else {
                    power.setExp(msg.exp);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
    
    private static List<Action> getAllUnlockedActions(IPower<?> power, ActionType type) {
        return Stream.concat(
                power.getActions(type).stream(), 
                power.getActions(type).stream().map(Action::getShiftVariationIfPresent))
                .distinct()
                .filter(a -> power.isActionUnlocked(a))
                .collect(Collectors.toList());
    }
    
    private static void updateToasts(ActionType type, IPower<?> power, List<Action> oldActions, ToastGui toastGui) {
        List<Action> actions = power.getActions(type);
        for (Action action : actions) {
            if (power.isActionUnlocked(action) && !oldActions.contains(action)) {
                ActionToast.addOrUpdate(toastGui, type == ActionType.ATTACK ? ActionToast.Type.STAND_ATTACK : ActionToast.Type.STAND_ABILITY, action, power.getType());
            }
            if (action.hasShiftVariation() && oldActions.contains(action)) {
                Action shiftVar = action.getShiftVariationIfPresent();
                if (power.isActionUnlocked(shiftVar) && !oldActions.contains(shiftVar)) {
                    ActionToast.addOrUpdate(toastGui, type == ActionType.ATTACK ? ActionToast.Type.STAND_ATTACK_VARIATION : ActionToast.Type.STAND_ABILITY_VARIATION, action, power.getType());
                    ActionToast.addOrUpdate(toastGui, type == ActionType.ATTACK ? ActionToast.Type.STAND_ATTACK_VARIATION : ActionToast.Type.STAND_ABILITY_VARIATION, shiftVar, power.getType());
                }
            }
        }
    }

}
