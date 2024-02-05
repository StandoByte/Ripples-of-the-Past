package com.github.standobyte.jojo.client.ui.actionshud;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.IPower;

import net.minecraft.client.Minecraft;
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
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.player.isSpectator()) return false;
            
            ActionsOverlayGui actionsHud = ActionsOverlayGui.getInstance();
            if (actionsHud == null) return false;
            
            return actionsHud.areHotbarsEnabled();
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other == KeyConflictContext.IN_GAME;
        }
        
    }
}
