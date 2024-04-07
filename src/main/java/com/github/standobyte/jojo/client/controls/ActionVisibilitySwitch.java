package com.github.standobyte.jojo.client.controls;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;

import net.minecraft.util.ResourceLocation;

public class ActionVisibilitySwitch {
    ActionsHotbar parentHotbar;
    final ResourceLocation actionId;
    Action<?> action;
    private boolean isEnabled;
    
    ActionVisibilitySwitch(ActionsHotbar parentHotbar, ResourceLocation actionId, boolean isEnabled) {
        this.parentHotbar = parentHotbar;
        this.actionId = actionId;
        this.isEnabled = isEnabled;
    }
    
    ActionVisibilitySwitch(ActionsHotbar parentHotbar, Action<?> action, boolean isEnabled) {
        this.parentHotbar = parentHotbar;
        this.action = action;
        this.actionId = action.getRegistryName();
        this.isEnabled = isEnabled;
    }
    
    public void init() {
        this.action = JojoCustomRegistries.ACTIONS.fromId(this.actionId);
    }
    
    public Action<?> getAction() {
        return action;
    }
    
    public ResourceLocation getActionId() {
        return actionId;
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }
    
    public void setIsEnabled(boolean isEnabled) {
        if (this.isEnabled != isEnabled) {
            this.isEnabled = isEnabled;
            parentHotbar.updateCache();
        }
    }
    
}
