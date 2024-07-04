package com.github.standobyte.jojo.client.ui.tooltip;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.github.standobyte.jojo.client.ClientUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.Style;

public class IconTooltipLine implements ITooltipLine {
    private final Icon icon;
    private final int count;
    
    public IconTooltipLine(Icon icon) {
        this(icon, 1);
    }
    
    public IconTooltipLine(Icon icon, int count) {
        this.icon = icon;
        this.count = count;
    }
    
    
    @Override
    public void draw(MatrixStack matrixStack, float x, float y, FontRenderer font) {
        Minecraft.getInstance().textureManager.bind(ClientUtil.ADDITIONAL_UI);
        for (int i = 0; i < count; i++) {
            RenderSystem.enableBlend();
            AbstractGui.blit(matrixStack, (int) x, (int) y, 247 - icon.ordinal() * 9, 247, 9, 9, 256, 256);
            x += 8;
        }
    }
    
    @Override
    public int getWidth(FontRenderer font) {
        return count * 8 + 1;
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
        TIME
    }

}
