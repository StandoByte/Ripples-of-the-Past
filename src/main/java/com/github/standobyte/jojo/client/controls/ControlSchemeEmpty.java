package com.github.standobyte.jojo.client.controls;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.IPower;

public class ControlSchemeEmpty extends ControlScheme {
    
    @Override
    public boolean initLoadedFromConfig(IPower<?, ?> power) {
        return false;
    }

    @Override
    public void addIfMissing(Hotbar hotbarType, Action<?> action) {
    }

    @Override
    public void reset(IPower<?, ?> power) {
    }

    @Override
    protected ActionKeybindEntry addKeybindEntry(ActionKeybindEntry keybind) {
        return keybind;
    }

    @Override
    public boolean removeKeybindEntry(ActionKeybindEntry keybind) {
        return false;
    }
}
