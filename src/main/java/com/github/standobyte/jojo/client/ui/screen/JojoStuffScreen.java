package com.github.standobyte.jojo.client.ui.screen;

import java.util.Arrays;
import java.util.function.Supplier;

import org.apache.commons.lang3.ArrayUtils;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ui.screen.controls.HudLayoutEditingScreen;
import com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen;
import com.github.standobyte.jojo.client.ui.screen.standskin.StandSkinsScreen;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class JojoStuffScreen {
    public static final ResourceLocation TABS = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/screen_tabs.png");
    public static final Tab CONTROLS_TAB = new Tab(TABS, 240, 240, new TranslationTextComponent("jojo.key.edit_hud"), 
            HudLayoutEditingScreen::new) {
        @Override
        protected Screen onClick() {
            Screen screen = new HudLayoutEditingScreen(hudEditingCurPower);
            Minecraft.getInstance().setScreen(screen);
            return screen;
        }
    };
    
    public static int uniformX(Minecraft mc) { return (mc.getWindow().getGuiScaledWidth()  + HudLayoutEditingScreen.WINDOW_WIDTH ) / 2; }
    public static int uniformY(Minecraft mc) { return (mc.getWindow().getGuiScaledHeight() - HudLayoutEditingScreen.WINDOW_HEIGHT) / 2; }
    
    public static class Tab {
        protected ResourceLocation icon;
        protected int texX;
        protected int texY;
        protected int texSizeX;
        protected int texSizeY;
        protected ITextComponent name;
        protected final Supplier<Screen> openScreen;
        protected boolean isEnabled;
        
        public Tab(ResourceLocation icon, int texX, int texY, ITextComponent name, Supplier<Screen> openScreen) {
            this(icon, texX, texY, 256, 256, name, openScreen);
        }
        
        public Tab(ResourceLocation icon, int texX, int texY, 
                int texSizeX, int texSizeY, ITextComponent name,
                Supplier<Screen> openScreen) {
            this.icon = icon;
            this.texX = texX;
            this.texY = texY;
            this.texSizeX = texSizeX;
            this.texSizeY = texSizeY;
            this.name = name;
            this.openScreen = openScreen;
            this.isEnabled = true;
        }
        
        public Tab disable() {
            this.isEnabled = false;
            return this;
        }
        
        protected Screen onClick() {
            Screen screen = openScreen.get();
            Minecraft.getInstance().setScreen(screen);
            return screen;
        }
        
        protected void renderIcon(MatrixStack matrixStack, int x, int y) {
            if (icon != null) {
                Minecraft.getInstance().textureManager.bind(icon);
                AbstractGui.blit(matrixStack, x + 2, y + 6, texX, texY, 16, 16, texSizeX, texSizeY);
            }
        }
        
        protected boolean isActive() {
            return isEnabled;
        }
    }
    
    public static interface TabSupplier extends Supplier<Tab> {}
    
    @Deprecated
    public static void renderRightSideTabs(MatrixStack matrixStack, 
            int x, int y, boolean atTheTop, int mouseX, int mouseY, Screen screen, 
            TabSupplier tabSelected, TabSupplier... tabs) {
        renderVerticalTabs(matrixStack, HandSide.RIGHT, 
                x, y, atTheTop, mouseX, mouseY, screen, 
                tabSelected, tabs);
    }
    
    public static void renderVerticalTabs(MatrixStack matrixStack, HandSide tabsSide, 
            int x, int y, boolean atTheTop, int mouseX, int mouseY, Screen screen, 
            TabSupplier tabSelected, TabSupplier... tabs) {
        Tab[] activeTabs = Arrays.stream(tabs)
                .map(Supplier::get).filter(Tab::isActive)
                .toArray(Tab[]::new);
        int selectedIndex = ArrayUtils.indexOf(activeTabs, tabSelected.get());

        int x0 = x;
        int y0 = y;
        TextureManager textureManager = Minecraft.getInstance().textureManager;
        if (tabsSide == HandSide.LEFT) {
            x -= 24;
        }
        for (int i = 0; i < activeTabs.length; i++) {
            boolean isSelected = i == selectedIndex;
            int texX;
            switch (tabsSide) {
            case LEFT:
                texX = atTheTop && i == 0 ? 0 : 32;
                break;
            case RIGHT:
                texX = atTheTop && i == 0 ? 96 : 128;
                break;
            default:
                return;
            }
            int texY = isSelected ? 92 : 64;
            textureManager.bind(TABS);
            AbstractGui.blit(matrixStack, x - 4, y, texX, texY, 32, 28, 256, 256);
            y += 28;
        }
        
        y = y0;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (tabsSide == HandSide.LEFT) {
            x += 4;
        }
        for (int i = 0; i < activeTabs.length; i++) {
            Tab tab = activeTabs[i];
            tab.renderIcon(matrixStack, x, y);
            y += 28;
        }
        RenderSystem.disableBlend();
        
        x = x0;
        y = y0;
        int tooltipTab = getTabMouseOver(mouseX, mouseY, x, y, tabsSide, activeTabs.length);
        if (tooltipTab >= 0) {
            screen.renderTooltip(matrixStack, activeTabs[tooltipTab].name, mouseX, mouseY);
        }
    }
    
    private static int getTabMouseOver(int mouseX, int mouseY, int tabsX, int tabsY, HandSide tabsSide, int tabsCount) {
        if (tabsSide == HandSide.LEFT) {
            tabsX -= 28;
        }
        if (mouseX >= tabsX && mouseX < tabsX + 30) {
            for (int i = 0; i < tabsCount; i++) {
                if (mouseY >= tabsY && mouseY < tabsY + 28) {
                    return i;
                }
                tabsY += 28;
            }
        }
        
        return -1;
    }
    
    public static PowerClassification hudEditingCurPower;
    public static boolean mouseClick(double mouseX, double mouseY, int tabsX, int tabsY, HandSide tabsSide, TabSupplier[] tabValues) {
        Tab[] activeTabs = Arrays.stream(tabValues)
                .map(Supplier::get).filter(Tab::isActive)
                .toArray(Tab[]::new);
        
        int tabIndex = getTabMouseOver((int) mouseX, (int) mouseY, tabsX, tabsY, tabsSide, activeTabs.length);
        if (tabIndex >= 0) {
            Tab tab = activeTabs[tabIndex];
            tab.onClick();
            return true;
        }
        
        return false;
    }
    
    
    
    public static enum StandTab implements TabSupplier {
        GENERAL_INFO(new Tab(null, 0, 0, 16, 16, new TranslationTextComponent("jojo.stand_ui.name"), 
                StandInfoScreen::new).disable() /* WIP screen */),
        CONTROLS(CONTROLS_TAB),
        SKINS(new Tab(null, 0, 0, 16, 16, new TranslationTextComponent("jojo.stand_skins.button"), 
                () -> new StandSkinsScreen(IStandPower.getPlayerStandPower(Minecraft.getInstance().player)))
                .disable() /* WIP */
                );
        
        private final Tab tab;
        private StandTab(Tab tab) {
            this.tab = tab;
        }
        
        @Override
        public Tab get() {
            return tab;
        }
    }
    
    public static void renderStandTabs(MatrixStack matrixStack, 
            int x, int y, boolean atTheTop, int mouseX, int mouseY, Screen screen, 
            StandTab tab, IStandPower playerStand) {
        StandTab.GENERAL_INFO.tab.icon = playerStand.clGetPowerTypeIcon();
        StandTab.SKINS.tab.icon = playerStand.clGetPowerTypeIcon();
        renderVerticalTabs(matrixStack, HandSide.RIGHT, 
                x, y, atTheTop, mouseX, mouseY, screen, 
                tab, StandTab.values());
    }

    
    
    public static enum HamonTab implements TabSupplier {
        MAIN_SCREEN(new Tab(ModPowers.HAMON.get().getIconTexture(null), 
                0, 0, 16, 16, new TranslationTextComponent("jojo.key.hamon_skills_window"), HamonScreen::new)),
        CONTROLS(CONTROLS_TAB);
        
        private final Tab tab;
        private HamonTab(Tab tab) {
            this.tab = tab;
        }
        
        @Override
        public Tab get() {
            return tab;
        }
    }
    
    public static enum VampirismTab implements TabSupplier {
        CONTROLS(CONTROLS_TAB);
        
        private final Tab tab;
        private VampirismTab(Tab tab) {
            this.tab = tab;
        }
        
        @Override
        public Tab get() {
            return tab;
        }
    }
    
}
