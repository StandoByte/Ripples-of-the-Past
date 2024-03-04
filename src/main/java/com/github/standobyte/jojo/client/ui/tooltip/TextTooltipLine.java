package com.github.standobyte.jojo.client.ui.tooltip;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.Style;

public class TextTooltipLine implements ITooltipLine {
    private final ITextProperties text;
    
    public TextTooltipLine(ITextProperties textLine) {
        this.text = textLine;
    }
    
    @Override
    public void draw(MatrixStack matrixStack, float x, float y, FontRenderer font) {
        IRenderTypeBuffer.Impl renderType = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
        font.drawInBatch(LanguageMap.getInstance().getVisualOrder(text), x, y, -1, 
                true, matrixStack.last().pose(), renderType, false, 0, 0xF000F0);
        renderType.endBatch();
    }
    
    @Override
    public int getWidth(FontRenderer font) {
        return font.width(text);
    }

    @Override
    public int getHeight(FontRenderer font) {
        return font.lineHeight + 1;
    }
    
    @Override
    public List<ITooltipLine> split(int width, FontRenderer font, Style style) {
        return font.getSplitter().splitLines(text, width, style)
                .stream().map(TextTooltipLine::new).collect(Collectors.toList());
    }

    @Override
    public Stream<ITextProperties> getTextOnly() {
        return Stream.of(text);
    }

}
