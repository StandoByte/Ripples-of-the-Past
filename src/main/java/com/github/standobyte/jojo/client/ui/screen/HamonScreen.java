package com.github.standobyte.jojo.client.ui.screen;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonWindowOpenedPacket;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;

public class HamonScreen extends Screen {
    static final int WINDOW_WIDTH = 230;
    static final int WINDOW_HEIGHT = 228;
    
    static final int WINDOW_THIN_BORDER = 9;
    static final int WINDOW_UPPER_BORDER = 18;
    
    public static final ResourceLocation WINDOW = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/hamon_window.png");
    static final ResourceLocation TABS = new ResourceLocation("textures/gui/advancements/tabs.png");
    
    private HamonTabGui[] tabs;
    private HamonTabGui selectedTab;
    boolean isTeacherNearby = false;
    Set<HamonSkill> teacherSkills = EnumSet.noneOf(HamonSkill.class);
    
    HamonData hamon;
    
    boolean clickedOnSkill;

    public HamonScreen() {
        super(NarratorChatListener.NO_TITLE);
    }
    
    @Override
    protected void init() {
        hamon = INonStandPower.getPlayerNonStandPower(minecraft.player)
                .getTypeSpecificData(ModNonStandPowers.HAMON.get()).get();
        int i = 0;
        tabs = new HamonTabGui[] {
                new HamonStatsTabGui(minecraft, this, i++, "hamon.stats.tab"),
                new HamonGeneralSkillsTabGui(minecraft, this, i++, "hamon.strength_skills.tab", HamonSkill.HamonStat.STRENGTH),
                new HamonGeneralSkillsTabGui(minecraft, this, i++, "hamon.control_skills.tab", HamonSkill.HamonStat.CONTROL),
                new HamonTechniqueTabGui(minecraft, this, i++, "hamon.techniques.tab")
        };
        for (HamonTabGui tab : tabs) {
            tab.addButtons();
        }
        selectTab(tabs[0]);
        PacketManager.sendToServer(new ClHamonWindowOpenedPacket());
    }
    
