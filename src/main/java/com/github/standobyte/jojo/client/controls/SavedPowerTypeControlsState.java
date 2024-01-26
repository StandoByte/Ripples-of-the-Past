package com.github.standobyte.jojo.client.controls;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.power.IPower;

public class SavedPowerTypeControlsState {
    // TODO slots for custom layout variants
    @Nonnull private ControlScheme.SaveState currentControlScheme;
    
    public SavedPowerTypeControlsState(ControlScheme.SaveState currentControlScheme) {
        this.currentControlScheme = currentControlScheme;
    }
    
    public ControlScheme createControlScheme(IPower<?, ?> power) {
        return currentControlScheme.createControlScheme(power);
    }
}
