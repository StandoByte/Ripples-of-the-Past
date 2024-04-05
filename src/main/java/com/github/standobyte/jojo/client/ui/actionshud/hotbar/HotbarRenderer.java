package com.github.standobyte.jojo.client.ui.actionshud.hotbar;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ui.BlitFloat;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

@SuppressWarnings("deprecation")
public class HotbarRenderer {
    public static final ResourceLocation HOTBAR_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/overlay_hotbar.png");
    
    public static final int EDGE_EXTRA_WIDTH = 15;
    
    public static void renderHotbar(MatrixStack matrixStack, Minecraft mc, int x, int y, int slots, float alpha) {
        if (slots <= 0) return;
        mc.getTextureManager().bind(HOTBAR_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        
        HotbarTexPosition texPos = HotbarTexPosition.getHotbarFromSlotsCount(slots);
        AbstractGui.blit(matrixStack, x - 14, y - 14, texPos.texX, texPos.texY, slots * 20 + 30, 50, 512, 512);
        
    }
    
    public static void renderHotbar(MatrixStack matrixStack, Minecraft mc, float x, float y, 
            int slotsCount, float alpha) {
        mc.getTextureManager().bind(HOTBAR_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        HotbarTexPosition texPos = HotbarTexPosition.getHotbarFromSlotsCount(slotsCount);
        AbstractGui.blit(matrixStack, 
                (int) x - 14, (int) y - 14, 
                texPos.texX, texPos.texY, 
                20 * slotsCount + EDGE_EXTRA_WIDTH * 2, 50, 
                512, 512);
    }
    
    public static void renderFoldingHotbar(MatrixStack matrixStack, Minecraft mc, float x, float y, HotbarFold hotbarFold, float alpha) {
        int slotsCount = hotbarFold.getSlotsCount();
        if (slotsCount <= 0) return;
        mc.getTextureManager().bind(HOTBAR_LOCATION);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
        
        HotbarTexPosition texPos = HotbarTexPosition.getHotbarFromSlotsCount(slotsCount);
        hotbarFold.renderSlots(slot -> {
            float width = slot.getFrameRenderedWidth();
            if (width > 0) {
                BlitFloat.blitFloat(matrixStack, 
                        x + slot.getFrameRenderedLeftEdge() - EDGE_EXTRA_WIDTH + 1, y - EDGE_EXTRA_WIDTH + 1, 
                        texPos.texX + slot.getFrameRenderedTexX(), texPos.texY, 
                        width, 50, 
                        512, 512);
            }
        });
    }
    
    private static enum HotbarTexPosition {
        _1(390, 50),
        _2(370, 100),
        _3(350, 150),
        _4(330, 200),
        _5(310, 250),
        _6(290, 300),
        _7(270, 350),
        _8(250, 400),
        _9(230, 450),
        _10(0, 450),
        _11(0, 400),
        _12(0, 350),
        _13(0, 300),
        _14(0, 250),
        _15(0, 200),
        _16(0, 150),
        _17(0, 100),
        _18(0, 50),
        _19(0, 0);
        
        private final int texX;
        private final int texY;
        
        private HotbarTexPosition(int texX, int texY) {
            this.texX = texX;
            this.texY = texY;
        }
        
        static HotbarTexPosition getHotbarFromSlotsCount(int slots) {
            HotbarTexPosition[] values = values();
            return values[MathHelper.clamp(slots - 1, 0, values.length - 1)];
        }
    }
    
    public static void renderSlotSelection(MatrixStack matrixStack, Minecraft mc, float slotX, float slotY, float hotbarAlpha, boolean greenSelection) {
        mc.getTextureManager().bind(HOTBAR_LOCATION);
        if (greenSelection) {
            RenderSystem.color4f(0.0F, 1.0F, 0.0F, hotbarAlpha);
        }
        else {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, hotbarAlpha);
        }
        BlitFloat.blitFloat(matrixStack, slotX - 28, slotY - 28, 440, 0, 72, 72, 512, 512);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, hotbarAlpha);
    }
}
