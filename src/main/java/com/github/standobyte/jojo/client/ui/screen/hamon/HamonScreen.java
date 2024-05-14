package com.github.standobyte.jojo.client.ui.screen.hamon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap.OneTimeNotification;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.ui.screen.JojoStuffScreen;
import com.github.standobyte.jojo.client.ui.screen.TabPositionType;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClHamonWindowOpenedPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClReadHamonBreathTabPacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.AbstractHamonSkill;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.BaseHamonSkill.HamonStat;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.skill.HamonTechniqueManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.Style;

@SuppressWarnings("deprecation")
public class HamonScreen extends Screen {
    static final int WINDOW_WIDTH = 230;
    static final int WINDOW_HEIGHT = 227;
    
    static final int WINDOW_THIN_BORDER = 9;
    static final int WINDOW_UPPER_BORDER = 18;
    
    public static final ResourceLocation WINDOW = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/hamon_window.png");
    
    private final List<HamonTabGui> selectableTabs = new ArrayList<>();
    private final List<HamonTabGui> allTabs = new ArrayList<>();
    private HamonTabGui introTab;
    private boolean introWasRead;
    HamonStatsTabGui statsTab;
    HamonAbandonTabGui abandonTrainingTab;
    TmpHamonWipTabGui techniquesWipTab;
    HamonTabGui selectedTab;
    private Set<HamonTabGui> tabsWithSkillRequirements = new HashSet<>();
    boolean isTeacherNearby = false;
    @Nullable Collection<? extends AbstractHamonSkill> teacherSkills = null;
    protected int tickCount = 0;
    
    HamonData hamon;
    
    boolean clickedOnSkill;
    
    public static int screenX;
    public static int screenY;

    public HamonScreen() {
        super(NarratorChatListener.NO_TITLE);
    }
    
    @Override
    protected void init() {
        hamon = INonStandPower.getPlayerNonStandPower(minecraft.player)
                .getTypeSpecificData(ModPowers.HAMON.get()).get();
        introWasRead = minecraft.player.getCapability(PlayerUtilCapProvider.CAPABILITY)
                .map(cap -> cap.sentNotification(OneTimeNotification.HAMON_BREATH_GUIDE)).orElse(false);
        
        selectableTabs.clear();
        allTabs.clear();
        boolean addIntro = true;
        if (addIntro) {
            selectableTabs.add(introTab = new HamonIntroTabGui(minecraft, this, "hamon.intro.tab"));
        }
        selectableTabs.add(statsTab = new HamonStatsTabGui(minecraft, this, "hamon.stats.tab"));
        selectableTabs.add(new HamonGeneralSkillsTabGui(minecraft, this, "hamon.strength_skills.tab", HamonStat.STRENGTH));
        selectableTabs.add(new HamonGeneralSkillsTabGui(minecraft, this, "hamon.control_skills.tab", HamonStat.CONTROL));
        if (HamonTechniqueManager.techniquesEnabled(true)) {
            selectableTabs.add(new HamonTechniqueTabGui(minecraft, this, "hamon.techniques.tab"));
        }
        
        reorderTabs();
        
        allTabs.addAll(selectableTabs);
        allTabs.add(abandonTrainingTab = new HamonAbandonTabGui(minecraft, this, "hamon.abandon.tab"));
        allTabs.add(techniquesWipTab = new TmpHamonWipTabGui(minecraft, this));
        
        for (HamonTabGui tab : allTabs) {
            tab.addButtons();
            tab.updateButtons();
        }
        selectTab(introWasRead ? selectableTabs.get(1) : introTab);
        PacketManager.sendToServer(new ClHamonWindowOpenedPacket());
    }
    
