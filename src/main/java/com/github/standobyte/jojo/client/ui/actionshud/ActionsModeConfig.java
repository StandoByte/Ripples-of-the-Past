package com.github.standobyte.jojo.client.ui.actionshud;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.InputHandler.ActionKey;
import com.github.standobyte.jojo.client.controls.ControlScheme;
import com.github.standobyte.jojo.client.controls.HudControlSettings;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.util.Util;

public class ActionsModeConfig<P extends IPower<P, ?>> {
    final PowerClassification powerClassification;
    private P power;
    boolean autoOpened;
    
    private int selectedAttack = 0;
    private int selectedAbility = 0;
    Action<?> lastCustomKeybindAction;
    
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
        switch (hotbar) {
        case LEFT_CLICK:
            return selectedAttack;
        case RIGHT_CLICK:
            return selectedAbility;
        }
        return -1;
    }
    
    void setSelectedSlot(ControlScheme.Hotbar hotbar, int slot, ActionTarget target) {
        if (slot > -1) {
            List<Action<?>> actions = HudControlSettings.getInstance()
                    .getControlScheme(powerClassification)
                    .getActionsHotbar(hotbar)
                    .getEnabledActions();
            if (slot >= actions.size() || ((Action<P>) actions.get(slot)).getVisibleAction(power, target) == null) {
                slot = -1;
            }
        }
        else {
            slot = -1;
        }
        
        switch (hotbar) {
        case LEFT_CLICK:
            selectedAttack = slot;
            break;
        case RIGHT_CLICK:
            selectedAbility = slot;
            break;
        }
        resetSelectedTick();
    }
    
    @Nullable
    Action<P> getSelectedAction(ControlScheme.Hotbar hotbar, boolean shiftVariation, ActionTarget target) {
        int slot = getSelectedSlot(hotbar);
        if (slot == -1) {
            return null;
        }
        P power = getPower();
        Action<P> action = (Action<P>) HudControlSettings.getInstance()
                .getControlScheme(powerClassification)
                .getActionsHotbar(hotbar)
                .getBaseActionInSlot(slot);
        action = ActionsOverlayGui.resolveVisibleActionInSlot(
                action, shiftVariation, power, ActionsOverlayGui.getInstance().getMouseTarget());
        if (action == null) {
            setSelectedSlot(hotbar, -1, target);
        }
        return action;
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
