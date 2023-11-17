package com.github.standobyte.jojo.client.ui.marker;

import java.util.List;

import com.github.standobyte.jojo.client.ui.screen.stand.ge.EntityTypeIcon;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;

public class GoldExperienceLifeformMarker extends MarkerRenderer {
    
    public GoldExperienceLifeformMarker(Minecraft mc) {
        super(null, mc);
    }
    
    @Override
    protected boolean shouldRender() {
//        ActionsOverlayGui hud = ActionsOverlayGui.getInstance();
//        return hud.showExtraActionHud(ModStandsInit.CRAZY_DIAMOND_BLOCK_BULLET.get())
//               && !mc.player.isShiftKeyDown();
        return false;
    }
    
    @Override
    protected void renderIcon(MatrixStack matrixStack, MarkerInstance marker) {
        marker.standEffect.ifPresent(effect -> {
            LivingEntity target = effect.getTarget();
            if (target != null) {
                EntityTypeIcon.renderIcon(target.getType(), matrixStack, 0, 0);
            }
        });
    }
    
    @Override
    protected void updatePositions(List<MarkerInstance> list, float partialTick) {
        fillWithStandEffectTargets(list, partialTick, ModStandEffects.GE_CREATED_LIFEFORM.get(), 32, mc, false);
    }
}
