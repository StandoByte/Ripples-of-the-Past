package com.github.standobyte.jojo.client.ui.screen.controls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.InputHandler.MouseButton;
import com.github.standobyte.jojo.client.controls.ActionKeybindEntry;
import com.github.standobyte.jojo.client.controls.ActionKeybindEntry.PressActionType;
import com.github.standobyte.jojo.client.controls.ActionVisibilitySwitch;
import com.github.standobyte.jojo.client.controls.ActionsHotbar;
import com.github.standobyte.jojo.client.controls.ControlScheme;
import com.github.standobyte.jojo.client.controls.HudControlSettings;
import com.github.standobyte.jojo.client.controls.PowerTypeControlSchemes;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.screen.widgets.CustomButton;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.github.standobyte.jojo.util.general.Vector2i;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ControlsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
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
    private static final int WINDOW_WIDTH = 230;
    private static final int WINDOW_HEIGHT = 180;
    
    private static PowerClassification selectedTab = null;
    private IPower<?, ?> selectedPower;
    private List<IPower<?, ?>> powersPresent = new ArrayList<>();
    
    private Optional<ActionSlot> draggedAction = Optional.empty();
    private Optional<ActionSlot> hoveredAction = Optional.empty();
    
    private PowerTypeControlSchemes currentControlsScreen;
    private ControlScheme currentControlScheme;
    private Set<ResourceLocation> editedLayouts = new HashSet<>();
    
    private Widget addKeybindButton;
    
    public static Predicate<KeyBindingList.Entry> scrollCtrlListTo = null;

    public HudLayoutEditingScreen() {
        super(new TranslationTextComponent("jojo.screen.edit_hud_layout"));
    }
    
    @Override
    protected void init() {
        // reset layout
        addButton(new CustomButton(getWindowX() + WINDOW_WIDTH - 26, getWindowY() + 21, 20, 20, 
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
                blit(matrixStack, x, y, 0, 196 + getYImage(isHovered()) * height, width, height);
            }
        });
        
        // add keybind
        addButton(addKeybindButton = new CustomButton(getWindowX() + 10, getWindowY() + 64, 20, 20, 
                button -> {
                    createBlankKeybindEntry();
                    markLayoutEdited();
                }, 
                (button, matrixStack, x, y) -> {
                    renderTooltip(matrixStack, new TranslationTextComponent("jojo.screen.edit_hud_layout.add_keybind"), x, y);
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
        });
        
        // vanilla controls settings
        addButton(new CustomButton(getWindowX() - 26, getWindowY() + WINDOW_HEIGHT - 26, 22, 22, 
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
                if (power.hasPower()) {
                    powersPresent.add(power);
                    if (selectedTab == null || powerClassification == ActionsOverlayGui.getInstance().getCurrentMode()) {
                        selectTab(power);
                    }
                }
            });
        }

        if (selectedTab != null && selectedPower == null) {
            selectTab(IPower.getPlayerPower(minecraft.player, selectedTab));
        }
        
        updateKeybindEntriesPos();
    }
    
    public boolean works() {
        return selectedPower != null && selectedPower.hasPower();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        if (!works()) return;
        renderBackground(matrixStack, 0);
        hoveredAction = getSlotAt(mouseX, mouseY);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        renderTabButtons(matrixStack, false);
        renderWindow(matrixStack);
        renderTabButtons(matrixStack, true);
        renderHint(matrixStack);
        renderSlots(matrixStack, mouseX, mouseY);
        renderDragged(matrixStack, mouseX, mouseY);
        renderKeybindsList(matrixStack, mouseX, mouseY, partialTick);
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
    

    private void renderTabButtons(MatrixStack matrixStack, boolean renderSelectedTabButton) {
        for (int i = 0; i < powersPresent.size(); i++) {
            boolean isTabSelected = isTabSelected(powersPresent.get(i));
            if (isTabSelected ^ renderSelectedTabButton) continue;
            int textureX = i == 0 ? 200 : 228;
            int textureY = isTabSelected ? 224 : 192;
            minecraft.getTextureManager().bind(WINDOW);
            int[] xy = getTabButtonCoords(i);
            blit(matrixStack, xy[0], xy[1], textureX, textureY, 28, 32);

            RenderSystem.enableBlend();
            minecraft.getTextureManager().bind(powersPresent.get(i).clGetPowerTypeIcon());
            blit(matrixStack, xy[0] + 6, xy[1] + 10, 0, 0, 16, 16, 16, 16);
            RenderSystem.disableBlend();
            if (renderSelectedTabButton) break;
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
    private <P extends IPower<P, ?>> void renderSlots(MatrixStack matrixStack, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        P iSuckAtThis = (P) selectedPower;
        int x = HOTBARS_X + getWindowX();
        renderHotbar(iSuckAtThis, ControlScheme.Hotbar.LEFT_CLICK, matrixStack, x, ATTACKS_HOTBAR_Y + getWindowY(), mouseX, mouseY);
        renderHotbar(iSuckAtThis, ControlScheme.Hotbar.RIGHT_CLICK, matrixStack, x, ABILITIES_HOTBAR_Y + getWindowY(), mouseX, mouseY);
        renderKeybindSlots(iSuckAtThis, matrixStack, mouseX, mouseY);
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
    
    
    
    private void renderHint(MatrixStack matrixStack) {
        minecraft.getTextureManager().bind(WINDOW);
        int hintX = getWindowX() + WINDOW_WIDTH - 17;
        int hintY = getWindowY() + 6;
        blit(matrixStack, hintX, hintY, 32, 245, 11, 11);
    }
    
    private final List<ITextComponent> hintTooltip = ImmutableList.of(
            new TranslationTextComponent("jojo.screen.edit_hud_layout.hint.lmb"), 
            new TranslationTextComponent("jojo.screen.edit_hud_layout.hint.rmb"));
    private void renderToolTips(MatrixStack matrixStack, int mouseX, int mouseY) {
        if (draggedAction.isPresent()) return;
        int tab = getTabButtonAt(mouseX, mouseY);
        if (tab >= 0 && tab < powersPresent.size()) {
            renderTooltip(matrixStack, powersPresent.get(tab).getName(), mouseX, mouseY);
        }
        else {
            renderActionNameTooltip(matrixStack, mouseX, mouseY);
        }
        
        int hintX = getWindowX() + WINDOW_WIDTH - 17;
        int hintY = getWindowY() + 6;
        if (mouseX >= hintX && mouseX < hintX + 11 && mouseY >= hintY && mouseY < hintY + 11) {
            renderComponentTooltip(matrixStack, hintTooltip, mouseX, mouseY);
        }
    }
    
    private <P extends IPower<P, ?>> void renderActionNameTooltip(MatrixStack matrixStack, int mouseX, int mouseY) {
        P power = (P) selectedPower;
        hoveredAction.ifPresent(slot -> {
            renderActionName(matrixStack, power, (Action<P>) slot.actionSwitch.getAction(), mouseX, mouseY, slot.actionSwitch.isEnabled());
        });
    }
    
    private <P extends IPower<P, ?>> void renderActionName(MatrixStack matrixStack, P power, Action<P> action, int mouseX, int mouseY, boolean isEnabled) {
        if (action == null) return;
        IFormattableTextComponent name = getActionName(power, action, hasShiftDown());
        
        if (!isEnabled) {
            name.withStyle(TextFormatting.DARK_GRAY);
        }
        renderTooltip(matrixStack, name, mouseX, mouseY);
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
            name = action.getNameLocked(power).withStyle(TextFormatting.GRAY, TextFormatting.ITALIC);
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
        if (button == null) return false;
        int mouseX = (int) mouseXd;
        int mouseY = (int) mouseYd;
        Optional<ActionSlot> clickedActionSlot = getSlotAt(mouseX, mouseY);
        
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
                Optional<ActionKeybindEntry> clickedKeybindActionSlot = getKeybindSlotAt(mouseX, mouseY);
                if (clickedKeybindActionSlot.isPresent()) {
                    ActionKeybindEntry slot = clickedKeybindActionSlot.get();
                    slot.setAction(dragged.actionSwitch.getAction());
                    markLayoutEdited();
                }
            }

            draggedAction = Optional.empty();
            return true;
        }
        
        
        int tab = getTabButtonAt(mouseX, mouseY);
        if (tab >= 0 && tab < powersPresent.size()) {
            selectTab(powersPresent.get(tab));
            return true;
        }
        if (clickedActionSlot.isPresent()) {
            ControlScheme.Hotbar hotbar = clickedActionSlot.get().hotbar;
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
            case MIDDLE:
                setCustomKeybind(clickedActionSlot.get().actionSwitch.getAction(), InputMappings.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
                return true;
            default:
                return false;
            }
        }
        
        return mouseClickedEditingKeybind(mouseButton, /*KeyModifier.getActiveModifier()*/ KeyModifier.NONE)
                || super.mouseClicked(mouseX, mouseY, mouseButton);
    }

//    private List<KeyBindingList.Entry> registeredKeys = new ArrayList<>();
    private void selectTab(IPower<?, ?> power) {
        if (power != null && power.hasPower()) {
            selectedPower = power;
            selectedTab = power.getPowerClassification();
            
            clearInvalidKeybinds();
            currentControlsScreen = HudControlSettings.getInstance().getCachedControls(selectedTab);
            currentControlScheme = currentControlsScreen.getCurrentCtrlScheme();
            refreshCustomKeybindEntries();
            
            
            
//            registeredKeys.forEach(entry -> {
//                for (IGuiEventListener button : entry.children()) {
////                    buttons.remove(button);
////                    children.remove(button);
//                    JojoMod.LOGGER.debug(buttons.remove(button) ? "removed button" : "nope");
//                    JojoMod.LOGGER.debug(children.remove(button) ? "removed child" : "nuh-uh");
//                }
//            });
//            registeredKeys.clear();
//            int maxHeight = 150;
//            ClientModSettings modSettings = ClientModSettings.getInstance();
//            ClientModSettings.Settings modSettingsRead = ClientModSettings.getSettingsReadOnly();
//            registeredKeys.add(new HoldToggleKeyEntry(
//                    new VanillaKeyEntry(InputHandler.getInstance().attackHotbar, 
//                            this::getSelectedKey, this::setSelectedKey, maxHeight), 
//                    new ControlSettingToggleButton(40, 20, 
//                            button -> {
//                                modSettings.editSettings(s -> s.toggleLmbHotbar = !s.toggleLmbHotbar);
//                                InputHandler.getInstance().setToggledHotbarControls(ControlScheme.Hotbar.LEFT_CLICK, false);
//                            },
//                            () -> modSettingsRead.toggleLmbHotbar)));
//            registeredKeys.add(new HoldToggleKeyEntry(
//                    new VanillaKeyEntry(InputHandler.getInstance().abilityHotbar, 
//                            this::getSelectedKey, this::setSelectedKey, maxHeight), 
//                    new ControlSettingToggleButton(40, 20, 
//                            button -> {
//                                modSettings.editSettings(s -> s.toggleRmbHotbar = !s.toggleRmbHotbar);
//                                InputHandler.getInstance().setToggledHotbarControls(ControlScheme.Hotbar.RIGHT_CLICK, false);
//                            },
//                            () -> modSettingsRead.toggleRmbHotbar)));
//            registeredKeys.add(new HoldToggleKeyEntry(
//                    new VanillaKeyEntry(InputHandler.getInstance().disableHotbars, 
//                            this::getSelectedKey, this::setSelectedKey, maxHeight), 
//                    new ControlSettingToggleButton(40, 20, 
//                            button -> {
//                                modSettings.editSettings(s -> s.toggleDisableHotbars = !s.toggleDisableHotbars);
//                                InputHandler.getInstance().setToggleHotbarsDisabled(false);
//                            },
//                            () -> modSettingsRead.toggleDisableHotbars)));
//            switch (selectedTab) {
//            case STAND:
//                registeredKeys.add(new VanillaKeyEntry(InputHandler.getInstance().toggleStand, 
//                        this::getSelectedKey, this::setSelectedKey, maxHeight));
//                registeredKeys.add(new VanillaKeyEntry(InputHandler.getInstance().standRemoteControl, 
//                        this::getSelectedKey, this::setSelectedKey, maxHeight));
//                registeredKeys.add(new VanillaKeyEntry(InputHandler.getInstance().standMode, 
//                        this::getSelectedKey, this::setSelectedKey, maxHeight));
//                break;
//            case NON_STAND:
//                registeredKeys.add(new VanillaKeyEntry(InputHandler.getInstance().nonStandMode, 
//                        this::getSelectedKey, this::setSelectedKey, maxHeight));
//                IPowerType<?, ?> type = power.getType();
//                if (type == ModPowers.HAMON.get()) {
//                    registeredKeys.add(new VanillaKeyEntry(InputHandler.getInstance().hamonSkillsWindow, 
//                            this::getSelectedKey, this::setSelectedKey, maxHeight));
//                }
//                break;
//            }
//            registeredKeys.forEach(entry -> {
//                for (IGuiEventListener button : entry.children()) {
//                    if (button instanceof Widget) {
//                        addButton((Widget) button);
//                    }
//                    else {
//                        addWidget(button);
//                    }
//                }
//            });
        }
    }
    
    private void refreshCustomKeybindEntries() {
        keybindButtons.keySet().forEach(entry -> removeKeybindEntryFromUi(entry, false));
        keybindButtons.clear();
        selectedKey.clear();
        for (ActionKeybindEntry keybindEntry : currentControlScheme.getCustomKeybinds()) {
            _addKeybindEntryToUi(keybindEntry);
        }
    }
    
    @Nullable
    private final KeyBinding getSelectedKey() {
        return selectedKey.getKeybind();
    }

    private final void setSelectedKey(KeyBinding registeredKey) {
        selectedKey.setKeybind(registeredKey);
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
    
    
    
    
    
    private final SelectedKey selectedKey = new SelectedKey();
    private Optional<ActionKeybindEntry> hoveredKeybindSlot = Optional.empty();
    private final Map<ActionKeybindEntry, KeybindButtonsHolder> keybindButtons = new LinkedHashMap<>();
    private void renderKeybindsList(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        hoveredKeybindSlot = getKeybindSlotAt(mouseX, mouseY);
        Collection<KeyBinding> keys = new ArrayList<>();
        Collections.addAll(keys, minecraft.options.keyMappings);
        for (KeybindButtonsHolder keybindLine : keybindButtons.values()) {
            keys.add(keybindLine.keybindEntry.getKeybind());
        }
        
        for (Map.Entry<ActionKeybindEntry, KeybindButtonsHolder> entry : keybindButtons.entrySet()) {
            KeyBinding keybind = entry.getKey().getKeybind();
            KeybindButtonsHolder keybindUi = entry.getValue();
            
            keybindUi.keybindButton.setMessage(keybind.getTranslatedKeyMessage());
            boolean conflicting = false;
            boolean keyCodeModifierConflict = true; // less severe form of conflict, like SHIFT conflicting with SHIFT+G
            if (!keybind.isUnbound()) {
                for (KeyBinding otherKey : keys) {
                    if (otherKey != keybind && keybind.same(otherKey)) {
                        conflicting = true;
                        keyCodeModifierConflict &= otherKey.hasKeyCodeModifierConflict(keybind);
                    }
                }
            }

            if (!selectedKey.isEmpty() && selectedKey.getKeybind() == keybindUi.keybindEntry.getKeybind()) {
                keybindUi.keybindButton.setMessage(
                        new StringTextComponent("> ")
                        .append(keybindUi.keybindButton.getMessage().copy().withStyle(TextFormatting.YELLOW))
                        .append(" <").withStyle(TextFormatting.YELLOW));
            } else if (conflicting) {
                keybindUi.keybindButton.setMessage(
                        keybindUi.keybindButton.getMessage().copy()
                        .withStyle(keyCodeModifierConflict ? TextFormatting.GOLD : TextFormatting.RED));
            }
        }
        
        addKeybindButton.y = getWindowY() + 64 + keybindButtons.size() * 22;
        addKeybindButton.visible = canAddKeybinds();
        
        
        
//        int i = 0;
//        int x = getWindowX() + 15;
//        int y = getWindowY() + WINDOW_HEIGHT + 4;
//        for (KeyBindingList.Entry vanillaKeyEntry : registeredKeys) {
//            vanillaKeyEntry.render(matrixStack, i, y + i * 20, x, -1, 20, 
//                    mouseX, mouseY, false, partialTick);
//            ++i;
//        }
    }
    
    private boolean canAddKeybinds() {
        return keybindButtons.size() < 5;
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
    
    private Optional<ActionKeybindEntry> getKeybindSlotAt(int mouseX, int mouseY) {
        return keybindButtons.values().stream()
                .filter(pos -> {
                    int slotX = pos.getActionSlotX();
                    int slotY = pos.getActionSlotY();
                    return mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18;
                })
                .findFirst()
                .map(ui -> ui.keybindEntry);
    }
    
    private <P extends IPower<P, ?>> void renderKeybindSlots(P power, 
            MatrixStack matrixStack, int mouseX, int mouseY) {
        for (Map.Entry<ActionKeybindEntry, KeybindButtonsHolder> actionEntry : keybindButtons.entrySet()) {
            Action<P> action = (Action<P>) actionEntry.getKey().getAction();
            KeybindButtonsHolder pos = actionEntry.getValue();
            renderActionSlot(matrixStack, pos.getActionSlotX(), pos.getActionSlotY(), mouseX, mouseY, 
                    power, action, true, 
                    draggedAction.isPresent(), 
                    hoveredKeybindSlot.map(hovered -> hovered == actionEntry.getKey()).orElse(false),
                    action != null);
        }
    }
    
    private void createBlankKeybindEntry() {
        ActionKeybindEntry entry = currentControlScheme.addBlankKeybindEntry(PressActionType.CLICK);
        markKeybindEdited(entry);
        markLayoutEdited();
        _addKeybindEntryToUi(entry);
    }
    
    private void setCustomKeybind(Action<?> action, InputMappings.Type inputType, int key) {
        if (!canAddKeybinds()) {
            return;
        }
        
        Optional<ActionKeybindEntry> actionAlreadyHasKey = keybindButtons.keySet().stream()
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
            ActionKeybindEntry entry = currentControlScheme.addKeybindEntry(
                    PressActionType.CLICK, action, inputType, key);
            markKeybindEdited(entry);
            _addKeybindEntryToUi(entry);
        }

    }
    
    private void removeKeybindEntryFromUi(ActionKeybindEntry entry, boolean removeFromList) {
        KeybindButtonsHolder buttons = keybindButtons.get(entry);
        if (buttons != null) {
            for (Widget button : buttons.buttons) {
                removeButton(button);
            }
        }
        if (removeFromList) {
            keybindButtons.remove(buttons.keybindEntry);
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
        
        Widget removeButton = new CustomButton(
                -1, -1, 
                8, 8, StringTextComponent.EMPTY, button -> {
            if (currentControlScheme.removeKeybindEntry(entry)) {
                markLayoutEdited();
                removeKeybindEntryFromUi(entry, true);
                updateKeybindEntriesPos();
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
        
        KeybindButtonsHolder buttons = new KeybindButtonsHolder(entry,
                keyBindingButton, removeButton);
        keybindButtons.put(entry, buttons);
        updateKeybindEntryPos(buttons, keybindButtons.size() - 1);
    }
    
    private void updateKeybindEntriesPos() {
        int i = 0;
        for (KeybindButtonsHolder keyEntry : keybindButtons.values()) {
            updateKeybindEntryPos(keyEntry, i++);
        }
    }
    
    private void updateKeybindEntryPos(KeybindButtonsHolder entry, int index) {
        int x = getWindowX() + 10;
        int y = getWindowY() + 62 + index * 22;
        
        entry.setPos(x, y);
        
        entry.keybindButton.x = x + 22;
        entry.keybindButton.y = y;
        entry.removeButton.x = entry.keybindButton.x + entry.keybindButton.getWidth() + 4;
        entry.removeButton.y = y + 5;
        for (Widget button : entry.buttons) {
            if (!children.contains(button)) {
                addButton(button);
            }
        }
    }
    
    private boolean removeButton(Widget button) {
        buttons.remove(button);
        return children.remove(button);
    }
    
    private static class KeybindButtonsHolder {
        private final ActionKeybindEntry keybindEntry;
        private final List<Widget> buttons;
        private final Widget keybindButton;
        private final Widget removeButton;
        private int x;
        private int y;
        
        KeybindButtonsHolder(ActionKeybindEntry keybindEntry, 
                Widget keybindButton, Widget removeButton) {
            this.keybindEntry = keybindEntry;
            this.keybindButton = keybindButton;
            this.removeButton = removeButton;
            this.buttons = ImmutableList.of(keybindButton, removeButton);
        }
        
        int getActionSlotX() {
            return x;
        }
        
        int getActionSlotY() {
            return y + 1;
        }
        
        void setPos(int x, int y) {
            this.x = x;
            this.y = y;
        }
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
}
