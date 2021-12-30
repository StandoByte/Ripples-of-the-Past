package com.github.standobyte.jojo.client.ui;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;

import net.minecraft.client.GameSettings;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ActionsHotbarConfig<P extends IPower<P, ?>> {
    final PowerClassification powerClassification;
    private P power;
    private List<Action<P>> actions;
    private Action<P> rmbAction;
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
    
    List<Action<P>> getUnlockedActions() {
        return actions.stream()
                .filter(action -> power.isActionUnlocked(action))
                .collect(Collectors.toList());
    }
    
    @Nullable
    Action<P> getRmbAction() {
        return rmbAction;
    }

    @Nullable // FIXME handle the fact it's nullable
    Action<P> getSelectedAction() {
        List<Action<P>> unlocked = getUnlockedActions();
        if (unlocked.size() > selectedSlot) {
            return getUnlockedActions().get(selectedSlot);
        }
        return null;
    }
    
    @Nullable
    ITextComponent getSelectedActionName(GameSettings options) {
        Action<P> action = getSelectedAction();
        if (action == null) {
            return null;
        }
        ITextComponent actionName = action.getName(getPower());
        if (action.getHoldDurationMax() > 0) {
            actionName = new TranslationTextComponent("jojo.overlayv2.hold", actionName);
        }
        if (action.hasShiftVariation()) {
            actionName = new TranslationTextComponent("jojo.overlayv2.shift", actionName, 
                    new KeybindTextComponent(options.keyShift.getName()), action.getShiftVariationIfPresent().getName(getPower()));
        }
        return actionName;
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
        Action<P> selected = getSelectedAction();
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
