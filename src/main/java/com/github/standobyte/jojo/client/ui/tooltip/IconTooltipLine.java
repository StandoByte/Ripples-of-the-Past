package com.github.standobyte.jojo.client.ui.tooltip;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.actionshud.BarsRenderer;
import com.github.standobyte.jojo.client.ui.actionshud.BarsRenderer.BarType;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.Style;

public class IconTooltipLine implements ITooltipLine {
    private Icon icon;
    private NonStandPowerType<?> nonStandPowerType;
    private int count;
    private int rightSideSpace = 0;
    
    public IconTooltipLine(Icon icon) {
        this(icon, 1);
    }
    
    public IconTooltipLine(Icon icon, int count) {
        this.icon = icon;
        this.count = count;
    }
    
    public static IconTooltipLine powerEnergy(NonStandPowerType<?> powerType) {
        IconTooltipLine icon = new IconTooltipLine(Icon.NON_STAND_ENERGY);
        icon.nonStandPowerType = powerType;
        return icon;
    }
    
    public IconTooltipLine withRightSideSpace(int px) {
        this.rightSideSpace = px;
        return this;
    }
    
    
    @Override
    public void draw(MatrixStack matrixStack, float x, float y, FontRenderer font) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int[] iconTex;
        switch (icon) {
        case NON_STAND_ENERGY:
            if (nonStandPowerType != null) {
                Minecraft.getInstance().textureManager.bind(ActionsOverlayGui.OVERLAY_LOCATION);
                iconTex = BarsRenderer.getIconTex(BarsRenderer.getEnergyBarIcon(nonStandPowerType), ActionsOverlayGui.BarsOrientation.HORIZONTAL);
                if (nonStandPowerType == ModPowers.VAMPIRISM.get()) {
                    iconTex[5] += 3;
                    iconTex[6] += 3;
                    iconTex[4] = 2;
                }
                AbstractGui.blit(matrixStack, (int) x + iconTex[5] - 2, (int) y + iconTex[6], 
                        iconTex[0] / iconTex[4], iconTex[1] / iconTex[4], iconTex[2] / iconTex[4], iconTex[3] / iconTex[4], 256 / iconTex[4], 256 / iconTex[4]);
            }
            break;
        case STAND_STAMINA:
            Minecraft.getInstance().textureManager.bind(ActionsOverlayGui.OVERLAY_LOCATION);
            iconTex = BarsRenderer.getIconTex(BarType.STAMINA, ActionsOverlayGui.BarsOrientation.HORIZONTAL);
            iconTex[4] = 2;
            AbstractGui.blit(matrixStack, (int) x + 1, (int) y - 1, 
                    iconTex[0] / iconTex[4], iconTex[1] / iconTex[4], iconTex[2] / iconTex[4], iconTex[3] / iconTex[4], 256 / iconTex[4], 256 / iconTex[4]);
            break;
        case STAND_RESOLVE:
            Minecraft.getInstance().textureManager.bind(ActionsOverlayGui.OVERLAY_LOCATION);
            iconTex = BarsRenderer.getIconTex(BarType.RESOLVE, ActionsOverlayGui.BarsOrientation.HORIZONTAL);
            AbstractGui.blit(matrixStack, (int) x, (int) y, 
                    iconTex[0]           / iconTex[4], iconTex[1] / iconTex[4], iconTex[2], iconTex[3], 256 / iconTex[4], 256 / iconTex[4]);
            AbstractGui.blit(matrixStack, (int) x, (int) y, 
                    (iconTex[0] + 40) / iconTex[4], iconTex[1] / iconTex[4], iconTex[2], iconTex[3], 256 / iconTex[4], 256 / iconTex[4]);
            break;
        default:
            Minecraft.getInstance().textureManager.bind(ClientUtil.ADDITIONAL_UI);
            for (int i = 0; i < count; i++) {
                AbstractGui.blit(matrixStack, (int) x, (int) y, 247 - icon.ordinal() * 9, 247, 9, 9, 256, 256);
                x += 8;
            }
            break;
        }
    }
    
    @Override
    public int getWidth(FontRenderer font) {
        switch (icon) {
        case NON_STAND_ENERGY:
            return 9 + rightSideSpace;
        case STAND_STAMINA:
            return 9 + rightSideSpace;
        case STAND_RESOLVE:
            return 15 + rightSideSpace;
        default:
            return count * 8 + 1 + rightSideSpace;
        }
    }

    @Override
    public int getHeight(FontRenderer font) {
        return 10;
    }
    
    @Override
    public List<ITooltipLine> split(int width, FontRenderer font, Style style) {
        return Collections.singletonList(this);
    }
    
    @Override
    public Stream<ITextProperties> getTextOnly() {
        return Stream.empty();
    }
    
    
    public enum Icon {
        HEALTH,
        ARMOR,
        STRENGTH,
        VOLUME,
        TIME,
        
        NON_STAND_ENERGY,
        STAND_STAMINA,
        STAND_RESOLVE
    }

}
