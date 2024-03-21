package com.github.standobyte.jojo.client.ui.actionshud;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.ClientTicking;
import com.github.standobyte.jojo.client.ClientTicking.ITicking;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.controls.ControlScheme;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui.Alignment;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui.BarsOrientation;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public abstract class BarsRenderer {
    static final int BARS_WIDTH_PX = 28;
    
    protected final AbstractGui gui;
    protected final Map<BarType, ElementTransparency> barTransparencies;
    
    protected int x;
    protected int y;
    
    BarsRenderer(AbstractGui gui, ElementTransparency energy, ElementTransparency stamina, ElementTransparency resolve) {
        this.gui = gui;
        barTransparencies = new EnumMap<>(BarType.class);
        barTransparencies.put(BarType.ENERGY_HAMON, energy);
        barTransparencies.put(BarType.ENERGY_VAMPIRE, energy);
        barTransparencies.put(BarType.ENERGY_OTHER, energy);
        barTransparencies.put(BarType.STAMINA, stamina);
        barTransparencies.put(BarType.RESOLVE, resolve);
    }

    void render(MatrixStack matrixStack, int x, int y, Alignment alignment, 
            @Nullable ActionsModeConfig<?> currentMode, ActionsModeConfig<INonStandPower> nonStandMode, ActionsModeConfig<IStandPower> standMode, 
            int tickCounter, float partialTick, Minecraft mc) {
        PowerClassification currentModeType = currentMode != null ? currentMode.powerClassification : null;
        INonStandPower nonStandPower = nonStandMode.getPower();
        IStandPower standPower = standMode.getPower();
        this.x = x;
        this.y = y;
        
        align(alignment);

        float attackCost = 0;
        float abilityCost = 0;
        // FIXME get energy/stamina costs
//        if (currentMode != null) {
//            boolean shift = mc.player.isShiftKeyDown();
//            attackCost = getActionCost(currentMode, ActionType.ATTACK, mc.player, shift);
//            abilityCost = getActionCost(currentMode, ActionType.ABILITY, mc.player, shift);
//            if (abilityCost < 0) {
//                abilityCost = attackCost;
//            }
//            else if (attackCost < 0) {
//                attackCost = abilityCost;
//            }
//            if (attackCost < 0/* && abilityCost < 0*/) {
//                attackCost = 0;
//                abilityCost = 0;
//            }
//        }
        
        if (nonStandPower != null && nonStandPower.hasPower()) {
            float energy = nonStandPower.getEnergy();
            
            float translucentVal = nonStandPower
                    .getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> MathHelper.lerp(partialTick, hamon.getBreathStability(), hamon.getPrevBreathStability()))
                    .orElse(0F);
            
            float maxEnergy = nonStandPower
                    .getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> hamon.getMaxBreathStability())
                    .orElse(nonStandPower.getMaxEnergy());
            
            BarType type = null;
            if (nonStandPower.getType() == ModPowers.HAMON.get()) {
                type = BarType.ENERGY_HAMON;
            }
            else if (nonStandPower.getType() == ModPowers.VAMPIRISM.get()) {
                type = BarType.ENERGY_VAMPIRE;
            }
            else {
                type = BarType.ENERGY_OTHER;
            }
            
            if (type != null) {
                // FIXME ! (hamon 2) bar render effect
                renderBarStart(matrixStack, type, 
                        currentModeType == PowerClassification.NON_STAND, ActionsOverlayGui.getPowerUiColor(nonStandPower), 1, 
                        energy, maxEnergy, 
                        attackCost, abilityCost, nonStandMode.getSelectedTick() + partialTick, 
                        translucentVal, 
                        1F, tickCounter, partialTick);
            }
        }
        if (standPower != null && standPower.hasPower()) {
            if (standPower.usesStamina() && !standPower.isStaminaInfinite()) {
                float stamina = standPower.getStamina();
                renderBarStart(matrixStack, BarType.STAMINA, 
                        currentModeType == PowerClassification.STAND, 0xFFFFFF, 1, 
                        stamina, standPower.getMaxStamina(), 
                        attackCost, abilityCost, standMode.getSelectedTick() + partialTick,  
                        0, 
                        StandUtil.standIgnoresStaminaDebuff(standPower) ? 0.3F : 1F, tickCounter, partialTick);
            }
            if (standPower.usesResolve()) {
                float resolve = MathHelper.lerp(partialTick, 
                        Math.min(standPower.getPrevTickResolve(), standPower.getResolve()), 
                        Math.max(standPower.getPrevTickResolve(), standPower.getResolve()));
                int color = ActionsOverlayGui.getPowerUiColor(standPower);
                renderBarStart(matrixStack, BarType.RESOLVE, 
                        currentModeType == PowerClassification.STAND, color, (float) standPower.getResolveLevel() / (float) standPower.getMaxResolveLevel(), 
                        resolve, standPower.getMaxResolve(), 
                        0, 0, 0, 
                        standPower.getResolveCounter().getMaxAchievedValue(), 
                        1F, tickCounter, partialTick);
                setResolveBonusTextPos(x, y, alignment);
            }
        }
    }
    
    protected <P extends IPower<P, ?>> float getActionCost(ActionsModeConfig<P> mode, 
            ControlScheme.Hotbar hotbar, LivingEntity user, boolean shift, ActionTarget target) {
        Action<P> action = mode.getSelectedAction(hotbar, shift, target);
        
        if (action != null) {
            return action.getCostToRender(mode.getPower(), target);
        }
        return -1;
    }

    protected void setResolveBonusTextPos(int barsX, int barsY, Alignment barsAlignment) {}
    
    protected int resolveBonusX;
    protected int resolveBonusY;
    protected Alignment resolveBonusAlignment;
    void drawTextAfterRender(MatrixStack matrixStack, 
            @Nullable PowerClassification currentMode, INonStandPower nonStandPower, IStandPower standPower, 
            int tickCounter, float partialTick, FontRenderer font, ActionsOverlayGui hud) {
        if (currentMode == PowerClassification.STAND && standPower != null && standPower.hasPower() && standPower.usesResolve()) {
            float bonus = standPower.getResolveCounter().getBoostVisible(standPower.getUser());
            if (bonus > 1) {
                drawText(matrixStack, new StringTextComponent("x" + String.format("%.2f", bonus)), 
                        resolveBonusX, resolveBonusY, Alignment.RIGHT, ActionsOverlayGui.getPowerUiColor(standPower), partialTick, font, hud);
            }
        }
    }
    
    protected abstract void align(Alignment alignment);
    
    private void renderBarStart(MatrixStack matrixStack, BarType barType, 
            boolean fullSize, int color, float iconFill, 
            float value, float maxValue, 
            float attackCostValue, float abilityCostValue, float costTick, 
            float translucentBarValue, 
            float alpha, int ticks, float partialTick) {
        BarEffects lerpData = getBarEffects(barType);
        renderBarWithIcon(matrixStack, barType, 
                fullSize, color, iconFill, 
                lerpData.lerpValue(value, partialTick), maxValue, 
                attackCostValue, abilityCostValue, costTick, 
                lerpData.lerpTranslucentValue(translucentBarValue, partialTick), 
                alpha, ticks, partialTick);
    }
    
    protected abstract void renderBarWithIcon(MatrixStack matrixStack, BarType barType, 
            boolean fullSize, int color, float iconFill, 
            float value, float maxValue, 
            float attackCostValue, float abilityCostValue, float costTick, 
            float translucentBarValue, 
            float alpha, int ticks, float partialTick);
    
    protected final void renderBar(MatrixStack matrixStack, int x, int y, Alignment alignment, 
            int texX, int texY, int width, int length, int fill, int barColor, float barAlpha, 
            int borderTexX, int borderTexY, int scaleTexX, int scaleTexY, 
            int tranclucentFill, int cost1Fill, int cost2Fill, float costTick, 
            boolean fillEffect, BarType barType, float partialTick) {
        if (barAlpha > 0) {
            float[] rgb = ClientUtil.rgb(barColor);
            if (tranclucentFill > 0) {
                RenderSystem.color4f(rgb[0], rgb[1], rgb[2], barAlpha * 0.4F);
                barFill(matrixStack, x, y, alignment, texX, texY, width, length, tranclucentFill);
            }
            RenderSystem.color4f(rgb[0], rgb[1], rgb[2], barAlpha);
            if (fill > 0) {
                barFill(matrixStack, x, y, alignment, texX, texY, width, length, fill);
                if (fillEffect /*&& Minecraft.getInstance().options.graphicsMode != GraphicsFanciness.FAST*/) {
                    barFillEffect(matrixStack, x, y, alignment, width, length, fill, barType);
                }
            }
            // border
            drawBarElement(matrixStack, x, y, borderTexX, borderTexY, width, length + 2);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, barAlpha);
            // cost
            // FIXME cost
            float costAlpha = ClientUtil.getHighlightAlpha(costTick + 40F, 80F, 60F, 0.15F, 0.6F);
            renderCost(matrixStack, x, y, alignment, 
                    width, length, cost1Fill, fill, 0, 
                    barAlpha * costAlpha);
            renderCost(matrixStack, x, y, alignment, 
                    width, length, cost2Fill, fill, 3, 
                    barAlpha * costAlpha);
            // red highlight
            BarEffects barEffects = getBarEffects(barType);
            if (barEffects.redHighlightTick > 0) {
                float tick = barEffects.redHighlightTick - partialTick;
                float alpha = ClientUtil.getHighlightAlpha(tick, 10F, 8F, tick > 5F ? 0.25F : 0, 0.75F);
                renderRedHighlight(matrixStack, x, y, alignment, 
                        width, length, alpha);
            }
            // scale
            drawBarElement(matrixStack, x + 1, y + 1, scaleTexX, scaleTexY, width - 2, length);
            if (barAlpha != 1.0F) {
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }
    
    protected abstract void barFill(MatrixStack matrixStack, int x, int y, Alignment alignment, 
            int texX, int texY, int width, int length, int fill);
    
    protected void barFillEffect(MatrixStack matrixStack, int x, int y, Alignment alignment, 
            int width, int length, int fill, BarType barType) {}
    
    protected void drawBarElement(MatrixStack matrixStack, int x, int y, int texX, int texY, int width, int length) {
        gui.blit(matrixStack, x, y, texX, texY, width, length);
    }
    
    protected void renderCost(MatrixStack matrixStack, 
            int x, int y, Alignment alignment, 
            int width, int length, 
            int costFill, int barFill, 
            int offset, float alpha) {}

    protected void renderRedHighlight(MatrixStack matrixStack, 
            int x, int y, Alignment alignment, 
            int width, int length, 
            float alpha) {}
    
    protected static final int ICON_WIDTH = 12;
    protected static final int ICON_HEIGHT = 16;
    protected int[] getIconTex(BarType type, BarsOrientation orientation) {
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
        case ENERGY_HAMON:
            return new int[] {240, 16, ICON_WIDTH, ICON_HEIGHT, 1, 0, -7};
        case ENERGY_VAMPIRE:
            return new int[] {240, 0, ICON_WIDTH, ICON_HEIGHT, 1, 0, -3};
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
    
    public enum BarType {
        ENERGY_HAMON,
        ENERGY_VAMPIRE,
        ENERGY_OTHER,
        STAMINA,
        RESOLVE
    }
    
    
    
    private static final Map<BarType, BarEffects> BAR_EFFECTS = Util.make(new HashMap<>(), map -> {
        for (BarType barType : BarType.values()) {
            map.put(barType, new BarEffects());
        }
    });
    
    public static class BarEffects implements ITicking {
        private int redHighlightTick = 0;
        
        private float prevTickValue;
        private float prevTickTranslucentValue;
        private float thisTickValue;
        private float thisTickTranslucentValue;
        
        private BarEffects() {
            ClientTicking.addTicking(this);
        }
        
        @Override
        public void tick() {
            this.prevTickValue = this.thisTickValue;
            this.prevTickTranslucentValue = this.thisTickTranslucentValue;
            
            if (redHighlightTick > 0) redHighlightTick--;
        }
        
        public void triggerRedHighlight(int ticks) {
            this.redHighlightTick = ticks;
        }
        
        public void resetRedHighlight() {
            this.redHighlightTick = redHighlightTick % 20;
        }
        
        private float lerpValue(float value, float partialTick) {
            this.thisTickValue = value;
            return MathHelper.lerp(partialTick, prevTickValue, value);
        }
        
        private float lerpTranslucentValue(float value, float partialTick) {
            this.thisTickTranslucentValue = value;
            return MathHelper.lerp(partialTick, prevTickTranslucentValue, value);
        }
    }
    
    public static BarEffects getBarEffects(BarType barType) {
        return BAR_EFFECTS.get(barType);
    }
    
    

    protected void drawText(MatrixStack matrixStack, ITextComponent text, int x, int y, Alignment alignment, 
            int color, float partialTick, FontRenderer font, ActionsOverlayGui hud) {
        hud.drawBackdrop(matrixStack, x, y, font.width(text), alignment, null, 1.0F, partialTick);
        hud.drawString(matrixStack, font, text, x, y, alignment, color);
    }
}
