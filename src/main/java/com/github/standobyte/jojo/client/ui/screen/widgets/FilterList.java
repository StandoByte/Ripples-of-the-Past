package com.github.standobyte.jojo.client.ui.screen.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class FilterList<T extends FilterList.Entry> implements IGuiEventListener {
    private int topEntry = 0;
    private final List<T> allEntries;
    private List<T> renderedEntries;
    public boolean visible = false;
    
    @Nullable private Predicate<T> filter;
    
    private int x;
    private int y;
    private int yTop;
    private int yBottom;
    private int width;
    private int height;
    private int maxHeight;
    private int maxEntriesRendered;
    private int entriesRenderedCount;
    private final int entryHeight;
    
    public FilterList(List<T> entries, 
            int x, int yTop, int width, int yBottom, int entryHeight) {
        this.x = x;
        this.yTop = yTop;
        this.yBottom = yBottom;
        this.width = width;
        this.entryHeight = entryHeight;
        this.allEntries = entries;
        this.renderedEntries = new ArrayList<>(allEntries);
        updateMaxHeight();
    }
    
    public void setYBottom(int yBottom) {
        this.yBottom = yBottom;
        updateMaxHeight();
    }
    
    public int getYBottom() {
        return yBottom;
    }
    
    private void updateMaxHeight() {
        this.maxHeight = yBottom - yTop;
        this.maxEntriesRendered = Math.max(maxHeight / entryHeight, 1);
        this.entriesRenderedCount = Math.min(renderedEntries.size(), maxEntriesRendered);
        this.height = entriesRenderedCount * entryHeight + 32;
        this.y = yBottom - height;
        updatePositions();
    }
    
    private void updatePositions() {
        int i = -topEntry;
        for (T entry : renderedEntries) {
            entry.setY(this.y + 16 + (int) i * entryHeight);
            i++;
        }
        setTopEntryIndex(topEntry);
    }
    
    public void render(MatrixStack matrixStack, Minecraft mc, 
            int mouseX, int mouseY, float partialTick) {
        if (!visible) return;
        
        mc.textureManager.bind(ClientUtil.ADDITIONAL_UI);
        AbstractGui.blit(matrixStack, x + width / 2, y, 
                16, getScrollUpState(mouseX, mouseY).texY, 16, 16, 256, 256);
        
        for (int i = 0; i < entriesRenderedCount && i + topEntry < renderedEntries.size(); i++) {
            T entry = renderedEntries.get(i + topEntry);
            entry.render(matrixStack, mc, mouseX, mouseY, partialTick);
        }

        mc.textureManager.bind(ClientUtil.ADDITIONAL_UI);
        AbstractGui.blit(matrixStack, x + width / 2, this.y + height - 16, 
                0, getScrollDownState(mouseX, mouseY).texY, 16, 16, 256, 256);
    }
    
    private int getMaxTopEntryIndex() {
        return Math.max(renderedEntries.size() - maxEntriesRendered, 0);
    }
    
    
    public void setFilter(@Nullable Predicate<T> filter) {
        this.filter = filter; // TODO fix filter
        this.renderedEntries = new ArrayList<>();
        for (T entry : allEntries) {
            boolean visible = filter == null || filter.test(entry);
            if (visible) {
                renderedEntries.add(entry);
            }
            entry.setVisible(visible);
        }
        
        updateMaxHeight();
    }
    
    
    private ScrollButtonState getScrollButtonState(int mouseX, int mouseY, int x, int y, boolean isEnabled) {
        if (!isEnabled) {
            return ScrollButtonState.DISABLED;
        }
        boolean isHovered = mouseX > x + 3 && mouseX < x + 14 && mouseY > y + 4 && mouseY < y + 12;
        return isHovered ? ScrollButtonState.HOVERED : ScrollButtonState.NORMAL;
    }
    
    private ScrollButtonState getScrollUpState(int mouseX, int mouseY) { // FIXME make these separate buttons
        return getScrollButtonState(mouseX, mouseY, x + width / 2, y, topEntry > 0);
    }
    
    private ScrollButtonState getScrollDownState(int mouseX, int mouseY) {
        return getScrollButtonState(mouseX, mouseY, x + width / 2, y + height - 16, topEntry < getMaxTopEntryIndex());
    }
    
    private static enum ScrollButtonState {
        NORMAL(144),
        DISABLED(160),
        HOVERED(176);
        
        private final int texY;
        private ScrollButtonState(int texY) {
            this.texY = texY;
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonId) {
        if (!visible) return false;
        if (getScrollUpState((int) mouseX, (int) mouseY) == ScrollButtonState.HOVERED) {
            setTopEntryIndex(topEntry - 1);
            Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        if (getScrollDownState((int) mouseX, (int) mouseY) == ScrollButtonState.HOVERED) {
            setTopEntryIndex(topEntry + 1);
            Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        
        for (int i = 0; i < entriesRenderedCount; i++) {
            if (renderedEntries.get(i + topEntry).mouseClicked(mouseX, mouseY, buttonId)) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!visible) return false;
        
        if (delta < 0) {
            setTopEntryIndex(topEntry + 1);
        }
        else if (delta > 0) {
            setTopEntryIndex(topEntry - 1);
        }
        return true;
    }
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }
    
    private void setTopEntryIndex(int index) {
        index = MathHelper.clamp(index, 0, getMaxTopEntryIndex());
        if (this.topEntry != index) {
            int diff = index - this.topEntry;
            this.topEntry = index;
            renderedEntries.forEach(entry -> entry.addY(-diff * entryHeight));
        }
    }
    
    
    public static interface Entry {
        void setY(int y);
        void addY(int addY);
        void setVisible(boolean isVisible);
        void render(MatrixStack matrixStack, Minecraft mc, int mouseX, int mouseY, float partialTick);
        boolean mouseClicked(double mouseX, double mouseY, int buttonId);
    }
}
