package com.github.standobyte.jojo.client.ui.screen.hamon;

import java.util.Collections;
import java.util.List;

import com.github.standobyte.jojo.JojoMod;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class HamonTabGui extends AbstractGui {
    protected static final ResourceLocation ADV_WIDGETS = new ResourceLocation("textures/gui/advancements/widgets.png");
    protected final Minecraft minecraft;
    protected final HamonScreen screen;
    protected final int tabIndex;
    private final ITextComponent title;
    protected final List<IReorderingProcessor> descLines;
    private final ResourceLocation background;
    
    protected double scrollX;
    protected double scrollY;
    protected int intScrollX;
    protected int intScrollY;
    protected int buttonY = -1;
    private final int minX = 0;
    private final int minY = 0;
    protected int maxX;
    protected int maxY;
    protected boolean leftUpperCorner;

    HamonTabGui(Minecraft minecraft, HamonScreen screen, int index, String title, int scrollWidth, int scrollHeight) {
        this.minecraft = minecraft;
        this.screen = screen;
        this.tabIndex = index;
        this.title = new TranslationTextComponent(title);
        this.descLines = minecraft.font.split(createTabDescription(title + ".desc"), 200);
        this.background = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/advancements/jojo.png");
        this.maxX = scrollWidth;
        this.maxY = scrollHeight;
    }
    
    protected ITextComponent createTabDescription(String key) {
        return new TranslationTextComponent(key);
    }

    ITextComponent getTitle() {
       return title;
    }

    void drawTab(MatrixStack matrixStack, int windowX, int windowY, boolean isSelected) {
        int textureX = tabIndex > 0 ? 32 : 0;
        int textureY = 64;
        textureY += (isSelected) ? 28 : 0;
        minecraft.getTextureManager().bind(HamonScreen.TABS);
        blit(matrixStack, windowX - 32 + 4, windowY + getTabY(tabIndex), textureX, textureY, 32, 28);
    }
    
    List<IReorderingProcessor> additionalTabNameTooltipInfo() {
        return Collections.emptyList();
    }

    boolean isMouseOnTabIcon(int windowX, int windowY, double mouseX, double mouseY) {
        int i = windowX - 32 + 4;
        int j = windowY + getTabY(tabIndex);
        return mouseX > (double)i && mouseX < (double)(i + 28) && mouseY > (double)j && mouseY < (double)(j + 32);
    }

    void drawIcon(MatrixStack matrixStack, int windowX, int windowY, ItemRenderer itemRenderer) {} // TODO tab icons
    
    protected int getTabY(int index) {
        return 28 * index;
    }
    
    void drawContents(MatrixStack matrixStack) {
        if (!leftUpperCorner) {
            scrollX = 0;
            scrollY = 0;
            leftUpperCorner = true;
        }
        RenderSystem.pushMatrix();
        RenderSystem.enableDepthTest();
        RenderSystem.translatef(0.0F, 0.0F, 950.0F);
        RenderSystem.colorMask(false, false, false, false);
        fill(matrixStack, 4680, 2260, -4680, -2260, -16777216);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.translatef(0.0F, 0.0F, -950.0F);
        RenderSystem.depthFunc(518);
        fill(matrixStack, HamonScreen.WINDOW_WIDTH - 18, HamonScreen.WINDOW_HEIGHT - 27, 0, 0, -16777216);
        RenderSystem.depthFunc(515);
        if (background != null)  {
            minecraft.getTextureManager().bind(background);
        } else {
            minecraft.getTextureManager().bind(TextureManager.INTENTIONAL_MISSING_TEXTURE);
        }

        intScrollX = MathHelper.floor(scrollX);
        intScrollY = MathHelper.floor(scrollY);
        int k = intScrollX % 16;
        int l = intScrollY % 16;
        for (int i1 = -1; i1 <= 13; ++i1) {
            for (int j1 = -1; j1 <= 13; ++j1) {
                blit(matrixStack, k + 16 * i1, l + 16 * j1, 0.0F, 0.0F, 16, 16, 16, 16);
            }
        }
        
        RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();
        drawText(matrixStack);
        RenderSystem.enableDepthTest();
        RenderSystem.enableRescaleNormal();
        drawActualContents(matrixStack);
        
        RenderSystem.depthFunc(518);
        RenderSystem.translatef(0.0F, 0.0F, -950.0F);
        RenderSystem.colorMask(false, false, false, false);
        fill(matrixStack, 4680, 2260, -4680, -2260, -16777216);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.translatef(0.0F, 0.0F, 950.0F);
        RenderSystem.depthFunc(515);
        RenderSystem.popMatrix();
    }
    
    abstract void addButtons();
    
    abstract List<Widget> getButtons();

    protected abstract void drawActualContents(MatrixStack matrixStack);

    protected abstract void drawText(MatrixStack matrixStack);
    
    protected void drawDesc(MatrixStack matrixStack) {
        for (int i = 0; i < descLines.size(); i++) {
            minecraft.font.draw(matrixStack, descLines.get(i), (float) scrollX + 6, (float) scrollY + 22 + i * 9, 0xFFFFFF);
        }
    }
    
    abstract void renderButtons(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick);
    
    abstract boolean mouseClicked(double mouseX, double mouseY, int mouseButton);
    
    abstract boolean mouseReleased(double mouseX, double mouseY, int mouseButton);

    void drawToolTips(MatrixStack matrixStack, int mouseX, int mouseY, int windowPosX, int windowPosY) {}

    void scroll(double xMovement, double yMovement) {
        if (maxX - minX > HamonScreen.WINDOW_WIDTH - 8) {
            scrollX = MathHelper.clamp(scrollX + xMovement, (double)(-(maxX - (HamonScreen.WINDOW_WIDTH - 18))), 0.0D);
        }
        if (maxY - minY > HamonScreen.WINDOW_HEIGHT - 8) {
            scrollY = MathHelper.clamp(scrollY + yMovement, (double)(-(maxY - (HamonScreen.WINDOW_HEIGHT - 27))), 0.0D);
        }
    }
    
    abstract void updateTab();
}
