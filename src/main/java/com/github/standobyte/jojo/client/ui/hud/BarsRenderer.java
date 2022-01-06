package com.github.standobyte.jojo.client.ui.hud;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui.Alignment;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui.BarsOrientation;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui.ElementTransparency;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.AbstractGui;

public abstract class BarsRenderer {
    static final int BARS_WIDTH_PX = 28;
    
    protected final AbstractGui gui;
    protected final Map<BarType, ElementTransparency> barTransparencies;
    
    protected int x;
    protected int y;
    
    BarsRenderer(AbstractGui gui, ElementTransparency energy, ElementTransparency stamina, ElementTransparency resolve) {
        this.gui = gui;
        barTransparencies = new EnumMap<>(BarType.class);
        barTransparencies.put(BarType.ENERGY, energy);
        barTransparencies.put(BarType.STAMINA, stamina);
        barTransparencies.put(BarType.RESOLVE, resolve);
    }

    void render(MatrixStack matrixStack, int x, int y, Alignment alignment, 
            @Nullable PowerClassification currentMode, INonStandPower nonStandPower, IStandPower standPower, 
            int tickCounter, float partialTick) {
        this.x = x;
        this.y = y;
        align(alignment);
        if (nonStandPower != null && nonStandPower.hasPower()) {
            // FIXME get energy costs
            renderBar(matrixStack, BarType.ENERGY, nonStandPower.getType(), 
                    currentMode == PowerClassification.NON_STAND, nonStandPower.getType().getColor(), 
                    nonStandPower.getEnergy(), nonStandPower.getMaxEnergy(), 250f, 450f, 
                    tickCounter, partialTick); 
        }
        if (standPower != null && standPower.hasPower()) {
            if (standPower.usesStamina()) {
                // FIXME get stamina costs
                renderBar(matrixStack, BarType.STAMINA, null, 
                        currentMode == PowerClassification.STAND, 0xFFFFFF, 
                        standPower.getStamina(), standPower.getMaxStamina(), 750f, 350f, 
                        tickCounter, partialTick);
            }
            if (standPower.usesResolve()) {
                int color = standPower.getType().getColor();
                renderBar(matrixStack, BarType.RESOLVE, null, 
                        currentMode == PowerClassification.STAND, color, 
                        standPower.getResolve(), standPower.getMaxResolve(), 0, 0, 
                        tickCounter, partialTick);
            }
        }
    }
    
    protected abstract void align(Alignment alignment);
    
    protected abstract void renderBar(MatrixStack matrixStack, BarType barType, NonStandPowerType<?> powerType, 
            boolean highlight, int color, 
            float value, float maxValue, float attackCostValue, float abilityCostValue, 
            int ticks, float partialTick);
    
    private static final float CYCLE_TICKS = 80;
    private static final float MAX_ALPHA_TICKS = 30;
    private static final float MAX_ALPHA = 1.0F;
    private static final float COEFF = MAX_ALPHA / MAX_ALPHA_TICKS;
    protected final float getHighlightAlpha(float ticks) {
        ticks %= CYCLE_TICKS;
        float alpha = ticks <= CYCLE_TICKS / 2 ? ticks * COEFF : ticks * -COEFF + COEFF * CYCLE_TICKS;
        return Math.min(alpha, MAX_ALPHA) + 0.25F;
    }
    
    protected static final int ICON_WIDTH = 12;
    protected static final int ICON_HEIGHT = 16;
    protected int[] getIconTex(BarType type, NonStandPowerType<?> powerType, BarsOrientation orientation) {
        switch (type) {
        case STAMINA:
            return new int[] {128, 0, ICON_WIDTH, ICON_HEIGHT, 1, 0, -7};
        case RESOLVE:
            switch (orientation) {
            case VERTICAL:
                return new int[] {128, 32, ICON_WIDTH, ICON_HEIGHT, 2, -999, -999};
            case HORIZONTAL:
                return new int[] {128, 64, ICON_HEIGHT + 1, ICON_WIDTH - 2, 2, -4, 0};
            }
        case ENERGY:
            int texY = 160;
            int horizontalYOffset = 0;
            if (powerType == ModNonStandPowers.VAMPIRISM.get()) {
                texY = 0;
                horizontalYOffset = -3;
            }
            else if (powerType == ModNonStandPowers.HAMON.get()) {
                texY = 16;
                horizontalYOffset = -7;
            }
            return new int[] {240, texY, ICON_WIDTH, ICON_HEIGHT, 1, 0, horizontalYOffset};
        default:
            return new int[] {240, 160, ICON_WIDTH, ICON_HEIGHT, 1, 0, 0};
        }
    }
    
    protected final void renderIcon(MatrixStack matrixStack, int x, int y, 
            int texX, int texY, int width, int height, int scale) {
        if (scale > 1) {
            matrixStack.pushPose();
            matrixStack.scale(1F / scale, 1F / scale, 1F);
            matrixStack.translate(x * (scale - 1), y * (scale - 1), 0);
        }
        gui.blit(matrixStack, x, y, 
                texX, texY, width * scale, height * scale);
        if (scale > 1) {
            matrixStack.popPose();
        }
    }
    
    protected enum BarType {
        ENERGY,
        STAMINA,
        RESOLVE
    }
}
