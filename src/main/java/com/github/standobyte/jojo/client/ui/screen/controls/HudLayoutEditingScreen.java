package com.github.standobyte.jojo.client.ui.screen.controls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.InputHandler.MouseButton;
import com.github.standobyte.jojo.client.controls.ActionKeybindEntry;
import com.github.standobyte.jojo.client.controls.ActionVisibilitySwitch;
import com.github.standobyte.jojo.client.controls.ActionsHotbar;
import com.github.standobyte.jojo.client.controls.ControlScheme;
import com.github.standobyte.jojo.client.controls.HudControlSettings;
import com.github.standobyte.jojo.client.controls.PowerTypeControlSchemes;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.screen.JojoStuffScreen;
import com.github.standobyte.jojo.client.ui.screen.widgets.CustomButton;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.general.Vector2i;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.ControlsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.client.gui.widget.list.KeyBindingList;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.settings.KeyModifier;

/* 
 * FIXME !!!! (layout editing) close the window when the player's power being changed/replaced
 */
// TODO saving layout variants
@SuppressWarnings("deprecation")
public class HudLayoutEditingScreen extends Screen {
    private static final ResourceLocation WINDOW = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/layout_editing.png");
    public static final int WINDOW_WIDTH = 230;
    public static final int WINDOW_HEIGHT = 180;
    
    private static PowerClassification selectedTab = null;
    private IPower<?, ?> selectedPower;
    private List<IPower<?, ?>> powersPresent = new ArrayList<>();
    
    private Optional<ActionSlot> draggedAction = Optional.empty();
    private Optional<ActionSlot> hoveredAction = Optional.empty();
    
    private PowerTypeControlSchemes currentControlsScreen;
    private ControlScheme currentControlScheme;
    private Set<ResourceLocation> editedLayouts = new HashSet<>();
    
    private final SelectedKey selectedKey = new SelectedKey();
    private ActionKeybindsList keybindsList;
    private Widget addKeybindButton;
    
    public static Predicate<KeyBindingList.Entry> scrollCtrlListTo = null;

    public HudLayoutEditingScreen() {
        super(new TranslationTextComponent("jojo.screen.edit_hud_layout"));
    }

    public HudLayoutEditingScreen(PowerClassification selectTab) {
        this();
        selectedTab = selectTab;
    }
    
    @Override
    protected void init() {
        // reset layout
        addButton(new CustomButton(getWindowX() - 26, getWindowY() + WINDOW_HEIGHT - 34, 24, 24, 
                button -> {
                    currentControlScheme.reset(selectedPower);
                    markLayoutEdited();
                    refreshCustomKeybindEntries();
                }, 
                (button, matrixStack, x, y) -> {
                    renderTooltip(matrixStack, new TranslationTextComponent("jojo.screen.edit_hud_layout.reset"), x, y);
                }) {

            @Override
            protected void renderCustomButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.getTextureManager().bind(WINDOW);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                blit(matrixStack, x, y, 0, 184 + getYImage(isHovered()) * height, width, height);
            }
        });
        
