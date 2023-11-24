package com.github.standobyte.jojo.client.ui.marker;

import java.util.List;

import com.github.standobyte.jojo.action.stand.GoldExperienceRevertLifeform;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.screen.stand.ge.EntityTypeIcon;
import com.github.standobyte.jojo.entity.GETransformationEntity;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class GoldExperienceLifeformMarker extends MarkerRenderer {
    
    public GoldExperienceLifeformMarker(Minecraft mc) {
        super(null, mc);
    }
    
    @Override
    protected boolean shouldRender() {
        ActionsOverlayGui hud = ActionsOverlayGui.getInstance();
        return hud.showExtraActionHud(ModStandsInit.GOLD_EXPERIENCE_CREATE_LIFEFORM.get());
    }
    
    @Override
    protected void renderIcon(MatrixStack matrixStack, MarkerInstance marker, float partialTick) {}
    
    @Override
    protected void renderIconOnBorder(MatrixStack matrixStack, MarkerInstance marker, float partialTick) {
        marker.standEffect.ifPresent(effect -> {
            Entity target = effect.getTarget();
            if (target != null) {
                if (target instanceof GETransformationEntity) { 
                    GETransformationEntity tfEntity = (GETransformationEntity) target;
                    Entity tfTarget = tfEntity.getTransformationTarget();
                    if (tfTarget != null) {
                        float ratio = tfEntity.getTfProgressTime(partialTick) / tfEntity.getDuration();
                        ActionsOverlayGui.renderRadialIndicator(matrixStack, 0, 0, ratio);
                        
                        matrixStack.pushPose();
                        matrixStack.scale(0.75F, 0.75F, 1);
                        EntityTypeIcon.renderIcon(tfTarget.getType(), matrixStack, 16, -10);
                        matrixStack.popPose();
                    }
                }
                else {
                    EntityTypeIcon.renderIcon(target.getType(), matrixStack, 0, 0);
                }
            }
        });
    }
    
    @Override
    protected void updatePositions(List<MarkerInstance> list, float partialTick) {
        updateGELifeformMarkers(list, partialTick, mc, false);
    }
    
    public static void updateGELifeformMarkers(List<MarkerInstance> list, float partialTick, Minecraft mc, boolean highlightLookedAt) {
        fillWithStandEffectTargets(list, partialTick, ModStandEffects.GE_CREATED_LIFEFORM.get(), 
                GoldExperienceRevertLifeform.MARKER_DISTANCE, mc, highlightLookedAt);
        
        for (MarkerInstance marker : list) {
            marker.standEffect.ifPresent(effect -> {
                if (effect.getTarget() instanceof GETransformationEntity) {
                    GETransformationEntity tfEntity = (GETransformationEntity) effect.getTarget();
                    Entity tfTarget = tfEntity.getTransformationTarget();
                    if (tfTarget != null) {
                        marker.pos = tfEntity.getPosition(partialTick).add(0, tfTarget.getBbHeight() * 1.1, 0);
                    }
                }
            });
        }
    }
}
