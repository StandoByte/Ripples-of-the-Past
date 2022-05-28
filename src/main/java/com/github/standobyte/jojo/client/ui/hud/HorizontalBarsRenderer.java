package com.github.standobyte.jojo.client.ui.hud;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui.Alignment;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui.BarsOrientation;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui.ElementTransparency;
import com.github.standobyte.jojo.power.nonstand.type.NonStandPowerType;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.AbstractGui;

public class HorizontalBarsRenderer extends BarsRenderer {
    private static final int BAR_LENGTH = 200;
    private static final int BAR_LENGTH_SHORTENED = 150;
    
    private Alignment alignment;

    HorizontalBarsRenderer(AbstractGui gui, ElementTransparency energy, ElementTransparency stamina, ElementTransparency resolve) {
        super(gui, energy, stamina, resolve);
    }

    @Override
    protected void align(Alignment alignment) {
        this.alignment = alignment;
        if (alignment == Alignment.RIGHT) {
            x -= BAR_LENGTH + ICON_WIDTH + 4;
        }
    }

    @Override
    protected void renderBarWithIcon(MatrixStack matrixStack, BarType barType, NonStandPowerType<?> powerType, 
            boolean highlight, int color, float iconFill, 
            float value, float maxValue, float attackCostValue, float abilityCostValue, float tranclucentBarValue, 
            int ticks, float partialTick) {
        int barLength = highlight ? BAR_LENGTH : BAR_LENGTH_SHORTENED;
        int fill = (int) ((float) barLength * (value / maxValue));
        int texY = barType == BarType.STAMINA ? 176 : 160;
        
        int barX = x;
        if (alignment == Alignment.LEFT) {
            barX += ICON_WIDTH + 1;
        }
        
        if (highlight) {
            renderBar(matrixStack, barX, y, alignment, 
                    0, texY, 8, barLength, fill, color, alphaMultiplier(barType), 
                    0, 128, 1, 145, 
                    (int) ((float) barLength * (tranclucentBarValue / maxValue)), 
                    (int) (attackCostValue / maxValue), (int) (attackCostValue / maxValue), 
                    ClientUtil.getHighlightAlpha(ticks + partialTick, 80F, 30F, 1.25F, 0.25F));
            int[] iconTex = getIconTex(barType, powerType, BarsOrientation.HORIZONTAL);
            int iconX = x;
            if (alignment == Alignment.RIGHT) {
                iconX += BAR_LENGTH + 3;
            }
            else {
                iconX += iconTex[5];
            }
            renderIcon(matrixStack, iconX, y + iconTex[6], 
                    iconTex[0], iconTex[1], iconTex[2], iconTex[3], iconTex[4]);
            if (barType == BarType.RESOLVE && iconFill > 0) {
                renderIcon(matrixStack, iconX, y + iconTex[6], 
                        iconTex[0] + 40, iconTex[1], (int) ((float) iconTex[2] * iconFill), iconTex[3], iconTex[4]);
            }
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            y += 12;
        }
        else {
            ElementTransparency transparency = barTransparencies.get(barType);
            if (transparency.shouldRender()) {
                int xOffset = alignment == Alignment.RIGHT ? BAR_LENGTH - BAR_LENGTH_SHORTENED : 0;
                renderBar(matrixStack, barX + xOffset, y, alignment, 
                        0, texY + 8, 5, barLength, fill, color, transparency.getAlpha(partialTick) * alphaMultiplier(barType), 
                        0, 136, 1, 153, 
                        0, 0, 0, 0);
            }
            y += 9;
        }
    }
    
    @Override
    protected void barFill(MatrixStack matrixStack, int x, int y, Alignment alignment, 
            int texX, int texY, int width, int length, int fill) {
        if (alignment == Alignment.RIGHT) {
            // it just works
            matrixStack.pushPose();
            matrixStack.scale(-1, 1, 1);
            fill = length - fill;
            gui.blit(matrixStack, 
                    -(x + fill + 1), y + 1, 
                    -(texX - length + fill - 1), texY + 1, 
                    -length + fill, width - 2);
            matrixStack.popPose();
        }
        else {
            gui.blit(matrixStack, 
                    x + 1, y + 1, 
                    texX + 1, texY + 1, 
                    fill, width - 2);
        }
    }
    
    @Override
    protected void drawBarElement(MatrixStack matrixStack, int x, int y, int texX, int texY, int width, int length) {
        super.drawBarElement(matrixStack, x, y, texX, texY, length, width);
    }

    @Override
    protected void setResolveBonusTextPos(int barsX, int barsY, Alignment barsAlignment) {
        switch (barsAlignment) {
        case LEFT:
            this.resolveBonusX = barsX + 245;
            break;
        case RIGHT:
            this.resolveBonusX = barsX - 219;
            break;
        }
        this.resolveBonusY = barsY + 13;
        this.resolveBonusAlignment = barsAlignment;
    }
}
