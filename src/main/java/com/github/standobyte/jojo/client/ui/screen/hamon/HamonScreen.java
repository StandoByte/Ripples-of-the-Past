package com.github.standobyte.jojo.client.ui.screen.hamon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonWindowOpenedPacket;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonSkill;
import com.github.standobyte.jojo.power.nonstand.type.hamon.HamonSkill.HamonStat;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings("deprecation")
public class HamonScreen extends Screen {
    static final int WINDOW_WIDTH = 230;
    static final int WINDOW_HEIGHT = 228;
    
    static final int WINDOW_THIN_BORDER = 9;
    static final int WINDOW_UPPER_BORDER = 18;
    
    public static final ResourceLocation WINDOW = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/hamon_window.png");
    static final ResourceLocation TABS = new ResourceLocation("textures/gui/advancements/tabs.png");
    
    HamonTabGui[] selectableTabs;
    HamonAbandonTabGui abandonTrainingTab;
    HamonTabGui selectedTab;
    private Set<HamonTabGui> tabsWithSkillRequirements = new HashSet<>();
    boolean isTeacherNearby = false;
    @Nullable Collection<HamonSkill> teacherSkills = Collections.emptyList();
    
    HamonData hamon;
    
    boolean clickedOnSkill;

    public HamonScreen() {
        super(NarratorChatListener.NO_TITLE);
    }
    
    @Override
    protected void init() {
        hamon = INonStandPower.getPlayerNonStandPower(minecraft.player)
                .getTypeSpecificData(ModPowers.HAMON.get()).get();
        int i = 0;
        selectableTabs = new HamonTabGui[] {
                new HamonStatsTabGui(minecraft, this, i++, "hamon.stats.tab"),
                new HamonGeneralSkillsTabGui(minecraft, this, i++, "hamon.strength_skills.tab", HamonStat.STRENGTH),
                new HamonGeneralSkillsTabGui(minecraft, this, i++, "hamon.control_skills.tab", HamonStat.CONTROL),
                new HamonTechniqueTabGui(minecraft, this, i++, "hamon.techniques.tab")
        };
        for (HamonTabGui tab : selectableTabs) {
            tab.addButtons();
            tab.updateButton();
        }
        abandonTrainingTab = new HamonAbandonTabGui(minecraft, this, i++, "hamon.abandon.tab");
        abandonTrainingTab.addButtons();
        selectTab(selectableTabs[0]);
        PacketManager.sendToServer(new ClHamonWindowOpenedPacket());
    }
    
    @Override
    public <T extends Widget> T addButton(T button) {
        return super.addButton(button);
    }

