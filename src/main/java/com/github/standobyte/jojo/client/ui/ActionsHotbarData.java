package com.github.standobyte.jojo.client.ui;

import com.github.standobyte.jojo.client.ui.ActionsOverlayGui.UiMode;

import net.minecraft.util.text.ITextComponent;

public class ActionsHotbarData {
    private int selectedIndex = 0;
    ITextComponent name;
    ITextComponent nameShift;
    boolean isRightTarget;
    boolean isAvaliable;
    int standSelection;
    int nonStandSelection;
    
    boolean noActionSelected() {
        return selectedIndex == -1;
    }
    
    void setSelectedIndex(UiMode mode, int index) {
        this.selectedIndex = index;
        switch (mode) {
        case STAND:
            this.standSelection = index;
            break;
        case NON_STAND:
            this.nonStandSelection = index;
            break;
        default:
            break;
        }
    }
    
    int getSelectedIndex() {
        return selectedIndex;
    }
    
    int getSavedSelectedIndex(UiMode mode) {
        switch (mode) {
        case STAND:
            return standSelection;
        case NON_STAND:
            return nonStandSelection;
        default:
            return -1;
        }
    }

}
