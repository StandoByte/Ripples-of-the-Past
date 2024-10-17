package com.github.standobyte.jojo.client.ui.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientEventHandler;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.screen.controls.HudLayoutEditingScreen;
import com.github.standobyte.jojo.client.ui.screen.hamon.HamonScreen;
import com.github.standobyte.jojo.client.ui.screen.standskin.StandSkinsScreen;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
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
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public interface IJojoScreen {
    
    TabCategory getTabCategory();
    
    Tab getTab();
    
    default void defaultRenderTabs(MatrixStack matrixStack, int mouseX, int mouseY, Screen thisAsScreen) {
        renderCategoryTabs(matrixStack, 
                mouseX, mouseY, thisAsScreen, getTabCategory());
        renderVerticalTabs(matrixStack, HandSide.RIGHT, 
                true, mouseX, mouseY, thisAsScreen, 
                getTab(), getTabCategory());
    }
    
    default boolean defaultClickTab(double mouseX, double mouseY) {
        return clickCategoryTab(mouseX, mouseY) || mouseClickRightSideTab(mouseX, mouseY, getTabCategory());
    }
    
    
    public static final ResourceLocation TABS = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/screen_tabs.png");
    
    public static int uniformX(Minecraft mc) { return (mc.getWindow().getGuiScaledWidth()  + HudLayoutEditingScreen.WINDOW_WIDTH ) / 2; }
    public static int uniformY(Minecraft mc) { return (mc.getWindow().getGuiScaledHeight() - HudLayoutEditingScreen.WINDOW_HEIGHT) / 2; }
    
    public static int uniformUpperX(Minecraft mc) { return (mc.getWindow().getGuiScaledWidth()  - HudLayoutEditingScreen.WINDOW_WIDTH ) / 2; }
    public static int uniformUpperY(Minecraft mc) { return (mc.getWindow().getGuiScaledHeight() - HudLayoutEditingScreen.WINDOW_HEIGHT) / 2 - 28; }
    
    
    public static class Tab {
        protected ResourceLocation icon;
        protected int texX;
        protected int texY;
        protected int texSizeX;
        protected int texSizeY;
        protected ITextComponent name;
        protected Supplier<Screen> openScreen;
        protected boolean isEnabled;
        
        public Tab(ResourceLocation icon, int texX, int texY, ITextComponent name) {
            this(icon, texX, texY, 256, 256, name);
        }
        
        public Tab(ResourceLocation icon, int texX, int texY, 
                int texSizeX, int texSizeY, ITextComponent name) {
            this.icon = icon;
            this.texX = texX;
            this.texY = texY;
            this.texSizeX = texSizeX;
            this.texSizeY = texSizeY;
            this.name = name;
            this.isEnabled = true;
        }
        
        public Tab disable() {
            this.isEnabled = false;
            return this;
        }
        
        public Tab withScreen(Supplier<Screen> openScreen) {
            this.openScreen = openScreen;
            return this;
        }
        
        public <T extends Screen & IJojoScreen> Tab rememberLastOpened(Class<T> screenClass) {
            LastScreenRemembered.REMEMBER_LAST_TAB.add(screenClass);
            return this;
        }
        
        
        protected boolean openScreen(@Nullable TabCategory curCategory) {
            if (openScreen == null) {
                throw new UnsupportedOperationException();
            }
            Screen screen = openScreen.get();
            if (screen != null) {
                Minecraft.getInstance().setScreen(screen);
                return true;
            }
            return false;
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
    
    
    public static void renderCategoryTabs(MatrixStack matrixStack, 
            int mouseX, int mouseY, Screen screen, 
            TabCategory tabSelected) {
        Minecraft mc = Minecraft.getInstance();
        renderCategoryTabs(matrixStack, uniformUpperX(mc), uniformUpperY(mc), mouseX, mouseY, screen, tabSelected);
    }
    
    public static void renderCategoryTabs(MatrixStack matrixStack, 
            int x, int y, int mouseX, int mouseY, Screen screen, 
            TabCategory tabSelected) {
        TabCategory[] activeTabs = TabCategory.getVisibleCategories();
        int selectedIndex = ArrayUtils.indexOf(activeTabs, tabSelected);
        
        int x0 = x;
        TextureManager textureManager = Minecraft.getInstance().textureManager;
        for (int i = 0; i < activeTabs.length; i++) {
            boolean isSelected = i == selectedIndex;
            int texX = (isSelected ? 0 : 168) + (i == 0 ? 0 : 28);
            int texY = isSelected ? 32 : 2;
            textureManager.bind(TABS);
            AbstractGui.blit(matrixStack, x, y, texX, texY, 28, 32, 256, 256);
            x += 28;
        }
        
        x = x0;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        for (int i = 0; i < activeTabs.length; i++) {
            TabCategory tab = activeTabs[i];
            tab.renderIcon(matrixStack, x + 6, y + 9);
            x += 28;
        }
        RenderSystem.disableBlend();
        
        x = x0;
        int tooltipTab = upperTabMouseOver(mouseX, mouseY, x, y, activeTabs.length);
        if (tooltipTab >= 0) {
            screen.renderTooltip(matrixStack, activeTabs[tooltipTab].getName(), mouseX, mouseY);
        }
    }
    
    public static final int UPPER_TABS_RIGHT_ALIGNMENT_OFFSET = 98;
    public static void renderCategoryTabsRight(MatrixStack matrixStack, 
            int x, int y, int mouseX, int mouseY, Screen screen, 
            TabCategory categorySelected) {
        TabCategory[] activeTabs = TabCategory.getVisibleCategories();
        int selectedIndex = ArrayUtils.indexOf(activeTabs, categorySelected);
        boolean atTheTop = false;
        
        int y0 = y;
        TextureManager textureManager = Minecraft.getInstance().textureManager;
        for (int i = 0; i < activeTabs.length; i++) {
            boolean isSelected = i == selectedIndex;
            int texX = atTheTop && i == 0 ? 96 : 128;
            int texY = isSelected ? 92 : 64;
            textureManager.bind(TABS);
            AbstractGui.blit(matrixStack, x - 4, y, texX, texY, 32, 28, 256, 256);
            y += 28;
        }
        
        y = y0;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        for (int i = 0; i < activeTabs.length; i++) {
            TabCategory tab = activeTabs[i];
            tab.renderIcon(matrixStack, x + 2, y + 6);
            y += 28;
        }
        RenderSystem.disableBlend();
        
        y = y0;
        int tooltipTab = getTabMouseOver(mouseX, mouseY, x, y, HandSide.RIGHT, activeTabs.length);
        if (tooltipTab >= 0) {
            screen.renderTooltip(matrixStack, activeTabs[tooltipTab].getName(), mouseX, mouseY);
        }
    }
    
    @Nullable
    public static TabCategory upperTabAt(double mouseX, double mouseY, int tabsX, int tabsY) {
        TabCategory[] activeTabs = TabCategory.getVisibleCategories();
        int index = upperTabMouseOver((int) mouseX, (int) mouseY, tabsX, tabsY, activeTabs.length);
        if (index >= 0) {
            return activeTabs[index];
        }
        return null;
    }
    
    public static boolean clickCategoryTab(double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        return clickCategoryTab(mouseX, mouseY, uniformUpperX(mc), uniformUpperY(mc));
    }
    
    public static boolean clickCategoryTab(double mouseX, double mouseY, int tabsX, int tabsY) {
        TabCategory upperTab = upperTabAt(mouseX, mouseY, tabsX, tabsY);
        if (upperTab != null) {
            return upperTab.onClick();
        }
        return false;
    }
    
    public static int upperTabMouseOver(int mouseX, int mouseY, int tabsX, int tabsY, int tabsCount) {
        if (mouseY >= tabsY && mouseY < tabsY + 30) {
            for (int i = 0; i < tabsCount; i++) {
                if (mouseX >= tabsX && mouseX < tabsX + 28) {
                    return i;
                }
                tabsX += 28;
            }
        }
        
        return -1;
    }
    
    
    public static void renderRightSideTabs(MatrixStack matrixStack, 
            boolean atTheTop, int mouseX, int mouseY, Screen screen, 
            Tab tab, TabCategory category) {
        Minecraft mc = Minecraft.getInstance();
        renderVerticalTabs(matrixStack, HandSide.RIGHT, 
                uniformX(mc), uniformY(mc), atTheTop, mouseX, mouseY, screen, 
                tab, category);
    }
    
    public static void renderVerticalTabs(MatrixStack matrixStack, HandSide tabsSide, 
            boolean atTheTop, int mouseX, int mouseY, Screen screen, 
            Tab tabSelected, TabCategory category) {
        Minecraft mc = Minecraft.getInstance();
        renderVerticalTabs(matrixStack, tabsSide, 
                uniformX(mc), uniformY(mc), atTheTop, mouseX, mouseY, screen, 
                tabSelected, category);
    }
    
    public static void renderVerticalTabs(MatrixStack matrixStack, HandSide tabsSide, 
            int x, int y, boolean atTheTop, int mouseX, int mouseY, Screen screen, 
            Tab tabSelected, TabCategory category) {
        Tab[] activeTabs = category.getActiveTabs();
        int selectedIndex = ArrayUtils.indexOf(activeTabs, tabSelected);

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
    
    public static int getTabMouseOver(int mouseX, int mouseY, int tabsX, int tabsY, HandSide tabsSide, int tabsCount) {
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
    
    public static boolean mouseClickRightSideTab(double mouseX, double mouseY, TabCategory category) {
        Minecraft mc = Minecraft.getInstance();
        return mouseClickSideTab(mouseX, mouseY, uniformX(mc), uniformY(mc), HandSide.RIGHT, category);
    }
    
    public static boolean mouseClickSideTab(double mouseX, double mouseY, int tabsX, int tabsY, HandSide tabsSide, TabCategory category) {
        Tab[] activeTabs = category.getActiveTabs();
        
        int tabIndex = getTabMouseOver((int) mouseX, (int) mouseY, tabsX, tabsY, tabsSide, activeTabs.length);
        if (tabIndex >= 0) {
            Tab tab = activeTabs[tabIndex];
            tab.openScreen(category);
            return true;
        }
        
        return false;
    }
    
    
    
    public static class LastScreenRemembered {
        private static final Set<Class<? extends IJojoScreen>> REMEMBER_LAST_TAB = new HashSet<>();
        
        public static TabCategory lastTabCategory;
        public static PowerClassification lastHudEditingPowerClass;
    }
    
    public static void setTabToOpenNext(TabCategory tabCategory, Tab tab) {
        if (tabCategory != null) {
            LastScreenRemembered.lastTabCategory = tabCategory;
            tabCategory.lastTab = tab;
        }
    }
    
    public static void rememberScreenTab(Screen screen) {
        if (screen != null && LastScreenRemembered.REMEMBER_LAST_TAB.contains(screen.getClass())) {
            IJojoScreen jojoScreen = (IJojoScreen) screen;
            setTabToOpenNext(jojoScreen.getTabCategory(), jojoScreen.getTab());
        }
    }
    
    public static void onScreenKeyPress() {
        TabCategory category = LastScreenRemembered.lastTabCategory;
        if (category != null && category.isCategoryVisible() && category.lastTab != null && category.lastTab.isEnabled) {
            category.lastTab.openScreen(null);
        }
        else {
            CONTROLS_TAB.openScreen(null);
        }
    }
    
    
    
    public static class TabCategory {
        private static final List<TabCategory> VALUES = new ArrayList<>();
        
        public static final TabCategory GENERAL = new TabCategory(GeneralTab.values(), new TranslationTextComponent("jojo.ui.player_menu")) {
            @Override public void renderIcon(MatrixStack matrixStack, int x, int y) { 
                ClientUtil.renderPlayerFace(matrixStack, x, y, Minecraft.getInstance().player);
            }
        };
        
        public static final TabCategory STAND = new TabCategory(StandTab.values(), PowerClassification.STAND);
        
        public static final TabCategory NON_STAND_DEFAULT = new TabCategory(new TabSupplier[] { () -> CONTROLS_TAB }, PowerClassification.NON_STAND) {
            @Override public boolean isCategoryVisible() {
                return super.isCategoryVisible() && !NON_STAND_CATEGORIES.containsKey(InputHandler.getInstance().getPowerCache(this.power).getType().getRegistryName());
            }
        };
        
        public Tab lastTab;
        
        
        protected final TabSupplier[] tabs;
        protected final PowerClassification power;
        protected final ITextComponent name;
        
        public TabCategory(TabSupplier[] tabs, PowerClassification power) {
            this(tabs, StringTextComponent.EMPTY, power);
        }
        
        public TabCategory(TabSupplier[] tabs, ITextComponent name) {
            this(tabs, name, null);
        }
        
        public TabCategory(TabSupplier[] tabs, ITextComponent name, PowerClassification power) {
            this.tabs = tabs;
            this.power = power;
            this.name = name;
            VALUES.add(this);
        }
        
        public TabSupplier[] getTabs() {
            return tabs;
        }
        
        public boolean onClick() {
            Tab tab = (lastTab != null ? lastTab : getTabs()[0].get());
            if (tab != null) {
                tab.openScreen(this);
                return true;
            }
            return false;
        }
        
        public Tab[] getActiveTabs() {
            return Arrays.stream(getTabs())
                    .map(Supplier::get).filter(Tab::isActive)
                    .toArray(Tab[]::new);
        }
        
        public boolean isCategoryVisible() {
            if (power != null && !InputHandler.getInstance().hasPower(power)) {
                return false;
            }
            return Arrays.stream(getTabs()).map(Supplier::get).anyMatch(Tab::isActive);
        }
        
        public ITextComponent getName() {
            if (power != null) {
                return InputHandler.getInstance().getPowerCache(power).getName();
            }
            return this.name;
        }
        
        public void renderIcon(MatrixStack matrixStack, int x, int y) {
            if (power != null) {
                ResourceLocation icon = InputHandler.getInstance().getPowerCache(power).clGetPowerTypeIcon(); 
                Minecraft.getInstance().getTextureManager().bind(icon);
                AbstractGui.blit(matrixStack, x, y, 0, 0, 16, 16, 16, 16);
            }
        }
        
        public PowerClassification getPower() {
            return power;
        }
        
        
        private static final Map<ResourceLocation, TabCategory> NON_STAND_CATEGORIES = new HashMap<>();
        public static <T extends TabSupplier> TabCategory registerCategory(NonStandPowerType<?> powerType, T[] tabsArray) {
            TabCategory tabCategory = new TabCategory(tabsArray, PowerClassification.NON_STAND) {
                @Override
                public boolean isCategoryVisible() {
                    return InputHandler.getInstance().getPowerCache(PowerClassification.NON_STAND).getType() == powerType
                            && super.isCategoryVisible();
                }
            };
            NON_STAND_CATEGORIES.put(powerType.getRegistryName(), tabCategory);
            return tabCategory;
        }
        
        public static TabCategory getTabsCategory(IPower<?, ?> power) {
            if (power.hasPower()) {
                IPowerType<?, ?> powerType = power.getType();
                TabCategory registered = NON_STAND_CATEGORIES.get(powerType.getRegistryName());
                if (registered != null) {
                    return registered;
                }
                if (power.getPowerClassification() == PowerClassification.STAND) {
                    return STAND;
                }
            }

            return null;
        }
        
        public static TabCategory[] getVisibleCategories() {
            return VALUES.stream()
                    .filter(TabCategory::isCategoryVisible)
                    .toArray(TabCategory[]::new);
        }
        
        @Nullable
        public static TabCategory getCategoryForPower(IPower<?, ?> power) {
            TabCategory category = null;
            if (power != null && power.hasPower()) {
                switch (power.getPowerClassification()) {
                case STAND:
                    category = STAND;
                    break;
                case NON_STAND:
                    TabCategory nonStandCategory = NON_STAND_CATEGORIES.get(power.getType().getRegistryName());
                    category = nonStandCategory != null ? nonStandCategory : NON_STAND_DEFAULT;
                    break;
                default:
                    throw new AssertionError();
                }
            }
            if (category != null && category.isCategoryVisible()) {
                return category;
            }
            return null;
        }
    }
    
    
    
    public static enum GeneralTab implements TabSupplier {
        WIP_CATEGORY(new Tab(null, 
                0, 0, 16, 16, new StringTextComponent("TBA"))
                .withScreen(PlaceholderScreen::new));
        
        
        private final Tab tab;
        private GeneralTab(Tab tab) {
            this.tab = tab;
        }
        
        @Override
        public Tab get() {
            return tab;
        }
    }
    
    
    public static final Tab CONTROLS_TAB = new Tab(TABS, 240, 240, new TranslationTextComponent("jojo.key.edit_hud")) {
        @Override
        protected boolean openScreen(TabCategory curCategory) {
            PowerClassification power = curCategory != null ? curCategory.getPower() : null;
            if (power == null) power = ActionsOverlayGui.getInstance().getCurrentMode();
            if (power == null) power = LastScreenRemembered.lastHudEditingPowerClass;
            if (power == null) power = PowerClassification.STAND;
            
            if (power != null) {
                if (!InputHandler.getInstance().hasPower(power)) {
                    power = power == PowerClassification.STAND ? PowerClassification.NON_STAND : PowerClassification.STAND;
                }
                if (InputHandler.getInstance().hasPower(power)) {
                    Minecraft.getInstance().setScreen(new HudLayoutEditingScreen(power));
                    return true;
                }
            }
            return false;
        }
    }.rememberLastOpened(HudLayoutEditingScreen.class);
    
    public static enum StandTab implements TabSupplier {
        GENERAL_INFO(new Tab(null, 0, 0, 16, 16, new TranslationTextComponent("jojo.stand_ui.name")) {
            @Override
            protected void renderIcon(MatrixStack matrixStack, int x, int y) {
                IPower<?, ?> playerStand = InputHandler.getInstance().getPowerCache(PowerClassification.STAND);
                icon = playerStand.clGetPowerTypeIcon();
                super.renderIcon(matrixStack, x, y);
            }
        }
        .withScreen(StandInfoScreen::new)
        .rememberLastOpened(StandInfoScreen.class)),
        
        CONTROLS(CONTROLS_TAB),
        
        SKINS(new Tab(null, 0, 0, 16, 16, new TranslationTextComponent("jojo.stand_skins.button")) {
            @Override
            protected void renderIcon(MatrixStack matrixStack, int x, int y) {
                icon = null;
                IPower<?, ?> playerStand = InputHandler.getInstance().getPowerCache(PowerClassification.STAND);
                if (playerStand.hasPower()) {
                    List<StandSkin> allSkins = StandSkinsManager.getInstance().getStandSkinsView(playerStand.getType().getRegistryName());
                    if (!allSkins.isEmpty()) {
                        float ticks = ClientEventHandler.getInstance().tickCount + ClientUtil.getPartialTick();
                        StandSkin cycledSkin = allSkins.get((int) (ticks / 20) % allSkins.size());
                        icon = StandSkinsManager.getInstance().getRemappedResPath(manager -> Optional.of(cycledSkin), playerStand.getType().getIconTexture(null));
                    }
                }
                super.renderIcon(matrixStack, x, y);
            }
        }
        .withScreen(() -> new StandSkinsScreen(IStandPower.getPlayerStandPower(Minecraft.getInstance().player)))
        .rememberLastOpened(StandSkinsScreen.class));
        
        
        private final Tab tab;
        private StandTab(Tab tab) {
            this.tab = tab;
        }
        
        @Override
        public Tab get() {
            return tab;
        }
    }
    
    public static enum HamonTab implements TabSupplier {
        MAIN_SCREEN(new Tab(ModPowers.HAMON.get().getIconTexture(null), 
                0, 0, 16, 16, new TranslationTextComponent("jojo.key.hamon_skills_window"))
                .withScreen(HamonScreen::new)),
        
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
