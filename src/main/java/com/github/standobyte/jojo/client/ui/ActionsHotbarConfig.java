package com.github.standobyte.jojo.client.ui;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.util.math.MathHelper;

public class ActionsHotbarConfig<P extends IPower<?>> {
    final PowerClassification powerClassification;
    private P power;
    private List<Action> actions;
    private Action rmbAction;
    private int selectedSlot;
    boolean chosenManually;
    
    ActionsHotbarConfig(PowerClassification powerClassification) {
        this.powerClassification = powerClassification;
    }
    
    void setPower(P power) {
        this.power = power;
        this.actions = Stream
                .concat(power.getAttacks().stream(), power.getAbilities().stream())
                .collect(Collectors.toList());
    }
    
    P getPower() {
        return power;
    }
    
    int getAllActionsCount() {
        return actions.size();
    }
    
    // FIXME add new ones to the right
    // FIXME plants infusion doesn't upgrade
    List<Action> getUnlockedActions() {
        return actions.stream()
                .filter(action -> power.isActionUnlocked(action))
                .collect(Collectors.toList());
    }
    
    @Nullable
    Action getRmbAction() {
        return rmbAction;
    }

    @Nullable // FIXME handle the fact it's nullable
    Action getSelectedAction() {
        List<Action> unlocked = getUnlockedActions();
        if (unlocked.size() > selectedSlot) {
            return getUnlockedActions().get(selectedSlot);
        }
        return null;
    }
    
    void scrollSelectedSlot(boolean backwards) {
        int size = getUnlockedActions().size();
        selectedSlot = backwards ? 
                (--selectedSlot + size) % size
                : ++selectedSlot % size;
    }
    
    void selectSlot(int slot) {
        if (slot == MathHelper.clamp(slot, 0, getUnlockedActions().size() - 1)) {
            this.selectedSlot = slot;
        }
    }
    
    int getSelectedSlot() {
        return selectedSlot;
    }
    
    void swapRmbAndCurrentActions() {
        Action selected = getSelectedAction();
        if (rmbAction != null) {
            int i = actions.indexOf(getSelectedAction());
            if (i > -1) {
                actions.add(i, rmbAction);
            }
        }
        rmbAction = selected;
        actions.remove(selected);
    }
    
    boolean moveRmbActionToList() {
        if (rmbAction != null) {
            int i = actions.indexOf(getSelectedAction());
            if (i > -1) {
                actions.add(i, rmbAction);
                rmbAction = null;
                return true;
            }
        }
        return false;
    }
}
