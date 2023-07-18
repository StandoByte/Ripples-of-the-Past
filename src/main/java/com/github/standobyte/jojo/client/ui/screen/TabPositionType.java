package com.github.standobyte.jojo.client.ui.screen;

import java.util.Arrays;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.AbstractGui;

public enum TabPositionType {
    ABOVE(0, 0, 28, 32, 8),
    BELOW(84, 0, 28, 32, 8),
    LEFT(0, 64, 32, 28, 5),
    RIGHT(96, 64, 32, 28, 5);

    public static final int MAX_TABS = Arrays.stream(values()).mapToInt(e -> e.max).sum();
    private final int textureX;
    private final int textureY;
    private final int width;
    private final int height;
    private final int max;

    private TabPositionType(int texX, int texY, int width, int height, int max) {
        this.textureX = texX;
        this.textureY = texY;
        this.width = width;
        this.height = height;
        this.max = max;
    }

    public int getMax() {
        return max;
    }

    public void draw(MatrixStack matrixStack, AbstractGui gui, int offsetX, int offsetY, boolean isSelected, int index) {
        int i = textureX;
        if (index > 0) {
            i += width;
        }

        if (index == max - 1) {
            i += width;
        }

        int j = isSelected ? textureY + height : textureY;
        gui.blit(matrixStack, offsetX + getX(index), offsetY + getY(index), i, j, width, height);
    }

    public int getX(int index) {
        switch(this) {
        case ABOVE:
            return (width + 4) * index;
        case BELOW:
            return (width + 4) * index;
        case LEFT:
            return -width + 4;
        case RIGHT:
            return 248;
        default:
            throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
        }
    }

    public int getY(int yndex) {
        switch(this) {
        case ABOVE:
            return -height + 4;
        case BELOW:
            return 136;
        case LEFT:
            return height * yndex;
        case RIGHT:
            return height * yndex;
        default:
            throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
        }
    }

    public int getIconX(int offsetX, int index) {
        int x = offsetX + getX(index);
        switch(this) {
        case ABOVE:
            x += 6;
            break;
        case BELOW:
            x += 6;
            break;
        case LEFT:
            x += 10;
            break;
        case RIGHT:
            x += 6;
        }
        return x;
    }
    
    public int getIconY(int offsetY, int index) {
        int y = offsetY + getY(index);
        switch(this) {
        case ABOVE:
            y += 9;
            break;
        case BELOW:
            y += 6;
            break;
        case LEFT:
            y += 5;
            break;
        case RIGHT:
            y += 5;
        }
        return y;
    }

    public boolean isMouseOver(int offsetX, int offsetY, int index, double mouseX, double mouseY) {
        int i = offsetX + getX(index);
        int j = offsetY + getY(index);
        return mouseX > i && mouseX < i + width && mouseY > j && mouseY < j + height;
    }
}