    public void removeButton(Widget button) {
        buttons.remove(button);
        children.remove(button);
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
        for (HamonTabGui hamonTabGui : selectableTabs) {
            if (hamonTabGui.isMouseOnTabIcon(x, y, mouseX, mouseY)) {
                selectTab(hamonTabGui);
                return true;
            }
        }
        if (selectedTab != null && mouseInsideWindow(mouseX, mouseY)) {
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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (mouseInsideWindow(mouseX, mouseY) && selectedTab != null) {
            selectedTab.mouseScrolled(mouseX - windowPosX() - WINDOW_THIN_BORDER, mouseY - windowPosY() - WINDOW_UPPER_BORDER, scroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }
    
    boolean mouseInsideWindow(double mouseX, double mouseY) {
        int x = windowPosX();
        int y = windowPosY();
        return mouseX > x + WINDOW_THIN_BORDER && mouseX < x + WINDOW_WIDTH - WINDOW_THIN_BORDER 
                && mouseY > y + WINDOW_UPPER_BORDER && mouseY < y + WINDOW_HEIGHT - WINDOW_THIN_BORDER;
    }
    
    void selectTab(HamonTabGui tab) {
        selectedTab = tab;
        for (HamonTabGui hamonTabGui : selectableTabs) {
            hamonTabGui.getButtons().forEach(button -> button.active = hamonTabGui == tab);
        }
        abandonTrainingTab.getButtons().forEach(button -> button.active = abandonTrainingTab == tab);
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
        tabsWithSkillRequirements.clear();
        renderBackground(matrixStack);
        renderInside(matrixStack, mouseX, mouseY, x, y, partialTick);
        renderWindow(matrixStack, x, y);
        renderToolTips(matrixStack, mouseX, mouseY, x, y);
    }

    @Override
    public void tick() {
        for (HamonTabGui tabGui : selectableTabs) {
            tabGui.tick();
        }
    }
    
    int windowPosX() {
        return (width - WINDOW_WIDTH) / 2;
    }
    
    int windowPosY() {
        return (height - WINDOW_HEIGHT) / 2;
    }
    
    void addSkillRequirementTab(HamonTabGui tab) {
        tabsWithSkillRequirements.add(tab);
    }

    private void renderInside(MatrixStack matrixStack, int mouseX, int mouseY, int windowX, int windowY, float partialTick) {
        if (selectedTab != null) {
            selectedTab.drawContents(this, matrixStack, mouseX, mouseY, partialTick, 
                    (float)(windowX + WINDOW_THIN_BORDER), (float)(windowY + WINDOW_UPPER_BORDER));
            RenderSystem.depthFunc(515);
            RenderSystem.disableDepthTest();
        }
    }

    public void renderWindow(MatrixStack matrixStack, int windowX, int windowY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        minecraft.getTextureManager().bind(WINDOW);
        blit(matrixStack, windowX, windowY, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        for (HamonTabGui tabGui : selectableTabs) {
            tabGui.drawTab(matrixStack, windowX, windowY, tabGui == selectedTab, tabsWithSkillRequirements.contains(tabGui));
        }
        RenderSystem.enableRescaleNormal();
        RenderSystem.defaultBlendFunc();
        for (HamonTabGui tabGui : selectableTabs) {
            tabGui.drawIcon(matrixStack, windowX, windowY, itemRenderer);
        }
        RenderSystem.disableBlend();
        if (selectedTab != null) {
            font.draw(matrixStack, selectedTab.getTitle(), windowX + 8, windowY + 6, 0x404040);
        }
    }

    private void renderToolTips(MatrixStack matrixStack, int mouseX, int mouseY, int windowX, int windowY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (selectedTab != null && mouseInsideWindow(mouseX, mouseY)) {
            RenderSystem.pushMatrix();
            RenderSystem.enableDepthTest();
            RenderSystem.translatef((float)(windowX + WINDOW_THIN_BORDER), (float)(windowY + WINDOW_UPPER_BORDER), 400.0F);
            selectedTab.drawToolTips(matrixStack, mouseX - windowX - WINDOW_THIN_BORDER, mouseY - windowY - WINDOW_UPPER_BORDER, windowX, windowY);
            RenderSystem.disableDepthTest();
            RenderSystem.popMatrix();
        }
        for (HamonTabGui hamonTabGui : selectableTabs) {
            if (hamonTabGui.isMouseOnTabIcon(windowX, windowY, (double)mouseX, (double)mouseY)) {
                List<IReorderingProcessor> tooltipLines = new ArrayList<IReorderingProcessor>();
                tooltipLines.add(hamonTabGui.getTitle().getVisualOrderText());
                tooltipLines.addAll(hamonTabGui.additionalTabNameTooltipInfo());
                renderTooltip(matrixStack, tooltipLines, mouseX, mouseY);
                break;
            }
        }
    }

    @SuppressWarnings("resource")
    public static void setTeacherSkills(Collection<HamonSkill> skills) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof HamonScreen) {
            HamonScreen hamonScreen = ((HamonScreen) screen);
            hamonScreen.isTeacherNearby = !skills.isEmpty();
            hamonScreen.teacherSkills = skills;
        }
    }

    @SuppressWarnings("resource")
    public static void updateTabs() {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof HamonScreen) {
            HamonScreen hamonScreen = ((HamonScreen) screen);
            for (HamonTabGui tab : hamonScreen.selectableTabs) {
                tab.updateTab();
            }
        }
    }
}