    private void reorderTabs() {
        int i = 0;
        for (HamonTabGui tab : selectableTabs) {
//            if (introWasRead && tab == introTab) {
//                tab.setPosition(TabPositionType.RIGHT, 0);
//            }
//            else {
                tab.setPosition(TabPositionType.LEFT, i++);
//            }
        }
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
        
        if (mouseInsideWindow(mouseX, mouseY) && super.mouseClicked(mouseX, mouseY, mouseButton)) {
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
        if (JojoStuffScreen.mouseClick(mouseX, mouseY, 
                JojoStuffScreen.uniformX(minecraft), JojoStuffScreen.uniformY(minecraft), 
                JojoStuffScreen.TabsEnumType.HAMON)) {
            return true;
        }
        if (selectedTab != null) {
            if (selectedTab.mouseClicked(
                    mouseX - x - WINDOW_THIN_BORDER, 
                    mouseY - y - WINDOW_UPPER_BORDER, 
                    mouseButton, mouseInsideWindow(mouseX, mouseY))) {
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
            selectedTab.mouseDragged(xMovement, yMovement);
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
        if (selectedTab == tab) return;
        
        if (tab != introTab && !introWasRead) {
            introWasRead = true;
            PacketManager.sendToServer(new ClReadHamonBreathTabPacket());
            reorderTabs();
        }
        
        boolean wipNotice = true;
        if (wipNotice && !(selectedTab instanceof TmpHamonWipTabGui) && tab instanceof HamonTechniqueTabGui) {
            techniquesWipTab.techniquesTab = tab;
            tab = techniquesWipTab;
        }
        
        selectedTab = tab;
        for (HamonTabGui hamonTabGui : allTabs) {
            hamonTabGui.onTabSelected(tab);
        }
        tab.updateTab();
    }
    
    boolean forEachTabUntil(Predicate<HamonTabGui> action) {
        for (HamonTabGui tab : selectableTabs) {
            if (action.test(tab)) {
                return true;
            }
        }
        return false;
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
        partialTick = minecraft.getFrameTime(); // i was jebaited
        int x = windowPosX();
        int y = windowPosY();
        screenX = x;
        screenY = y;
        tabsWithSkillRequirements.clear();
        renderBackground(matrixStack);
        renderInside(matrixStack, mouseX, mouseY, x, y, partialTick);
        renderWindow(matrixStack, mouseX, mouseY, x, y);
        renderToolTips(matrixStack, mouseX, mouseY, x, y);
    }

    @Override
    public void tick() {
        tickCount++;
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

    public void renderWindow(MatrixStack matrixStack, int mouseX, int mouseY, int windowX, int windowY) {
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
        JojoStuffScreen.renderHamonTabs(matrixStack, 
                JojoStuffScreen.uniformX(minecraft), JojoStuffScreen.uniformY(minecraft), false, 
                mouseX, mouseY, this, JojoStuffScreen.HamonTab.MAIN_SCREEN);
        RenderSystem.disableBlend();
        if (selectedTab != null) {
            font.draw(matrixStack, selectedTab.getTitle(), windowX + 8, windowY + 6, 0x404040);
        }
    }

    private int tooltipOffsetX;
    private int tooltipOffsetY;
    private void renderToolTips(MatrixStack matrixStack, int mouseX, int mouseY, int windowX, int windowY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (selectedTab != null && mouseInsideWindow(mouseX, mouseY)) {
            RenderSystem.pushMatrix();
            RenderSystem.enableDepthTest();
            tooltipOffsetX = windowX + WINDOW_THIN_BORDER;
            tooltipOffsetY = windowY + WINDOW_UPPER_BORDER;
            RenderSystem.translatef((float) tooltipOffsetX, (float) tooltipOffsetY, 400.0F);
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
    
    @Override
    public void renderComponentHoverEffect(MatrixStack matrixStack, @Nullable Style style, int mouseX, int mouseY) {
        super.renderComponentHoverEffect(matrixStack, style, mouseX, mouseY);
    }

    // these two overrides make the tooltip wrap at the right edge of the screen correctly
    @Override
    public void renderToolTip(MatrixStack matrixStack, List<? extends IReorderingProcessor> tooltips, 
            int mouseX, int mouseY, FontRenderer font) {
        matrixStack.translate(-tooltipOffsetX, -tooltipOffsetY, 0);
        super.renderToolTip(matrixStack, tooltips, mouseX + tooltipOffsetX, mouseY + tooltipOffsetY, font);
        matrixStack.translate(tooltipOffsetX, tooltipOffsetY, 0);
    }
    
    @Override
    public void renderWrappedToolTip(MatrixStack matrixStack, List<? extends ITextProperties> tooltips, 
            int mouseX, int mouseY, FontRenderer font) {
        matrixStack.translate(-tooltipOffsetX, -tooltipOffsetY, 0);
        super.renderWrappedToolTip(matrixStack, tooltips, mouseX + tooltipOffsetX, mouseY + tooltipOffsetY, font);
        matrixStack.translate(tooltipOffsetX, tooltipOffsetY, 0);
    }
    
    public static void setTeacherSkills(Collection<? extends AbstractHamonSkill> skills) {
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
            for (HamonTabGui tab : hamonScreen.selectableTabs) {
                tab.updateTab();
                tab.onTabSelected(hamonScreen.selectedTab); // makes sure the learn technique buttons are disabled
            }
        }
    }
}
