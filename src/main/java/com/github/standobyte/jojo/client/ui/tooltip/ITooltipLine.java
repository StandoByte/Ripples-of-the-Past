package com.github.standobyte.jojo.client.ui.tooltip;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.Style;

public interface ITooltipLine {
    void draw(MatrixStack matrixStack, float x, float y, FontRenderer font);
    int getWidth(FontRenderer font);
    int getHeight(FontRenderer font);
    List<ITooltipLine> split(int width, FontRenderer font, Style style);
    Stream<ITextProperties> getTextOnly();
}
