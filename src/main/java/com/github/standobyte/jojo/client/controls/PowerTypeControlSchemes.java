package com.github.standobyte.jojo.client.controls;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.power.IPower;

import net.minecraft.util.ResourceLocation;

public class PowerTypeControlSchemes {
    public final ResourceLocation powerTypeId;
    // TODO slots for custom layout variants
    @Nonnull ControlScheme currentControlScheme;
    
    public PowerTypeControlSchemes(ResourceLocation powerTypeId, ControlScheme currentControlSchemeLoaded) {
        this.powerTypeId = powerTypeId;
        this.currentControlScheme = currentControlSchemeLoaded;
    }
    
    void update(IPower<?, ?> power) {
        currentControlScheme.initLoadedFromConfig(power);
        currentControlScheme.update(power);
    }
    
    public ControlScheme getCurrentCtrlScheme() {
        return currentControlScheme;
    }
}
