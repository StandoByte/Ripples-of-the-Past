package com.github.standobyte.jojo.client.ui.actionshud;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.InputHandler.ActionKey;
import com.github.standobyte.jojo.client.controls.ActionKeybindEntry;
import com.github.standobyte.jojo.client.controls.ControlScheme;
import com.github.standobyte.jojo.client.controls.HudControlSettings;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.util.Util;

public class ActionsModeConfig<P extends IPower<P, ?>> {
    final PowerClassification powerClassification;
    private P power;
    boolean autoOpened;
    
    ActionKeybindEntry lastHotkeyAction;
    
    private int costOverlayTick = 0;
    
    private final Map<ActionKey, SelectedTargetIcon> targetIcons = Util.make(new EnumMap<>(ActionKey.class), map -> {
        for (ActionKey key : ActionKey.values()) {
            map.put(key, new SelectedTargetIcon());
        }
    });
    
    ActionsModeConfig(PowerClassification powerClassification) {
        this.powerClassification = powerClassification;
    }
    
    void setPower(P power) {
        this.power = power;
    }
    
    public P getPower() {
        return power;
    }
    
    int getSelectedSlot(ControlScheme.Hotbar hotbar) {
        return HudControlSettings.getInstance()
                .getControlScheme(powerClassification)
                .getActionsHotbar(hotbar)
                .getSelectedSlot();
    }
    
    void setSelectedSlot(ControlScheme.Hotbar hotbar, int slot, ActionTarget target) {
        HudControlSettings.getInstance()
        .getControlScheme(powerClassification)
        .getActionsHotbar(hotbar)
        .setSelectedSlot(slot, power, target);
        
        resetSelectedTick();
    }
    
    @Nullable
    Action<P> getSelectedAction(ControlScheme.Hotbar hotbar, boolean shiftVariation, ActionTarget target) {
        return HudControlSettings.getInstance()
                .getControlScheme(powerClassification)
                .getActionsHotbar(hotbar)
                .getSelectedAction(power, shiftVariation, target);
    }
    
    void tick() {
        costOverlayTick++;
    }
    
    void resetSelectedTick() {
        costOverlayTick = 0;
    }

    int getSelectedTick() {
        return costOverlayTick;
    }
    
    SelectedTargetIcon getTargetIcon(ActionKey actionKey) {
        return targetIcons.get(actionKey);
    }
    
    static class SelectedTargetIcon {
        private Action.TargetRequirement targetType;
        private boolean isRightTarget;
        
        void update(Action.TargetRequirement targetType, boolean isRightTarget) {
            this.targetType = targetType;
            this.isRightTarget = isRightTarget;
        }
        
        @Nullable
        int[] getIconTex() {
            if (targetType == null) return null;
            int x;
            switch (targetType) {
            case NONE:
                return null;
            case BLOCK:
                x = 0;
                break;
            case ENTITY:
                x = 32;
                break;
            case ANY:
                x = 64;
                break;
            default:
                return null;
            }
            int y = isRightTarget ? 192 : 224;
            return new int[] {x, y};
        }
    }

}
