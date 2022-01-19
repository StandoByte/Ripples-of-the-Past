package com.github.standobyte.jojo.client.ui.hud;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui.Alignment;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui.BarsOrientation;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui.ElementTransparency;
import com.github.standobyte.jojo.power.nonstand.type.NonStandPowerType;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.AbstractGui;

public class VerticalBarsRenderer extends BarsRenderer {
    static final int BAR_HEIGHT = 100;
    private static final int BAR_HEIGHT_SHORTENED = 75;

    VerticalBarsRenderer(AbstractGui gui, ElementTransparency energy, ElementTransparency stamina, ElementTransparency resolve) {
        super(gui, energy, stamina, resolve);
    }

    @Override
    protected void align(Alignment alignment) {
        if (alignment == Alignment.RIGHT) {
            x -= BARS_WIDTH_PX;
        }
    }

    @Override
    protected void renderBarWithIcon(MatrixStack matrixStack, BarType barType, NonStandPowerType<?> powerType, 
            boolean highlight, int color, 
            float value, float maxValue, float attackCostValue, float abilityCostValue, float tranclucentBarValue, 
            int ticks, float partialTick) {
        int barHeight = highlight ? BAR_HEIGHT : BAR_HEIGHT_SHORTENED;
        int fill = (int) ((float) barHeight * (value / maxValue));
        int texX = barType == BarType.STAMINA ? 48 : 32;
        
        if (highlight) {
            renderBar(matrixStack, x, y, 
                    texX, 0, 8, barHeight, fill, color, 1.0F, 
                    0, 0, 17, 1, 
                    (int) ((float) barHeight * (tranclucentBarValue / maxValue)), 
                    (int) (attackCostValue / maxValue), (int) (attackCostValue / maxValue), 
                    ClientUtil.getHighlightAlpha(ticks + partialTick, 80F, 30F, 1.25F, 0.25F));
            int[] iconTex = getIconTex(barType, powerType, BarsOrientation.VERTICAL);
            renderIcon(matrixStack, x - 2, y - iconTex[3] - 1, 
            iconTex[0], iconTex[1], iconTex[2], iconTex[3], iconTex[4]);
            x += 12;
        }
        else {
            ElementTransparency transparency = barTransparencies.get(barType);
            if (transparency.shouldRender()) {
                renderBar(matrixStack, x, y + BAR_HEIGHT - BAR_HEIGHT_SHORTENED, 
                        texX + 8, 0, 5, barHeight, fill, color, transparency.getAlpha(partialTick), 
                        8, 0, 25, 1, 
                        0, 0, 0, 0);
            }
            x += 9;
        }
    }
    
    private void renderBar(MatrixStack matrixStack, int x, int y, 
            int texX, int texY, int width, int length, int fill, int barColor, float barAlpha, 
            int borderTexX, int borderTexY, int scaleTexX, int scaleTexY, 
            int tranclucentFill, int cost1Fill, int cost2Fill, float costAlpha) {
        if (barAlpha > 0) {
            float[] rgb = ClientUtil.rgb(barColor);
            if (tranclucentFill > 0) {
                RenderSystem.color4f(rgb[0], rgb[1], rgb[2], barAlpha / 0.5F);
                barFill(matrixStack, x, y, texX, texY, width, length, tranclucentFill);
            }
            RenderSystem.color4f(rgb[0], rgb[1], rgb[2], barAlpha);
            if (fill > 0) {
                barFill(matrixStack, x, y, texX, texY, width, length, fill);
            }
            // border
            gui.blit(matrixStack, x, y, borderTexX, borderTexY, width, length + 2);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, barAlpha);
            // cost
            if (costAlpha > 0) {
                renderCost(matrixStack, x, y, cost1Fill, fill, length, 0, costAlpha);
                renderCost(matrixStack, x, y, cost2Fill, fill, length, width / 2 - 1, costAlpha);
            }
            // scale
            gui.blit(matrixStack, x + 1, y + 1, scaleTexX, scaleTexY, width - 2, length);
            if (barAlpha != 1.0F) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }
    
    private void barFill(MatrixStack matrixStack, int x, int y, 
            int texX, int texY, int width, int length, int fill) {
        gui.blit(matrixStack, x + 1, y + length - fill + 1, 
                texX + 1, texY + length - fill + 1, width - 2, fill);
    }
    
    private void renderCost(MatrixStack matrixStack, int barX, int barY, 
            int costFill, int barFill, int barHeight, int xOffset, float alpha) {
        if (costFill > 0) {
            boolean notEnough = costFill > barFill;
            AbstractGui.fill(matrixStack, 
                    barX + xOffset + 1, notEnough ? barY + barHeight - costFill + 1 : barY - barFill + barHeight + 1, 
                    barX + xOffset + 4, notEnough ? barY + barHeight + 1 : barY - barFill + barHeight + costFill + 1, 
                    ElementTransparency.addAlpha(0xFFFFFF, alpha * 0.5F));
            RenderSystem.enableBlend();
        }
    }
}
