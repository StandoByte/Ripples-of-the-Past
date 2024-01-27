package com.github.standobyte.jojo.client.controls;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.power.IPower;

public class PowerTypeControlSchemes {
    // TODO slots for custom layout variants
    @Nonnull ControlScheme currentControlScheme;
    
    public PowerTypeControlSchemes(ControlScheme currentControlSchemeLoaded) {
        this.currentControlScheme = currentControlSchemeLoaded;
    }
    
    // it won't be called before update, right?
    // right?
    public ControlScheme getCurrentCtrlScheme() {
        return currentControlScheme;
    }
    
    public void update(IPower<?, ?> power) {
        currentControlScheme.initLoadedFromConfig(power);
        currentControlScheme.update(power);
    }
}
