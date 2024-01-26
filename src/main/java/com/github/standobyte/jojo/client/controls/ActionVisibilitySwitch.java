package com.github.standobyte.jojo.client.controls;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;

import net.minecraft.util.ResourceLocation;

public class ActionVisibilitySwitch {
    final Action<?> action;
    boolean isVisible;
    
    private ActionVisibilitySwitch(Action<?> action, boolean isVisible) {
        this.action = action;
        this.isVisible = isVisible;
    }
    
    
    
    static class SaveInfo {
        public ResourceLocation action;
        public boolean isVisible;
        
        public SaveInfo(ResourceLocation action, boolean isVisible) {
            this.action = action;
            this.isVisible = isVisible;
        }

        @Nullable
        ActionVisibilitySwitch createSwitch() {
            Action<?> action = JojoCustomRegistries.ACTIONS.fromId(this.action);
            if (action != null) {
                return new ActionVisibilitySwitch(action, isVisible);
            }
            return null;
        }
    }
}