        // vanilla controls settings
        addButton(new CustomButton(getWindowX() - 24, getWindowY() + WINDOW_HEIGHT - 120, 22, 22, 
                button -> {
                    ControlsScreen mcControlsScreen = new ControlsScreen(this, minecraft.options);
                    
                    scrollCtrlListTo = entry -> {
                        if (entry instanceof KeyBindingList.CategoryEntry) {
                            ITextComponent categoryName = ClientReflection.getName((KeyBindingList.CategoryEntry) entry);
                            return InputHandler.MAIN_CATEGORY.equals(((TranslationTextComponent) categoryName).getKey());
                        }
                        
                        return false;
                    };
                    
                    minecraft.setScreen(mcControlsScreen);
                }, 
                (button, matrixStack, x, y) -> {
                    renderTooltip(matrixStack, new TranslationTextComponent("jojo.screen.edit_hud_layout.mc_controls"), x, y);
                }) {

            @Override
            protected void renderCustomButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.getTextureManager().bind(WINDOW);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                blit(matrixStack, x, y, 164, 190 + getYImage(isHovered()) * height, width, height);
            }
        });
        
        keybindsList = new ActionKeybindsList(minecraft, 254, 0, getWindowY() + 57, getWindowY() + 171, 22, this, selectedKey);
        keybindsList.setLeftPos(getWindowX() + 9);
        children.add(keybindsList);
        
        if (selectedTab != null) {
            IPower.getPowerOptional(minecraft.player, selectedTab).ifPresent(power -> {
                if (!power.hasPower()) {
                    selectedTab = null;
                }
            });
        }
        
        powersPresent.clear();
        for (PowerClassification powerClassification : PowerClassification.values()) {
            IPower.getPowerOptional(minecraft.player, powerClassification).ifPresent(power -> {
                PowerClassification firstPresent = null;
                PowerClassification activeHud = null;
                
                if (power.hasPower()) {
                    powersPresent.add(power);
                    if (firstPresent == null) {
                        firstPresent = powerClassification;
                    }
                    if (powerClassification == ActionsOverlayGui.getInstance().getCurrentMode()) {
                        activeHud = powerClassification;
                    }
                }
                
                if (selectedTab == null) {
                    selectedTab = activeHud != null ? activeHud : firstPresent;
                }
            });
        }
        
        if (selectedTab != null && selectedPower == null) {
            selectTab(IPower.getPlayerPower(minecraft.player, selectedTab));
        }
    }
    
    public boolean works() {
        return selectedPower != null && selectedPower.hasPower();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        if (!works()) return;
        renderAfterScissor = null;
        renderBackground(matrixStack, 0);
        hoveredAction = getSlotAt(mouseX, mouseY);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        renderWindow(matrixStack);
        renderTabButtons(matrixStack, mouseX, mouseY);
        renderHotbars(matrixStack, mouseX, mouseY);
        renderKeybindsList(matrixStack, mouseX, mouseY, partialTick);
        renderDragged(matrixStack, mouseX, mouseY);
        renderToolTips(matrixStack, mouseX, mouseY);
        buttons.forEach(button -> button.render(matrixStack, mouseX, mouseY, partialTick));
    }
    

    private void renderWindow(MatrixStack matrixStack) {
        RenderSystem.enableBlend();
        minecraft.getTextureManager().bind(WINDOW);
        blit(matrixStack, getWindowX(), getWindowY(), 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        blit(matrixStack, getWindowX() + 7, getWindowY() + 10, 232, 3, 9, 16);
        blit(matrixStack, getWindowX() + 7, getWindowY() + 36, 232, 39, 9, 16);
        RenderSystem.disableBlend();
    }
    

    private void renderTabButtons(MatrixStack matrixStack, int mouseX, int mouseY) {
        for (int i = 0; i < powersPresent.size(); i++) {
            boolean isTabSelected = isTabSelected(powersPresent.get(i));
            int textureX;
            int textureY;
            if (isTabSelected) {
                textureX = i == 0 ? 0 : 28;
                textureY = 32;
            }
            else {
                textureX = i == 0 ? 168 : 196;
                textureY = 2;
            }
            minecraft.getTextureManager().bind(JojoStuffScreen.TABS);
            int[] xy = getTabButtonCoords(i);
            blit(matrixStack, xy[0], xy[1], textureX, textureY, 28, 32);

            RenderSystem.enableBlend();
            minecraft.getTextureManager().bind(powersPresent.get(i).clGetPowerTypeIcon());
            blit(matrixStack, xy[0] + 6, xy[1] + 10, 0, 0, 16, 16, 16, 16);
            RenderSystem.disableBlend();
        }
        
        if (selectedTab != null) {
            int tabsX = JojoStuffScreen.uniformX(minecraft);
            int tabsY = JojoStuffScreen.uniformY(minecraft);
            JojoStuffScreen.TabsEnumType tabsType = JojoStuffScreen.TabsEnumType.getTabsEnum(selectedPower);
            if (tabsType != null) {
                switch (tabsType) {
                case STAND:
                    JojoStuffScreen.renderStandTabs(matrixStack, 
                            tabsX, tabsY, true, 
                            mouseX, mouseY, this, JojoStuffScreen.StandTab.CONTROLS, 
                            ((IStandPower) selectedPower));
                    break;
                case HAMON:
                    JojoStuffScreen.renderHamonTabs(matrixStack, 
                            tabsX, tabsY, true, 
                            mouseX, mouseY, this, JojoStuffScreen.HamonTab.CONTROLS);
                    break;
                case VAMPIRISM:
                    JojoStuffScreen.renderVampirismTabs(matrixStack, 
                            tabsX, tabsY, true, 
                            mouseX, mouseY, this, JojoStuffScreen.VampirismTab.CONTROLS);
                    break;
                }
            }
        }
    }
    
    private int[] getTabButtonCoords(int tabIndex) {
        int x = getWindowX() + tabIndex * 29;
        int y = getWindowY() - 28;
        return new int[] {x, y};
    }
    
    private boolean isTabSelected(IPower<?, ?> power) {
        return power == selectedPower;
    }

    private static final int HOTBARS_X = 20;
    private static final int ATTACKS_HOTBAR_Y = 10;
    private static final int ABILITIES_HOTBAR_Y = 36;
    private <P extends IPower<P, ?>> void renderHotbars(MatrixStack matrixStack, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        P iSuckAtThis = (P) selectedPower;
        int x = HOTBARS_X + getWindowX();
        renderHotbar(iSuckAtThis, ControlScheme.Hotbar.LEFT_CLICK, matrixStack, x, ATTACKS_HOTBAR_Y + getWindowY(), mouseX, mouseY);
        renderHotbar(iSuckAtThis, ControlScheme.Hotbar.RIGHT_CLICK, matrixStack, x, ABILITIES_HOTBAR_Y + getWindowY(), mouseX, mouseY);
        RenderSystem.disableBlend();
    }
    
    private <P extends IPower<P, ?>> void renderHotbar(P power, ControlScheme.Hotbar hotbar,
            MatrixStack matrixStack, int hotbarX, int hotbarY,
            int mouseX, int mouseY) {
        int i = 0;
        for (ActionVisibilitySwitch actionSwitch : currentControlScheme.getActionsHotbar(hotbar).getLegalActionSwitches()) {
            renderActionSlot(matrixStack, hotbarX + i * 18, hotbarY, mouseX, mouseY, 
                    power, actionSwitch, 
                    draggedAction.isPresent(), 
                    hoveredAction.map(slot -> slot.actionSwitch == actionSwitch).orElse(false), 
                    draggedAction.map(dragged -> dragged.actionSwitch != actionSwitch).orElse(true));
            i++;
        }
        
        plusSlotCoords(hotbar).ifPresent(pos -> {
            renderActionSlot(matrixStack, 
                    pos.x, pos.y, mouseX, mouseY, 
                    power, null, true, 
                    true, getPlusSlotAt(mouseX, mouseY).map(plusHotbar -> {
                        return plusHotbar == hotbar;
                    }).orElse(false), false);
            minecraft.getTextureManager().bind(WINDOW);
            blit(matrixStack, pos.x, pos.y, 64, 220, 18, 18);
        });
    }

    private <P extends IPower<P, ?>> void renderDragged(MatrixStack matrixStack, int mouseX, int mouseY) {
        draggedAction.ifPresent(dragged -> {
            RenderSystem.translatef(0.0F, 0.0F, 32.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            this.setBlitOffset(200);
            ActionVisibilitySwitch actionSwitch = dragged.actionSwitch;
            renderActionIcon(matrixStack, mouseX - 8, mouseY - 8, 
                    (Action<P>) actionSwitch.getAction(), true, (P) selectedPower);
            this.setBlitOffset(0);
            RenderSystem.disableBlend();
            RenderSystem.translatef(0.0F, 0.0F, -32.0F);
        });
    }
    
    private <P extends IPower<P, ?>> void renderActionSlot(MatrixStack matrixStack, 
            int x, int y, int mouseX, int mouseY, 
            P power, ActionVisibilitySwitch actionSwitch, 
            boolean fitsForDragged, boolean isHoveredOver, boolean renderActionIcon) {
        renderActionSlot(matrixStack, 
                x, y, mouseX, mouseY, 
                power, actionSwitch.getAction(), actionSwitch.isEnabled(), 
                fitsForDragged, isHoveredOver, renderActionIcon);
    }
    
    private <P extends IPower<P, ?>> void renderActionSlot(MatrixStack matrixStack, 
            int x, int y, int mouseX, int mouseY, 
            P power, Action<?> action, boolean isEnabled, 
            boolean fitsForDragged, boolean isHoveredOver, boolean renderActionIcon) {
        minecraft.getTextureManager().bind(WINDOW);
        int texX = isHoveredOver ? 82 : 64;
        if (fitsForDragged) {
            texX += 36;
        }
        blit(matrixStack, x, y, texX, 238, 18, 18);

        if (renderActionIcon) {
            renderActionIcon(matrixStack, x + 1, y + 1, (Action<P>) action, isEnabled, power);
        }
    }
    
    private <P extends IPower<P, ?>> void renderActionIcon(MatrixStack matrixStack, 
            int x, int y, Action<P> action, boolean isEnabled, P power) {
        if (action != null) {
            boolean shift = hasShiftDown();
            Action<P> actionResolved = ActionsOverlayGui.resolveVisibleActionInSlot(action, shift, power, ActionTarget.EMPTY);
            if (actionResolved != null) action = actionResolved;
            if (shift) {
                action = action.getShiftVariationIfPresent();
            }
            
            ResourceLocation icon = action.getIconTexture(power);
            minecraft.getTextureManager().bind(icon);
            
            boolean isUnlocked = action.isUnlocked(power);
            float alpha = isEnabled ? isUnlocked ? 1.0F : 0.6F : 0.2F;
            float color = isEnabled && isUnlocked ? 1.0F : 0.0F;
            
            RenderSystem.color4f(color, color, color, alpha);
            blit(matrixStack, x, y, 0, 0, 16, 16, 16, 16);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    
    private Optional<ActionSlot> getSlotAt(int mouseX, int mouseY) {
        mouseX -= getWindowX();
        mouseY -= getWindowY();
        if (mouseX < HOTBARS_X) return Optional.empty();
        
        Optional<ControlScheme.Hotbar> mouseHotbar;
        if (mouseY >= ATTACKS_HOTBAR_Y && mouseY < ATTACKS_HOTBAR_Y + 18) {
            mouseHotbar = Optional.of(ControlScheme.Hotbar.LEFT_CLICK);
        }
        else if (mouseY >= ABILITIES_HOTBAR_Y && mouseY < ABILITIES_HOTBAR_Y + 18) {
            mouseHotbar = Optional.of(ControlScheme.Hotbar.RIGHT_CLICK);
        }
        else {
            mouseHotbar = Optional.empty();
        }
        
        int x = mouseX - HOTBARS_X;
        return mouseHotbar
                .flatMap(hotbar -> {
                    List<ActionVisibilitySwitch> layout = currentControlScheme
                            .getActionsHotbar(hotbar)
                            .getLegalActionSwitches();
                    int slot = x / 18;
                    
                    if (slot >= layout.size()) {
                        return Optional.empty();
                    }
                    ActionVisibilitySwitch action = layout.get(slot);
                    return Optional.of(new ActionSlot(action, hotbar, slot));
                });
    }
    
    
    @Nullable
    private Optional<Vector2i> plusSlotCoords(ControlScheme.Hotbar hotbar) {
        if (draggedAction.isPresent() && draggedAction.get().hotbar != hotbar) {
            int hotbarLength = currentControlScheme.getActionsHotbar(hotbar).getLegalActionSwitches().size();
            if (hotbarLength > 9) {
                return Optional.empty();
            }
            int x = getWindowX() + HOTBARS_X + hotbarLength * 18;
            int y = getWindowY();
            switch (hotbar) {
            case LEFT_CLICK:
                y += ATTACKS_HOTBAR_Y;
                break;
            case RIGHT_CLICK:
                y += ABILITIES_HOTBAR_Y;
                break;
            default:
                return Optional.empty();
            }
            return Optional.of(new Vector2i(x, y));
        }
        
        return Optional.empty();
    }
    
    private Optional<ControlScheme.Hotbar> getPlusSlotAt(int mouseX, int mouseY) {
        for (ControlScheme.Hotbar hotbar : ControlScheme.Hotbar.values()) {
            Optional<Vector2i> coords = plusSlotCoords(hotbar);
            if (coords.isPresent()) {
                int x = coords.get().x;
                int y = coords.get().y;
                if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) {
                    return Optional.of(hotbar);
                }
            }
        }

        return Optional.empty();
    }
    
    
    
    private Runnable renderAfterScissor = null;
    private void renderToolTips(MatrixStack matrixStack, int mouseX, int mouseY) {
        if (draggedAction.isPresent()) return;
        int tab = getTabButtonAt(mouseX, mouseY);
        if (tab >= 0 && tab < powersPresent.size()) {
            renderTooltip(matrixStack, powersPresent.get(tab).getName(), mouseX, mouseY);
        }
        else {
            hoveredAction.ifPresent(slot -> {
                List<ITextComponent> tooltip = new ArrayList<>();
                
                IFormattableTextComponent name = getActionName(selectedPower, slot.actionSwitch.getAction());
                if (!slot.actionSwitch.isEnabled()) {
                    name.withStyle(TextFormatting.STRIKETHROUGH);
                }
                tooltip.add(name);
                
                tooltip.add(StringTextComponent.EMPTY);
                tooltip.add(new TranslationTextComponent("jojo.screen.edit_hud_layout.hint.lmb").withStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
                tooltip.add(new TranslationTextComponent("jojo.screen.edit_hud_layout.hint.rmb").withStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
                
                renderComponentTooltip(matrixStack, tooltip, mouseX, mouseY);
            });
            
            keybindsList.getHoveredKeybindSlot().ifPresent(slot -> {
                if (slot.getAction() != null) {
                    IFormattableTextComponent name = getActionName(selectedPower, slot.getAction());
                    renderTooltip(matrixStack, name, mouseX, mouseY);
                }
            });
        }
        
        if (renderAfterScissor != null) {
            renderAfterScissor.run();
            renderAfterScissor = null;
        }
    }
    
    private <P extends IPower<P, ?>> IFormattableTextComponent getActionName(IPower<?, ?> power, Action<P> action) {
        return getActionName((P) power, action, hasShiftDown());
    }
    
    public static <P extends IPower<P, ?>> IFormattableTextComponent getActionName(P power, Action<P> action, boolean shift) {
        Action<P> actionReplacing = ActionsOverlayGui.resolveVisibleActionInSlot(action, shift, power, ActionTarget.EMPTY);
        if (actionReplacing != null) {
            action = actionReplacing;
        }
        IFormattableTextComponent name;
        
        if (action.isUnlocked(power)) {
            name = action.getTranslatedName(power, action.getTranslationKey(power, ActionTarget.EMPTY));
        }
        
        else {
            name = action.getNameLocked(power).withStyle(TextFormatting.DARK_GRAY);
        }
        
        return name;
    }
    
    private int getTabButtonAt(int mouseX, int mouseY) {
        mouseX -= getWindowX();
        mouseY -= getWindowY();
        if (mouseY > -28 && mouseY < 0 && mouseX >= 0) {
            int tab = mouseX / 29;
            return tab;
        }
        return -1;
    }
    
    private int getWindowX() { return (width - WINDOW_WIDTH) / 2; }
    private int getWindowY() { return (height - WINDOW_HEIGHT) / 2; }

    @Override
    public boolean mouseClicked(double mouseXd, double mouseYd, int mouseButton) {
        MouseButton button = MouseButton.getButtonFromId(mouseButton);
        
        int mouseX = (int) mouseXd;
        int mouseY = (int) mouseYd;
        Optional<ActionSlot> clickedActionSlot = getSlotAt(mouseX, mouseY);
        
        if (button == MouseButton.LEFT) {
            if (draggedAction.isPresent()) {
                ActionSlot dragged = draggedAction.get();
                Optional<ControlScheme.Hotbar> plusSlot = getPlusSlotAt(mouseX, mouseY);
                
                if (clickedActionSlot.isPresent()) {
                    ActionSlot clicked = clickedActionSlot.get();
                    // move action to another position
                    if (dragged.hotbar == clicked.hotbar) {
                        currentControlScheme.getActionsHotbar(clicked.hotbar).moveTo(dragged.actionSwitch, clicked.index);
                    }
                    // move action to the other hotbar
                    else {
                        currentControlScheme.getActionsHotbar(dragged.hotbar).remove(dragged.actionSwitch);
                        currentControlScheme.getActionsHotbar(clicked.hotbar).addTo(dragged.actionSwitch, clicked.index);
                    }
                    markLayoutEdited();
                }
                
                // move action to the end of the other hotbar
                else if (plusSlot.isPresent()) {
                    currentControlScheme.getActionsHotbar(dragged.hotbar).remove(dragged.actionSwitch);
                    ActionsHotbar hotbarAddedTo = currentControlScheme.getActionsHotbar(plusSlot.get());
                    hotbarAddedTo.addTo(dragged.actionSwitch, hotbarAddedTo.getLegalActionSwitches().size());
                    draggedAction = Optional.empty();
                    markLayoutEdited();
                }
                
                else {
                    Optional<ActionKeybindEntry> clickedKeybindActionSlot = keybindsList.getHoveredKeybindSlot();
                    if (clickedKeybindActionSlot.isPresent()) {
                        ActionKeybindEntry slot = clickedKeybindActionSlot.get();
                        slot.setAction(dragged.actionSwitch.getAction());
                        markLayoutEdited();
                    }
                }
    
                draggedAction = Optional.empty();
                return true;
            }
        }
        
        
        int tab = getTabButtonAt(mouseX, mouseY);
        if (tab >= 0 && tab < powersPresent.size()) {
            selectTab(powersPresent.get(tab));
            return true;
        }
        
        JojoStuffScreen.TabsEnumType tabsType = JojoStuffScreen.TabsEnumType.getTabsEnum(selectedPower);
        if (tabsType != null && JojoStuffScreen.mouseClick(mouseX, mouseY, 
                JojoStuffScreen.uniformX(minecraft), JojoStuffScreen.uniformY(minecraft), tabsType)) {
            return true;
        }
        
        if (clickedActionSlot.isPresent()) {
            ControlScheme.Hotbar hotbar = clickedActionSlot.get().hotbar;
            if (button != null) {
                switch (button) {
                case LEFT:
                    draggedAction = clickedActionSlot;
                    return true;
                case RIGHT:
                    ActionVisibilitySwitch slot = clickedActionSlot.get().actionSwitch;
                    slot.setIsEnabled(!slot.isEnabled());
                    markLayoutEdited();

                    if (slot.isEnabled() && selectedPower == ActionsOverlayGui.getInstance().getCurrentPower()
                            && isActionVisible(slot.getAction(), selectedPower)) {
                        int slotIndex = currentControlScheme.getActionsHotbar(hotbar).getEnabledActions().indexOf(slot.getAction());
                        if (slotIndex >= 0) {
                            ActionsOverlayGui.getInstance().selectAction(hotbar, slotIndex);
                        }
                    }
                    return true;
                default:
                    break;
                }
            }
            setCustomKeybind(clickedActionSlot.get().actionSwitch.getAction(), InputMappings.Type.MOUSE, mouseButton);
            return true;
        }
        
        return mouseClickedEditingKeybind(mouseButton, /*KeyModifier.getActiveModifier()*/ KeyModifier.NONE)
                || super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    public void selectTab(PowerClassification power) {
        selectTab(IPower.getPlayerPower(Minecraft.getInstance().player, power));
    }
    
    private void selectTab(IPower<?, ?> power) {
        if (power != null && power.hasPower()) {
            selectedPower = power;
            selectedTab = power.getPowerClassification();
            
            clearInvalidKeybinds();
            currentControlsScreen = HudControlSettings.getInstance().getCachedControls(selectedTab);
            currentControlScheme = currentControlsScreen.getCurrentCtrlScheme();
            refreshCustomKeybindEntries();
        }
    }
    
    private void refreshCustomKeybindEntries() {
        keybindsList.clear();
        selectedKey.clear();
        for (ActionKeybindEntry keybindEntry : currentControlScheme.getCustomKeybinds()) {
            _addKeybindEntryToUi(keybindEntry);
        }
    }
    
    @Nullable
    private final KeyBinding getSelectedKey() {
        return selectedKey.getKeybind();
    }
    
    private <P extends IPower<P, ?>> boolean isActionVisible(Action<P> action, IPower<?, ?> power) {
        return action.getVisibleAction((P) power, ActionTarget.EMPTY) != null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (hoveredAction.isPresent()) {
            ControlScheme.Hotbar hotbar = hoveredAction.get().hotbar;
            if (hotbar != null) {
                ActionsOverlayGui.getInstance().scrollAction(hotbar, scroll > 0);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }
    
    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (InputHandler.getInstance().editHotbars.matches(key, scanCode)) {
            onClose();
            return true;
        } 
        else {
            int numKey = getNumKey(key, scanCode);
            if (numKey > -1) {
                Optional<ActionSlot> toMove = draggedAction;
                if (!toMove.isPresent()) toMove = hoveredAction;
                if (toMove.map(action -> {
                    // moves action to specific slot after pressing number key
                    ActionsHotbar hotbar = currentControlScheme.getActionsHotbar(action.hotbar);
                    if (numKey < hotbar.getLegalActionSwitches().size()) {
                        hotbar.moveTo(action.actionSwitch, numKey);
                        markLayoutEdited();
                        return true;
                    }
                    return false;
                }).orElse(false)) {
                    if (draggedAction.isPresent()) {
                        draggedAction = Optional.empty();
                    }
                    return true;
                }
            }
            else if (key != GLFW.GLFW_KEY_ESCAPE && hoveredAction.isPresent()) {
                setCustomKeybind(hoveredAction.get().actionSwitch.getAction(), InputMappings.Type.KEYSYM, key);
            }
        }
        
        return keyPressedEditingKeybind(key, scanCode, /*KeyModifier.getActiveModifier()*/ KeyModifier.NONE)
                || super.keyPressed(key, scanCode, modifiers);
    }
    
    private int getNumKey(int key, int scanCode) {
        for (int i = 0; i < 9; ++i) {
            if (minecraft.options.keyHotbarSlots[i].isActiveAndMatches(InputMappings.getKey(key, scanCode))) {
                return i;
            }
        }
        return -1;
    }
    
    private void markLayoutEdited() {
        editedLayouts.add(currentControlsScreen.powerTypeId);
    }
    
    @Override
    public void onClose() {
        super.onClose();
        ActionsOverlayGui.getInstance().revealActionNames();
        clearInvalidKeybinds();
        HudControlSettings controlsSave = HudControlSettings.getInstance();
        editedLayouts.forEach(controlsSave::saveForPowerType);
    }
    
    private static class ActionSlot {
        private final ActionVisibilitySwitch actionSwitch;
        private final ControlScheme.Hotbar hotbar;
        private final int index;
        
        private ActionSlot(ActionVisibilitySwitch actionSwitch, ControlScheme.Hotbar hotbar, int index) {
            this.actionSwitch = actionSwitch;
            this.hotbar = hotbar;
            this.index = index;
        }
    }
    
    
    
    
    
    private void renderKeybindsList(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        keybindsList.render(matrixStack, mouseX, mouseY, partialTick);
    }
    
    private boolean mouseClickedEditingKeybind(int buttonId, KeyModifier keyModifier) {
        if (!selectedKey.isEmpty()) {
            selectedKey.setKeyModifierAndCode(keyModifier, InputMappings.Type.MOUSE.getOrCreate(buttonId));
            
            if (selectedKey.getCustomActionKeybind() != null) {
                markKeybindEdited(selectedKey.getCustomActionKeybind());
                markLayoutEdited();
            }
            
            selectedKey.clear();
            KeyBinding.resetMapping();
            markLayoutEdited();
            return true;
        }
        
        return false;
    }
    
    private boolean keyPressedEditingKeybind(int keyCode, int scanCode, KeyModifier keyModifier) {
        if (!selectedKey.isEmpty()) {
            if (keyCode == 256) {
                selectedKey.setKeyModifierAndCode(keyModifier, InputMappings.UNKNOWN);
            } else {
                selectedKey.setKeyModifierAndCode(keyModifier, InputMappings.getKey(keyCode, scanCode));
            }
            
            if (selectedKey.getCustomActionKeybind() != null) {
                markKeybindEdited(selectedKey.getCustomActionKeybind());
                markLayoutEdited();
            }
            
            if (!KeyModifier.isKeyCodeModifier(selectedKey.getKeybind().getKey())) {
                selectedKey.clear();
            }
            KeyBinding.resetMapping();
            return true;
        }
        
        return false;
    }
    
    private void createBlankKeybindEntry() {
        ActionKeybindEntry entry = currentControlScheme.addBlankKeybindEntry();
        markKeybindEdited(entry);
        markLayoutEdited();
        _addKeybindEntryToUi(entry);
    }
    
    private void setCustomKeybind(Action<?> action, InputMappings.Type inputType, int key) {
        Optional<ActionKeybindEntry> actionAlreadyHasKey = keybindsList.getKeys()
                .filter(entry -> {
                    return entry.getAction() == action;
                })
                .findFirst();
        
        markLayoutEdited();
        
        if (actionAlreadyHasKey.isPresent()) {
            actionAlreadyHasKey.get().setKeybind(inputType, key);
            return;
        }
        else {
            ActionKeybindEntry entry = currentControlScheme.addKeybindEntry(action, inputType, key);
            markKeybindEdited(entry);
            _addKeybindEntryToUi(entry);
        }

    }
    
    private void _addKeybindEntryToUi(ActionKeybindEntry entry) {
        Widget keyBindingButton = new Button(
                -1, -1, 
                95, 20, entry.getKeybind().getTranslatedKeyMessage(), button -> {
            HudLayoutEditingScreen.this.selectedKey.setKeybind(entry);
        }) {
//            @Override
//            protected IFormattableTextComponent createNarrationMessage() {
//                if (entry.action != null) {
//                    ITextComponent actionName = getActionName(selectedPower, entry.action, shift);
//                    if (entry.keybind.isUnbound()) {
//                        return new TranslationTextComponent("narrator.controls.unbound", actionName);
//                    }
//                    else {
//                        return new TranslationTextComponent("narrator.controls.bound", actionName, super.createNarrationMessage());
//                    }
//                }
//                
//                return super.createNarrationMessage();
//            }
        };

        Widget keyPressModeButton = new CustomButton(
                -1, -1, 
                13, 13, StringTextComponent.EMPTY, button -> {
                    markLayoutEdited();
                    entry.setOnPress(GeneralUtil.nextEnumValCycle(entry.getOnKeyPress()));
                }, 
                (button, matrixStack, x, y) -> {
                    renderAfterScissor = () -> {
                        List<ITextComponent> tooltip = new ArrayList<>(2);
                        tooltip.add(new TranslationTextComponent("jojo.keybind_mode.key_press.title").withStyle(TextFormatting.BOLD));
                        tooltip.add(new TranslationTextComponent("jojo.keybind_mode.key_press." + entry.getOnKeyPress().name().toLowerCase()));
                        renderComponentTooltip(matrixStack, tooltip, x, y);
                    };
                }) {
            @Override
            protected void renderCustomButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.getTextureManager().bind(WINDOW);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                int texX = 188 + entry.getOnKeyPress().ordinal() * width;
                int texY = 217;
                blit(matrixStack, x, y, texX, texY + getYImage(isHovered()) * height, width, height);
            }
        };
        
        Widget keyActiveModeButton = new CustomButton(
                -1, -1, 
                13, 13, StringTextComponent.EMPTY, button -> {
                    markLayoutEdited();
                    entry.setHudInteraction(GeneralUtil.nextEnumValCycle(entry.getHudInteraction()));
                }, 
                (button, matrixStack, x, y) -> {
                    renderAfterScissor = () -> {
                        List<ITextComponent> tooltip = new ArrayList<>(2);
                        tooltip.add(new TranslationTextComponent("jojo.keybind_mode.is_active.title").withStyle(TextFormatting.BOLD));
                        tooltip.add(new TranslationTextComponent("jojo.keybind_mode.is_active." + entry.getHudInteraction().name().toLowerCase(), 
                                selectedPower.getName()));
                        renderComponentTooltip(matrixStack, tooltip, x, y);
                    };
                }) {
            @Override
            protected void renderCustomButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.getTextureManager().bind(WINDOW);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                int texX = 216 + entry.getHudInteraction().ordinal() * width;
                int texY = 217;
                blit(matrixStack, x, y, texX, texY + getYImage(isHovered()) * height, width, height);
            }
        };
        
        Widget keyHudVisibilityButton = new CustomButton(
                -1, -1, 
                10, 10, StringTextComponent.EMPTY, button -> {
                    markLayoutEdited();
                    entry.setVisibleInHud(!entry.isVisibleInHud());
                }, 
                (button, matrixStack, x, y) -> {
                    renderAfterScissor = () -> {
                        List<ITextComponent> tooltip = new ArrayList<>(2);
                        tooltip.add(new TranslationTextComponent("jojo.keybind_mode.hud_visibility." + String.valueOf(entry.isVisibleInHud())).withStyle(TextFormatting.BOLD));
                        renderComponentTooltip(matrixStack, tooltip, x, y);
                    };
                }) {
            @Override
            protected void renderCustomButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.getTextureManager().bind(WINDOW);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                int texX = entry.isVisibleInHud() ? 236 : 246;
                blit(matrixStack, x, y, texX, 192 + getYImage(isHovered()) * height, width, height);
            }
        };
        
        Widget removeButton = new CustomButton(
                -1, -1, 
                8, 8, StringTextComponent.EMPTY, button -> {
            if (currentControlScheme.removeKeybindEntry(entry)) {
                markLayoutEdited();
                keybindsList.removeByKey(entry);
            }
        }) {
            @Override
            protected void renderCustomButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.getTextureManager().bind(WINDOW);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                blit(matrixStack, x, y, 144, 192 + getYImage(isHovered()) * height, width, height);
            }
        };
        
        keybindsList.addKeybindEntry(keybindsList.new KeybindUIEntry(entry, 
                keyBindingButton, keyPressModeButton, keyActiveModeButton, keyHudVisibilityButton, removeButton));
    }
    
    private Map<ActionKeybindEntry, ControlScheme> editedKeybinds = new HashMap<>();
    private void markKeybindEdited(ActionKeybindEntry entry) {
        if (entry != null) {
            editedKeybinds.put(entry, currentControlScheme);
        }
    }
    
    private void clearInvalidKeybinds() {
        Set<ControlScheme> ctrlSchemes = new HashSet<>();
        editedKeybinds.forEach((key, ctrlScheme) -> {
            if (!key.isValid() || key.getKeybind().isUnbound()) {
                ctrlScheme.removeKeybindEntry(key);
            }
            ctrlSchemes.add(ctrlScheme);
        });
        editedKeybinds.clear();
    }
    
    
    
    
    
    public static class ActionKeybindsList extends AbstractOptionList<ActionKeybindsList.Entry> {
        private final Map<ActionKeybindEntry, ActionKeybindsList.KeybindUIEntry> keybindsMap = new HashMap<>();
        private final HudLayoutEditingScreen screen;
        private final SelectedKey selectedKeyHolder;

        public ActionKeybindsList(Minecraft mc, int width, int height, 
                int y0, int y1, int itemHeight, 
                HudLayoutEditingScreen screen, SelectedKey selectedKeyHolder) {
            super(mc, width, height, y0, y1, itemHeight);
            this.screen = screen;
            this.selectedKeyHolder = selectedKeyHolder;
            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
            addNewKeybindKey();
        }
        
        private final Collection<KeyBinding> vanillaKeys = Arrays.asList(minecraft.options.keyMappings);
        private final Collection<KeyBinding> entryKeys = new ArrayList<>();
        private final Iterable<KeyBinding> conflictKeys = Iterables.concat(vanillaKeys, entryKeys);
        private Optional<ActionKeybindEntry> hoveredKeybindSlot = Optional.empty();
        @Override
        protected void renderList(MatrixStack pMatrixStack, int pX, int pY, int pMouseX, int pMouseY, float pPartialTicks) {
            entryKeys.clear();
            for (ActionKeybindsList.KeybindUIEntry entry : keybindsMap.values()) {
                entryKeys.add(entry.keybindEntry.getKeybind());
            }
            hoveredKeybindSlot = Optional.empty();
            super.renderList(pMatrixStack, pX, pY, pMouseX, pMouseY, pPartialTicks);
        }
        
        public void addKeybindEntry(ActionKeybindsList.KeybindUIEntry entry) {
            ActionKeybindEntry keybind = entry.keybindEntry;
            if (keybindsMap.containsKey(keybind)) {
                return;
            }
            keybindsMap.put(keybind, entry);
            
            this.children().add(children().size() - 1, entry);
        }
        
        public boolean removeByKey(ActionKeybindEntry key) {
            ActionKeybindsList.KeybindUIEntry entry = keybindsMap.remove(key);
            if (entry != null) {
                boolean removed = removeEntry(entry);
                setScrollAmount(getScrollAmount());
                return removed;
            }
            return false;
        }
        
        public void clear() {
            super.clearEntries();
            keybindsMap.clear();
            addNewKeybindKey();
            setScrollAmount(getScrollAmount());
        }
        
        private void addNewKeybindKey() {
            super.addEntry(new AddNewKeyEntry(new CustomButton(screen.getWindowX() + 10, screen.getWindowY() + 64, 20, 20, 
                    button -> {
                        screen.createBlankKeybindEntry();
                        screen.markLayoutEdited();
                    }, 
                    (button, matrixStack, x, y) -> {
                        screen.renderTooltip(matrixStack, new TranslationTextComponent("jojo.screen.edit_hud_layout.add_keybind"), x, y);
                    }) {
                
                @Override
                protected void renderCustomButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
                    Minecraft minecraft = Minecraft.getInstance();
                    minecraft.getTextureManager().bind(WINDOW);
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.enableDepthTest();
                    blit(matrixStack, x, y, 144, 196 + getYImage(isHovered()) * height, width, height);
                }
            }));
        }
        
        @Override
        public int getItemCount() {
            return super.getItemCount();
        }
        
        public Stream<ActionKeybindEntry> getKeys() {
            return keybindsMap.keySet().stream();
        }
        
        @Override
        public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
            ClientUtil.enableGlScissor(x0, y0, x1 - x0, y1 - y0);
            super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
            ClientUtil.disableGlScissor();
        }
        
        @Override
        protected int getScrollbarPosition() {
            return x1 - 50;
        }
        
        @Override
        public int getRowLeft() {
            return x0 + 2;
        }
        
        public Optional<ActionKeybindEntry> getHoveredKeybindSlot() {
            return hoveredKeybindSlot;
        }

        @Override
        public int getRowWidth() {
            return width;
        }
        
        
        

        public abstract class Entry extends AbstractOptionList.Entry<ActionKeybindsList.Entry> {}
        
        public class KeybindUIEntry extends ActionKeybindsList.Entry {
            private final ActionKeybindEntry keybindEntry;
            private final List<Widget> buttons;
            private final Widget keybindButton;
            private final Widget keyPressModeButton;
            private final Widget keyActiveModeButton;
            private final Widget hudVisibilityButton;
            private final Widget removeButton;
            private int actionSlotX;
            private int actionSlotY;
            
            public KeybindUIEntry(ActionKeybindEntry keybindEntry, 
                    Widget keybindButton, Widget keyPressModeButton, Widget keyActiveModeButton, Widget hudVisibilityButton, Widget removeButton) {
                this.keybindEntry = keybindEntry;
                this.keybindButton = keybindButton;
                this.keyPressModeButton = keyPressModeButton;
                this.keyActiveModeButton = keyActiveModeButton;
                this.hudVisibilityButton = hudVisibilityButton;
                this.removeButton = removeButton;
                this.buttons = ImmutableList.of(keybindButton, keyPressModeButton, keyActiveModeButton, hudVisibilityButton, removeButton);
            }
            
            @Override
            public void render(MatrixStack matrixStack, int index, int top, int left, 
                    int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
                KeyBinding keybind = keybindEntry.getKeybind();
                
                keybindButton.x = left + 22;
                keybindButton.y = top;
                keybindButton.setMessage(keybind.getTranslatedKeyMessage());
                
                keyPressModeButton.x = keybindButton.x + keybindButton.getWidth() + 8;
                keyPressModeButton.y = top + 3;
                
                keyActiveModeButton.x = keyPressModeButton.x + keyPressModeButton.getWidth() + 2;
                keyActiveModeButton.y = top + 3;
                
                hudVisibilityButton.x = keyActiveModeButton.x + keyActiveModeButton.getWidth() + 4;
                hudVisibilityButton.y = top + 4;
                
                removeButton.x = left + width - 68;
                removeButton.y = top + 5;
                
                actionSlotX = left;
                actionSlotY = top + 1;
                
                if (mouseX >= actionSlotX && mouseX < actionSlotX + 18 && 
                        mouseY >= actionSlotY && mouseY < actionSlotY + 18) {
                    hoveredKeybindSlot = Optional.of(keybindEntry);
                }
                
                boolean conflicting = false;
                boolean keyCodeModifierConflict = true; // less severe form of conflict, like SHIFT conflicting with SHIFT+G
                if (!keybind.isUnbound()) {
                    for (KeyBinding otherKey : conflictKeys) {
                        if (otherKey != keybind && keybind.same(otherKey)) {
                            conflicting = true;
                            keyCodeModifierConflict &= otherKey.hasKeyCodeModifierConflict(keybind);
                        }
                    }
                }
                
                if (!selectedKeyHolder.isEmpty() && selectedKeyHolder.getKeybind() == this.keybindEntry.getKeybind()) {
                    this.keybindButton.setMessage(
                            new StringTextComponent("> ")
                            .append(this.keybindButton.getMessage().copy().withStyle(TextFormatting.YELLOW))
                            .append(" <").withStyle(TextFormatting.YELLOW));
                } else if (conflicting) {
                    this.keybindButton.setMessage(
                            this.keybindButton.getMessage().copy()
                            .withStyle(keyCodeModifierConflict ? TextFormatting.GOLD : TextFormatting.RED));
                }
                
                for (Widget button : buttons) {
                    button.render(matrixStack, mouseX, mouseY, partialTicks);
                }
                
                renderActionSlot(matrixStack, mouseX, mouseY);
            }

            // TODO render keybind slots
            private <P extends IPower<P, ?>> void renderActionSlot(MatrixStack matrixStack, double mouseX, double mouseY) {
                Action<?> action = keybindEntry.getAction();
                screen.renderActionSlot(matrixStack, actionSlotX, actionSlotY, (int) mouseX, (int) mouseY, 
                        (P) screen.selectedPower, action, true, 
                        screen.draggedAction.isPresent(), 
                        hoveredKeybindSlot.map(hovered -> hovered == keybindEntry).orElse(false),
                        action != null);
            }

            @Override
            public List<? extends IGuiEventListener> children() {
                return buttons;
            }

            @Override
            public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
                for (Widget button : buttons) {
                    if (button.mouseClicked(pMouseX, pMouseY, pButton)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
                for (Widget button : buttons) {
                    if (button.mouseReleased(pMouseX, pMouseY, pButton)) {
                        return true;
                    }
                }
                return false;
            }

        }
        
        public class AddNewKeyEntry extends ActionKeybindsList.Entry {
            private final Button addNewKeybindButton;
            
            public AddNewKeyEntry(Button addNewKeybindButton) {
                this.addNewKeybindButton = addNewKeybindButton;
            }
            
            @Override
            public List<? extends IGuiEventListener> children() {
                return Collections.singletonList(addNewKeybindButton);
            }

            @Override
            public void render(MatrixStack matrixStack, int index, int top, int left, int width, int height,
                    int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
                addNewKeybindButton.x = left;
                addNewKeybindButton.y = top;
                addNewKeybindButton.render(matrixStack, mouseX, mouseY, partialTicks);
            }
        }
    }
}
