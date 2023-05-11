package com.github.standobyte.jojo.client.ui.actionshud;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.IPower;

import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;

public class QuickAccess<P extends IPower<P, ?>> {
    private final Action<P> action;
    private final P power;
    
    private QuickAccess(Action<P> action, P power) {
        this.action = action;
        this.power = power;
    }
    
    
    
    public static enum QuickAccessKeyConflictContext implements IKeyConflictContext {
        INSTANCE;

        @Override
        public boolean isActive() {
            ActionsOverlayGui actionsHud = ActionsOverlayGui.getInstance();
            if (actionsHud == null) return false;
            return actionsHud.isQuickAccessActive();
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other == KeyConflictContext.IN_GAME;
        }
        
    }
}
