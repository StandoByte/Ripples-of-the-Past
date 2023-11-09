package com.github.standobyte.jojo.client.ui.screen.stand.ge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.GoldExperienceChooseLifeform;
import com.github.standobyte.jojo.client.ClientUtil;
import com.google.common.collect.Streams;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.entity.EntityType;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class FilterList {
    private static final int CHECKBOX_HEIGHT = 20;
    private static final int CHECKBOX_WIDTH = 20;
    private int topEntry = 0;
    private final List<Entry> allEntries;
    private List<Entry> renderedEntries;
    public boolean visible = false;
    
    @Nullable private Predicate<Entry> filter;
    
    private int x;
    private int y;
    private int yTop;
    private int yBottom;
    private int width;
    private int height;
    private int maxHeight;
    private int maxEntriesRendered;
    private int entriesRenderedCount;
    
    public FilterList(List<EntityType<?>> entityTypes, 
            int xRight, int yTop, int width, int yBottom, ChooseLifeformScreen screen) {
        this.x = xRight - width;
        this.yTop = yTop;
        this.yBottom = yBottom;
        this.width = width;
        this.allEntries = Streams.mapWithIndex(
                entityTypes.stream(), 
                (entityType, i) -> new Entry(entityType, 
                        new LifeformFilterListCheckbox(xRight - CHECKBOX_WIDTH, -1, CHECKBOX_WIDTH, CHECKBOX_HEIGHT, 
                                entityType.getDescription(), 
                                () -> !GoldExperienceChooseLifeform.hiddenEntriesTmp.contains(entityType),
                                stateBeingSet -> {
                                    if (stateBeingSet) {
                                        screen.showEntry(entityType);
                                    }
                                    else {
                                        screen.hideEntry(entityType);
                                    }
                                })))
                .collect(Collectors.toCollection(ArrayList::new));
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
        this.maxEntriesRendered = Math.max(maxHeight / CHECKBOX_HEIGHT, 1);
        this.entriesRenderedCount = Math.min(renderedEntries.size(), maxEntriesRendered);
        this.height = entriesRenderedCount * CHECKBOX_HEIGHT + 32;
        this.y = yBottom - height;
        updatePositions();
    }
    
    private void updatePositions() {
        int i = -topEntry;
        for (Entry entry : renderedEntries) {
            entry.checkbox.y = this.y + 16 + (int) i * CHECKBOX_HEIGHT;
            i++;
        }
        setTopEntryIndex(topEntry);
    }
    
    public void render(MatrixStack matrixStack, Minecraft mc, 
            int mouseX, int mouseY, float partialTick) {
        if (!visible) return;
        
        mc.textureManager.bind(ChooseLifeformScreen.LIFEFORM_CHOOSE_LOCATION);
        AbstractGui.blit(matrixStack, x + width / 2, y, 
                16, getScrollUpState(mouseX, mouseY).texY, 16, 16, 128, 128);
        
        for (int i = 0; i < entriesRenderedCount && i + topEntry < renderedEntries.size(); i++) {
            Entry entry = renderedEntries.get(i + topEntry);
            ClientUtil.drawRightAlignedString(matrixStack, mc.font, 
                    entry.checkbox.getMessage(), 
                    x + width - 25, entry.checkbox.y + (CHECKBOX_HEIGHT - mc.font.lineHeight) / 2, 
                    0xFFFFFF);
            entry.checkbox.render(matrixStack, mouseX, mouseY, partialTick);
        }

        mc.textureManager.bind(ChooseLifeformScreen.LIFEFORM_CHOOSE_LOCATION);
        AbstractGui.blit(matrixStack, x + width / 2, this.y + height - 16, 
                0, getScrollDownState(mouseX, mouseY).texY, 16, 16, 128, 128);
    }
    
    private int getMaxTopEntryIndex() {
        return Math.max(renderedEntries.size() - maxEntriesRendered, 0);
    }
    
    
    public void setFilter(@Nullable Predicate<Entry> filter) {
        this.filter = filter; // TODO fix filter
        this.renderedEntries = new ArrayList<>();
        for (Entry entry : allEntries) {
            boolean visible = filter == null || filter.test(entry);
            if (visible) {
                renderedEntries.add(entry);
            }
            entry.checkbox.visible = visible;
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
        NORMAL(80),
        DISABLED(96),
        HOVERED(112);
        
        private final int texY;
        private ScrollButtonState(int texY) {
            this.texY = texY;
        }
    }
    
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
            CheckboxButton checkbox = renderedEntries.get(i + topEntry).checkbox;
            if (checkbox.mouseClicked(mouseX, mouseY, buttonId)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!visible) return false;
        
        if (mouseOver(mouseX, mouseY)) {
            if (delta < 0) {
                setTopEntryIndex(topEntry + 1);
            }
            else if (delta > 0) {
                setTopEntryIndex(topEntry - 1);
            }
            return true;
        }
        
        return false;
    }
    
    private boolean mouseOver(double mouseX, double mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }
    
    private void setTopEntryIndex(int index) {
        index = MathHelper.clamp(index, 0, getMaxTopEntryIndex());
        if (this.topEntry != index) {
            int diff = index - this.topEntry;
            this.topEntry = index;
            renderedEntries.forEach(entry -> entry.checkbox.y -= diff * CHECKBOX_HEIGHT);
        }
    }
    
    
    public static class Entry {
        public final EntityType<?> entityType;
        private final CheckboxButton checkbox;
        
        private Entry(EntityType<?> entityType, CheckboxButton checkbox) {
            this.entityType = entityType;
            this.checkbox = checkbox;
        }
    }
}
