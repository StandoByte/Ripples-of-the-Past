package com.github.standobyte.jojo.port_bridge.minecraft;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class Tooltip {
    
    public static Button.ITooltip create(ITextComponent text) {
        return (Button button, MatrixStack matrixStack, int x, int y) -> {
            List<? extends ITextProperties> tooltips = Arrays.asList(text);
            Minecraft mc = Minecraft.getInstance();
            FontRenderer font = mc.font;
            MainWindow window = mc.getWindow();
            int width = window.getGuiScaledWidth();
            int height = window.getGuiScaledHeight();
            GuiUtils.drawHoveringText(matrixStack, tooltips, x, y, width, height, -1, font);
        };
    }
}
