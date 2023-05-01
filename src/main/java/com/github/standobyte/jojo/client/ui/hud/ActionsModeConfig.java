package com.github.standobyte.jojo.client.ui.hud;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

public class ActionsModeConfig<P extends IPower<P, ?>> {
    final PowerClassification powerClassification;
    private P power;
    boolean autoOpened;
    
    private int selectedAttack = 0;
    private int selectedAbility = 0;
    int costOverlayTick = 0;
    
    private final SelectedTargetIcon attackTargetIcon = new SelectedTargetIcon();
    private final SelectedTargetIcon abilityTargetIcon = new SelectedTargetIcon();
    
    ActionsModeConfig(PowerClassification powerClassification) {
        this.powerClassification = powerClassification;
    }
    
    void setPower(P power) {
        this.power = power;
    }
    
    public P getPower() {
        return power;
    }
    
    int getSelectedSlot(ActionType hotbar) {
        switch (hotbar) {
        case ATTACK:
            return selectedAttack;
        case ABILITY:
            return selectedAbility;
        }
        return -1;
    }
    
    void setSelectedSlot(ActionType hotbar, int slot) {
        if (slot > -1) {
            List<Action<P>> actions = power.getActions(hotbar).getEnabled();
            if (slot >= actions.size() || actions.get(slot).getVisibleAction(power) == null) {
                slot = -1;
            }
        }
        else {
            slot = -1;
        }
        
        switch (hotbar) {
        case ATTACK:
            selectedAttack = slot;
            break;
        case ABILITY:
            selectedAbility = slot;
            break;
        }
        resetSelectedTick();
    }
    
    @Nullable
    Action<P> getSelectedAction(ActionType hotbar, boolean shift) {
        int slot = getSelectedSlot(hotbar);
        if (slot == -1) {
            return null;
        }
        Action<P> action = getPower().getAction(hotbar, slot, shift);
        if (action == null) {
            setSelectedSlot(hotbar, -1);
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
    
    SelectedTargetIcon getTargetIcon(ActionType hotbar) {
        switch (hotbar) {
        case ATTACK:
            return attackTargetIcon;
        case ABILITY:
            return abilityTargetIcon;
        }
        return null;
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
