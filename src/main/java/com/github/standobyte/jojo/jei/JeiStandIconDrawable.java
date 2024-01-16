package com.github.standobyte.jojo.jei;

import java.util.List;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.mojang.blaze3d.matrix.MatrixStack;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.gui.elements.DrawableResource;
import mezz.jei.gui.ingredients.CycleTimer;

public class JeiStandIconDrawable implements IDrawable {
    private final CycleTimer iconsCycle = new CycleTimer(0);
    private final List<IDrawable> standIcons;
    
    public JeiStandIconDrawable(List<StandType<?>> standIcons) {
        this.standIcons = standIcons.stream()
                .map(stand -> new DrawableResource(stand.getIconTexture(null), 
                        0, 0, 16, 16, 0, 0, 0, 0, 16, 16))
                .collect(Collectors.toList());
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public void draw(MatrixStack matrixStack, int xOffset, int yOffset) {
        if (!standIcons.isEmpty()) {
            IDrawable standIcon = iconsCycle.getCycledItem(standIcons);
            standIcon.draw(matrixStack, xOffset, yOffset);
            iconsCycle.onDraw();
        }
    }

}