    @Override
    public <T extends Widget> T addButton(T button) {
        return super.addButton(button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        clickedOnSkill = false;
        if (super.mouseClicked(mouseX, mouseY, mouseButton)) {
            setDragging(false);
            return true;
        }
        setDragging(false);
        int x = windowPosX();
        int y = windowPosY();
        for (HamonTabGui hamonTabGui : tabs) {
            if (hamonTabGui.isMouseOnTabIcon(x, y, mouseX, mouseY)) {
                selectTab(hamonTabGui);
                return true;
            }
        }
        if (selectedTab != null && mouseX > x + WINDOW_THIN_BORDER && mouseX < x + WINDOW_WIDTH - WINDOW_THIN_BORDER 
                && mouseY > y + WINDOW_UPPER_BORDER && mouseY < y + WINDOW_HEIGHT - WINDOW_THIN_BORDER) {
            if (selectedTab.mouseClicked(mouseX - x - WINDOW_THIN_BORDER, mouseY - y - WINDOW_UPPER_BORDER, mouseButton)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        boolean dragging = isDragging();
        if (super.mouseReleased(mouseX, mouseY, mouseButton)) {
            return true;
        }
        setDragging(dragging);
        int x = windowPosX();
        int y = windowPosY();
        if (selectedTab != null && mouseX > x + WINDOW_THIN_BORDER && mouseX < x + WINDOW_WIDTH - WINDOW_THIN_BORDER 
                && mouseY > y + WINDOW_UPPER_BORDER && mouseY < y + WINDOW_HEIGHT - WINDOW_THIN_BORDER) {
            if (selectedTab.mouseReleased(mouseX - x - WINDOW_THIN_BORDER, mouseY - y - WINDOW_UPPER_BORDER, mouseButton)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double xPos, double yPos, int mouseButton, double xMovement, double yMovement) {
        if (xMovement != 0 || yMovement != 0) {
            setDragging(true);
        }
        if (selectedTab != null) {
            selectedTab.scroll(xMovement, yMovement);
            return true;
        }
        return super.mouseDragged(xPos, yPos, mouseButton, xMovement, yMovement);
    }
    
    private void selectTab(HamonTabGui tab) {
        selectedTab = tab;
        for (HamonTabGui hamonTabGui : tabs) {
            hamonTabGui.getButtons().forEach(button -> button.active = hamonTabGui == tab);
        }
        tab.updateTab();
    }
    
    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (InputHandler.getInstance().hamonSkillsWindow.matches(key, scanCode)) {
            minecraft.setScreen(null);
            minecraft.mouseHandler.grabMouse();
            return true;
        } else {
            return super.keyPressed(key, scanCode, modifiers);
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        int x = windowPosX();
        int y = windowPosY();
        renderBackground(matrixStack);
        renderInside(matrixStack, mouseX, mouseY, x, y);
        renderWindow(matrixStack, x, y);
        renderToolTips(matrixStack, mouseX, mouseY, x, y);
        if (selectedTab != null) {
            selectedTab.renderButtons(matrixStack, mouseX, mouseY, partialTick);
        }
    }
    
    int windowPosX() {
        return (width - WINDOW_WIDTH) / 2;
    }
    
    int windowPosY() {
        return (height - WINDOW_HEIGHT) / 2;
    }

    private void renderInside(MatrixStack matrixStack, int mouseX, int mouseY, int windowX, int windowY) {
        if (selectedTab != null) {
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float)(windowX + WINDOW_THIN_BORDER), (float)(windowY + WINDOW_UPPER_BORDER), 0.0F);
            selectedTab.drawContents(matrixStack);
            RenderSystem.popMatrix();
            RenderSystem.depthFunc(515);
            RenderSystem.disableDepthTest();
        }
    }

    public void renderWindow(MatrixStack matrixStack, int windowX, int windowY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        minecraft.getTextureManager().bind(WINDOW);
        blit(matrixStack, windowX, windowY, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        for (HamonTabGui tabGui : tabs) {
            tabGui.drawTab(matrixStack, windowX, windowY, tabGui == selectedTab);
        }
        RenderSystem.enableRescaleNormal();
        RenderSystem.defaultBlendFunc();
        for (HamonTabGui tabGui : tabs) {
            tabGui.drawIcon(matrixStack, windowX, windowY, itemRenderer);
        }
        RenderSystem.disableBlend();
        if (selectedTab != null) {
            font.draw(matrixStack, selectedTab.getTitle(), windowX + 8, windowY + 6, 0x404040);
        }
    }

    private void renderToolTips(MatrixStack matrixStack, int mouseX, int mouseY, int windowX, int windowY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (selectedTab != null) {
            RenderSystem.pushMatrix();
            RenderSystem.enableDepthTest();
            RenderSystem.translatef((float)(windowX + WINDOW_THIN_BORDER), (float)(windowY + WINDOW_UPPER_BORDER), 400.0F);
            selectedTab.drawToolTips(matrixStack, mouseX - windowX - WINDOW_THIN_BORDER, mouseY - windowY - WINDOW_UPPER_BORDER, windowX, windowY);
            RenderSystem.disableDepthTest();
            RenderSystem.popMatrix();
        }
        for (HamonTabGui hamonTabGui : tabs) {
            if (hamonTabGui.isMouseOnTabIcon(windowX, windowY, (double)mouseX, (double)mouseY)) {
                List<IReorderingProcessor> tooltipLines = new ArrayList<IReorderingProcessor>();
                tooltipLines.add(hamonTabGui.getTitle().getVisualOrderText());
                tooltipLines.addAll(hamonTabGui.additionalTabNameTooltipInfo());
                renderTooltip(matrixStack, tooltipLines, mouseX, mouseY);
                break;
            }
        }
    }

    public static void setTeacherSkills(Set<HamonSkill> skills) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof HamonScreen) {
            HamonScreen hamonScreen = ((HamonScreen) screen);
            hamonScreen.isTeacherNearby = !skills.isEmpty();
            hamonScreen.teacherSkills = skills;
        }
    }
    
    public static void updateTabs() {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof HamonScreen) {
            HamonScreen hamonScreen = ((HamonScreen) screen);
            for (HamonTabGui tab : hamonScreen.tabs) {
                tab.updateTab();
            }
        }
    }
}
