package com.github.standobyte.jojo.client.ui.actionshud;

import java.util.function.Consumer;

import com.github.standobyte.jojo.power.layout.ActionHotbarLayout;

public class HotbarFold {
    private static final HotbarFold[] NO_FOLD = new HotbarFold[ActionHotbarLayout.ARBITRARY_ACTIONS_LIMIT];
    
    private final Slot[] slotsInRenderOrder;
    private final Slot[] slotsInIndexOrder;
    private final int slotsCount;
    
    public static HotbarFold makeHotbarFold(int slotsCount, int selectedSlot, float foldProgress) {
        if (foldProgress <= 0) {
            return createNoFold(slotsCount);
        }
        return new HotbarFold(slotsCount, selectedSlot, Math.min(foldProgress, 1));
    }
    
    private static HotbarFold createNoFold(int slotsCount) {
        if (NO_FOLD[slotsCount] == null) {
            NO_FOLD[slotsCount] = new HotbarFold(slotsCount, -1, 0);
        }
        return NO_FOLD[slotsCount];
    }
    
    private HotbarFold(int slotsCount, int selectedSlot, float foldProgress) {
        this.slotsCount = slotsCount;
        this.slotsInIndexOrder = new Slot[slotsCount];
        this.slotsInRenderOrder = new Slot[slotsCount];
        
        int[] order = new int[slotsCount];
        if (selectedSlot >= 0 && selectedSlot < slotsCount) {
            int i = 0;
            for (int slot = 0; slot < selectedSlot; slot++) {
                order[i++] = slot;
            }
            for (int slot = slotsCount - 1; slot > selectedSlot; slot--) {
                order[i++] = slot;
            }
            order[slotsCount - 1] = selectedSlot;
        }
        else {
            for (int i = 0; i < slotsCount; i++) {
                order[i] = i;
            }
        }
        
        float[] posArr = new float[slotsCount];
        for (int i = 0; i < slotsCount; i++) {
            float pos = i * 20 * (1 - foldProgress);
            posArr[i] = pos;
        }
        
        for (int i = 0; i < slotsCount; i++) {
            Slot slot = new Slot(i, posArr[i]);
            slotsInIndexOrder[i] = slot;
            slotsInRenderOrder[order[i]] = slot;
            
            slot.slotFramePosX = i == 0 ? slot.pos : slot.pos + 1;
            slot.slotTexX = i == 0 ? 0 : i == slotsCount - 1 ? 161 : i * 20 + 1;
            slot.slotWidth = i == 0 || i == slotsCount - 1 ? 21 : 20;
            
            slot.slotRenderedLeftEdge = slot.slotFramePosX;
            slot.slotRenderedWidth = slot.slotWidth;
        }
        
        if (foldProgress > 0) {
            Slot slot = slotsInRenderOrder[slotsCount - 1];
            float hotbarLeftEdge = slot.slotFramePosX;
            float hotbarRightEdge = slot.slotFramePosX + slot.slotRenderedWidth;
            for (int i = slotsCount - 2; i >= 0; i--) {
                slot = slotsInRenderOrder[i];
                float slotLeftEdge = slot.slotRenderedLeftEdge;
                float slotRightEdge = slot.slotRenderedLeftEdge + slot.slotRenderedWidth;
                float slotRenderedRightEdge;
                if (slotLeftEdge < hotbarLeftEdge && slotRightEdge >= hotbarLeftEdge) {
                    slot.slotRenderedLeftEdge = slotLeftEdge;
                    slotRenderedRightEdge = hotbarLeftEdge;
                }
                else if (slotLeftEdge <= hotbarRightEdge && slotRightEdge > hotbarRightEdge) {
                    slot.slotRenderedLeftEdge = hotbarRightEdge;
                    slotRenderedRightEdge = slotRightEdge;
                }
                else {
                    slot.slotRenderedLeftEdge = slotLeftEdge;
                    slotRenderedRightEdge = slotLeftEdge;
                }
                
                slot.slotRenderedWidth = slotRenderedRightEdge - slot.slotRenderedLeftEdge;
                
                hotbarLeftEdge = Math.min(hotbarLeftEdge, slot.slotFramePosX);
                hotbarRightEdge = Math.max(hotbarRightEdge, slot.slotFramePosX + slot.slotWidth);
            }
        }
    }
    
    public int slotsCount() {
        return slotsCount;
    }
    
    public void renderSlots(Consumer<Slot> render) {
        for (Slot slot : slotsInRenderOrder) {
            render.accept(slot);
        }
    }
    
    public Slot getSlotWithIndex(int i) {
        if (i < 0 || i >= slotsCount) {
            return null;
        }
        return slotsInIndexOrder[i];
    }
    
    public static class Slot {
        public final int slotIndex;
        public final float pos;

        private float slotTexX;
        private float slotFramePosX;
        private float slotWidth;
        
        private float slotRenderedLeftEdge;
        private float slotRenderedWidth;
        
        private Slot(int slotIndex, float pos) {
            this.slotIndex = slotIndex;
            this.pos = pos;
        }
        
        public float getStartRenderPos() {
            return pos;
        }
        
        public float getFrameRenderedTexX() {
            return slotTexX + slotRenderedLeftEdge - slotFramePosX;
        }
        
        public float getFrameRenderedLeftEdge() {
            return slotRenderedLeftEdge;
        }
        
        public float getFrameRenderedWidth() {
            return slotRenderedWidth;
        }
    }
}
