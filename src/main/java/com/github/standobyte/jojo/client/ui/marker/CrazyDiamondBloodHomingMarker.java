package com.github.standobyte.jojo.client.ui.marker;

import java.util.List;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.CrazyDiamondBlockBullet;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class CrazyDiamondBloodHomingMarker extends MarkerRenderer {
    
    public CrazyDiamondBloodHomingMarker(Minecraft mc) {
        super(new ResourceLocation(JojoMod.MOD_ID, "textures/icons/blood_drops.png"), mc);
    }
    
    @Override
    protected boolean shouldRender() {
        ActionsOverlayGui hud = ActionsOverlayGui.getInstance();
        return hud.showExtraActionHud(ModStandsInit.CRAZY_DIAMOND_BLOCK_BULLET.get())
               && !mc.player.isShiftKeyDown();
    }
    
    @Override
    protected void updatePositions(List<MarkerInstance> list, float partialTick) {
        fillWithStandEffectTargets(list, partialTick, ModStandEffects.DRIED_BLOOD_DROPS.get(), CrazyDiamondBlockBullet.PLAYER_TRACKING_RANGE, mc, true);
    }
}
