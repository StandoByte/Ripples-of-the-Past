package com.github.standobyte.jojo.client.ui.actionshud;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui.Alignment;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui.BarsOrientation;
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
    protected void renderBarWithIcon(MatrixStack matrixStack, BarType barType, 
            boolean fullSize, int color, float iconFill, 
            float value, float maxValue, 
            float attackCostValue, float abilityCostValue, float costTick, 
            float translucentBarValue, 
            float alpha, int ticks, float partialTick) {
        int barHeight = fullSize ? BAR_HEIGHT : BAR_HEIGHT_SHORTENED;
        int fill = (int) ((float) barHeight * (value / maxValue));
        fill = Math.min(fill, barHeight);
        int translucentFill = (int) ((float) barHeight * (translucentBarValue / maxValue));
        fill = Math.min(fill, barHeight);
        int texX = barType == BarType.STAMINA ? 48 : 32;
        
        if (fullSize) {
            renderBar(matrixStack, x, y, null, 
                    texX, 0, 8, barHeight, fill, color, alpha, 
                    0, 0, 17, 1, 
                    translucentFill, 
                    (int) (barHeight * attackCostValue / maxValue), (int) (barHeight * abilityCostValue / maxValue), costTick, 
                    fullSize, barType, partialTick);
            int[] iconTex = getIconTex(barType, BarsOrientation.VERTICAL);
            renderIcon(matrixStack, x - 2, y - iconTex[3] - 1, 
                    iconTex[0], iconTex[1], iconTex[2], iconTex[3], iconTex[4]);
            if (barType == BarType.RESOLVE && iconFill > 0) {
                renderIcon(matrixStack, x - 2, y - iconTex[3] - 1, 
                        iconTex[0] + 40, iconTex[1], iconTex[2], (int) ((float) iconTex[3] * iconFill), iconTex[4]);
            }
            x += 12;
        }
        else {
            ElementTransparency transparency = barTransparencies.get(barType);
            if (transparency.shouldRender()) {
                renderBar(matrixStack, x, y + BAR_HEIGHT - BAR_HEIGHT_SHORTENED, null, 
                        texX + 8, 0, 5, barHeight, fill, color, transparency.getAlpha(partialTick) * alpha, 
                        8, 0, 25, 1, 
                        translucentFill, 0, 0, 0, 
                        fullSize, barType, partialTick);
            }
            x += 9;
        }
    }
    
    @Override
    protected void barFill(MatrixStack matrixStack, int x, int y, Alignment alignment, 
            int texX, int texY, int width, int length, int fill) {
        gui.blit(matrixStack, x + 1, y + length - fill + 1, 
                texX + 1, texY + length - fill + 1, width - 2, fill);
    }
    
    @Override
    protected void barFillEffect(MatrixStack matrixStack, int x, int y, Alignment alignment, 
            int width, int length, int fill, BarType barType) {
        if (barType == BarType.ENERGY_HAMON) {
            // TODO particles effect
        }
    }

    @Override
    protected void renderCost(MatrixStack matrixStack, 
            int x, int y, Alignment alignment, 
            int width, int height, 
            int costFill, int barFill, 
            int xOffset, float alpha) {
        if (costFill > 0) {
            int diff = Math.max(barFill - costFill, 0);
            AbstractGui.fill(matrixStack, 
                    x + xOffset + 1,               y - diff + height + 1 - costFill, 
                    x + xOffset + (width / 2) - 1, y - diff + height + 1, 
                    ClientUtil.addAlpha(0xFFFFFF, alpha));
            RenderSystem.enableBlend();
        }
    }

    @Override
    protected void renderRedHighlight(MatrixStack matrixStack, 
            int x, int y, Alignment alignment, 
            int width, int height, 
            float alpha) {
        AbstractGui.fill(matrixStack, 
                x + 1,         y + 1, 
                x + width - 1, y + height + 1, 
                ClientUtil.addAlpha(0xFF0000, alpha));
        RenderSystem.enableBlend();
    }

    @Override
    protected void setResolveBonusTextPos(int barsX, int barsY, Alignment barsAlignment) {
        switch (barsAlignment) {
        case LEFT:
            this.resolveBonusX = barsX + 38;
            break;
        case RIGHT:
            this.resolveBonusX = barsX - 7;
            break;
        }
        this.resolveBonusY = barsY + 106;
        this.resolveBonusAlignment = barsAlignment;
    }
}
