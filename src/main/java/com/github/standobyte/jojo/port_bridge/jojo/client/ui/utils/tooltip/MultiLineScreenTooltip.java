package com.github.standobyte.jojo.port_bridge.jojo.client.ui.utils.tooltip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.Language;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class MultiLineScreenTooltip {
    public List<ITextProperties> cachedTooltip;
    
    @Nullable
    public Language splitWithLanguage;

    public MultiLineScreenTooltip(ITextComponent title, ITextComponent... body) {
        //super(title, title);
        this.cachedTooltip = new ArrayList<>();
        this.cachedTooltip.add(title);
        Collections.addAll(this.cachedTooltip, body);
    }
    
    public Button.ITooltip bred() {
        return (Button button, MatrixStack matrixStack, int x, int y) -> {
            List<? extends ITextProperties> tooltips = this.cachedTooltip;
            Minecraft mc = Minecraft.getInstance();
            FontRenderer font = mc.font;
            MainWindow window = mc.getWindow();
            int width = window.getGuiScaledWidth();
            int height = window.getGuiScaledHeight();
            GuiUtils.drawHoveringText(matrixStack, tooltips, x, y, width, height, -1, font);
        };
    }
    
}
