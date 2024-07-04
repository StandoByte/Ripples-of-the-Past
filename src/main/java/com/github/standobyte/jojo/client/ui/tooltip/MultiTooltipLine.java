package com.github.standobyte.jojo.client.ui.tooltip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;

public class MultiTooltipLine implements ITooltipLine {
    private final List<ITooltipLine> lineParts;
    private final ITextProperties textOnly;

    public MultiTooltipLine(ITooltipLine... parts) {
        this.lineParts = new ArrayList<>();
        Collections.addAll(this.lineParts, parts);
        this.textOnly = StringTextComponent.EMPTY;
    }
    
    
    @Override
    public void draw(MatrixStack matrixStack, float x, float y, FontRenderer font) {
        for (ITooltipLine line : lineParts) {
            line.draw(matrixStack, x, y, font);
            x += line.getWidth(font) + 1;
        }
    }
    
    @Override
    public int getWidth(FontRenderer font) {
        return lineParts.stream().map(line -> line.getWidth(font) + 1).reduce(-1, Integer::sum);
    }

    @Override
    public int getHeight(FontRenderer font) {
        return lineParts.stream().map(line -> line.getHeight(font) + 1).max(Comparator.naturalOrder()).orElse(0);
    }
    
    @Override
    public List<ITooltipLine> split(int width, FontRenderer font, Style style) {
        return Collections.singletonList(this);
    }
    
    @Override
    public Stream<ITextProperties> getTextOnly() {
        return Stream.of(textOnly);
    }
}
