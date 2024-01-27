package com.github.standobyte.jojo.client.ui.screen.controls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.InputHandler.MouseButton;
import com.github.standobyte.jojo.client.controls.ActionKeybindEntry;
import com.github.standobyte.jojo.client.controls.ActionKeybindEntry.PressActionType;
import com.github.standobyte.jojo.client.controls.ActionVisibilitySwitch;
import com.github.standobyte.jojo.client.controls.ControlScheme;
import com.github.standobyte.jojo.client.controls.HudControlSettings;
import com.github.standobyte.jojo.client.controls.PowerTypeControlSchemes;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.screen.widgets.CustomButton;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
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
    
    private Optional<ActionData> draggedAction = Optional.empty();
    private Optional<ActionData> hoveredAction = Optional.empty();
    
    private Collection<IPower<?, ?>> editedLayouts = new ArrayList<>();

    public HudLayoutEditingScreen() {
        super(new TranslationTextComponent("jojo.screen.edit_hud_layout"));
    }
    
    @Override
    protected void init() {
        // TODO "reset layout" button
//        // reset layout
//        addButton(new CustomButton(getWindowX() + WINDOW_WIDTH - 30, getWindowY() + 6, 24, 24, 
//                button -> {
//                    PowerTypeControlsEntry controls = ActionsControlScheme.getCtrlSchemes(selectedTab);
//                    controls.getCurrentCtrlScheme().reset();
//                    setKeybindsList(controls);
//                    markLayoutEdited(selectedPower);
//                }, 
//                (button, matrixStack, x, y) -> {
//                    renderTooltip(matrixStack, new TranslationTextComponent("jojo.screen.edit_hud_layout.reset"), x, y);
//                }) {
//
//            @Override
//            protected void renderCustomButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
//                Minecraft minecraft = Minecraft.getInstance();
//                minecraft.getTextureManager().bind(WINDOW);
//                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
//                RenderSystem.enableBlend();
//                RenderSystem.defaultBlendFunc();
//                RenderSystem.enableDepthTest();
//                blit(matrixStack, x, y, 0, 184 + getYImage(isHovered()) * height, width, height);
//            }
//        });
        
        // add keybind
        addButton(new CustomButton(getWindowX() + WINDOW_WIDTH - 26, getWindowY() + 36, 20, 20, 
                button -> {
                    createBlankKeybindEntry();
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
        else {
            setKeybindsList(HudControlSettings.getInstance().getCachedControls(selectedTab));
        }
    }
    
    public boolean works() {
        return selectedPower != null && selectedPower.hasPower();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        if (!works()) return;
        renderBackground(matrixStack, 0);
        hoveredAction = getActionAt(mouseX, mouseY);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        renderTabButtons(matrixStack, false);
        renderWindow(matrixStack);
        renderTabButtons(matrixStack, true);
        renderHint(matrixStack);
        renderSlots(matrixStack, mouseX, mouseY);
        renderDragged(matrixStack, mouseX, mouseY);
        renderKeybindsList(matrixStack, mouseX, mouseY);
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
        for (ActionVisibilitySwitch actionSwitch : HudControlSettings.getInstance()
                .getControlScheme(power).getActionsHotbar(hotbar).getActionSwitchesView()) {
            renderActionSlot(matrixStack, hotbarX + i * 18, hotbarY, mouseX, mouseY, 
                    power, actionSwitch, 
                    draggedAction.map(dragged -> dragged.hotbar == hotbar).orElse(false), 
                    hoveredAction.map(slot -> slot.actionSwitch == actionSwitch).orElse(false), 
                    draggedAction.map(dragged -> dragged.actionSwitch != actionSwitch).orElse(true));
            i++;
        }
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
    
    private Optional<ActionData> getActionAt(int mouseX, int mouseY) {
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
                    List<ActionVisibilitySwitch> layout = HudControlSettings.getInstance()
                            .getControlScheme(selectedTab)
                            .getActionsHotbar(hotbar)
                            .getActionSwitchesView();
                    int slot = x / 18;
                    
                    if (slot >= layout.size()) {
                        return Optional.empty();
                    }
                    ActionVisibilitySwitch action = layout.get(slot);
                    return Optional.of(new ActionData(action, hotbar));
                });
    }
    
    
    
    private void renderHint(MatrixStack matrixStack) {
        minecraft.getTextureManager().bind(WINDOW);
        int hintX = getWindowX() + WINDOW_WIDTH - 48;
        int hintY = getWindowY() + 6;
        blit(matrixStack, hintX, hintY, 32, 245, 11, 11);
    }
    
    private final List<ITextComponent> hintTooltip = ImmutableList.of(
            new TranslationTextComponent("jojo.screen.edit_hud_layout.hint.lmb"), 
            new TranslationTextComponent("jojo.screen.edit_hud_layout.hint.rmb"), 
            new TranslationTextComponent("jojo.screen.edit_hud_layout.hint.mmb"));
    private void renderToolTips(MatrixStack matrixStack, int mouseX, int mouseY) {
        if (draggedAction.isPresent()) return;
        int tab = getTabButtonAt(mouseX, mouseY);
        if (tab >= 0 && tab < powersPresent.size()) {
            renderTooltip(matrixStack, powersPresent.get(tab).getName(), mouseX, mouseY);
        }
        else {
            renderActionNameTooltip(matrixStack, mouseX, mouseY);
        }
        
        int hintX = getWindowX() + WINDOW_WIDTH - 48;
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
        IFormattableTextComponent name = getActionName(power, action, shift);
        
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
        Optional<ActionData> clickedSlot = getActionAt(mouseX, mouseY);
        
        // TODO move action to the slot
        if (draggedAction.isPresent()) {
//            clickedSlot.ifPresent(clicked -> {
//                if (draggedAction.get().hotbar == clicked.hotbar) {
//                    HudControlSettings.getInstance()
//                    .getControlScheme(selectedTab)
//                    .getActionsHotbar(clicked.hotbar)
//                    .swapActionsOrder(
//                            draggedAction.get().actionSwitch.action, 
//                            clicked.actionSwitch.action);
//                    markLayoutEdited(selectedPower);
//                }
//            });
//            
//            clickKeybindSlot(draggedAction.get().actionSwitch.action, mouseX, mouseY);
            draggedAction = Optional.empty();
            return true;
        }
        
        else {
            int tab = getTabButtonAt(mouseX, mouseY);
            if (tab >= 0 && tab < powersPresent.size()) {
                selectTab(powersPresent.get(tab));
                return true;
            }
            if (clickedSlot.isPresent()) {
                ControlScheme.Hotbar hotbar = clickedSlot.get().hotbar;
                switch (button) {
                case LEFT:
                    draggedAction = clickedSlot;
                    return true;
                case RIGHT:
                    ActionVisibilitySwitch slot = clickedSlot.get().actionSwitch;
                    slot.setIsEnabled(!slot.isEnabled());
                    markLayoutEdited(selectedPower);
                    
                    // TODO select the action in the HUD
//                    if (slot.isEnabled() && selectedPower == ActionsOverlayGui.getInstance().getCurrentPower()
//                            && isActionVisible(slot.getAction(), selectedPower)) {
//                        int slotIndex = ActionsControlScheme.getCurrentCtrlScheme(selectedTab)
//                                .getHotbarsLayout().getHotbar(hotbar).getEnabled().indexOf(slot.getAction());
//                        if (slotIndex >= 0) {
//                            ActionsOverlayGui.getInstance().selectAction(hotbar, slotIndex);
//                        }
//                    }
                    return true;
                case MIDDLE:
                    setCustomKeybind(clickedSlot.get().actionSwitch.getAction(), InputMappings.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
                    return true;
                default:
                    return false;
                }
            }
        }
        
        return mouseClickedEditingKeybind(mouseButton) || super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    private void selectTab(IPower<?, ?> power) {
        if (power != null && power.hasPower()) {
            selectedPower = power;
            selectedTab = power.getPowerClassification();
            
            setKeybindsList(HudControlSettings.getInstance().getCachedControls(selectedTab));
        }
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
    
    private boolean shift = false;
    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (InputHandler.renderShiftVarInScreenUI(minecraft, key, scanCode)) {
            shift = true;
        }
        if (InputHandler.getInstance().editHotbars.matches(key, scanCode)) {
            onClose();
            return true;
        } 
        else {
            int numKey = getNumKey(key, scanCode);
            if (numKey > -1) {
                Optional<ActionData> toMove = draggedAction;
                if (!toMove.isPresent()) toMove = hoveredAction;
                if (toMove.map(action -> {
                    // TODO move by pressing number key
//                    ActionHotbarLayout<?> actionsHotbar = ActionsControlScheme.getCurrentCtrlScheme(selectedTab)
//                            .getHotbarsLayout().getHotbar(action.hotbar);
//                    if (numKey < actionsHotbar.getLayoutView().size()) {
//                        actionsHotbar.swapActionsOrder(
//                                action.actionSwitch.action, 
//                                actionsHotbar.getLayoutView().get(numKey).action);
//                        markLayoutEdited(selectedPower);
//                        return true;
//                    }
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
        
        return keyPressedEditingKeybind(key, scanCode)
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
    
    private void markLayoutEdited(IPower<?, ?> power) {
        editedLayouts.add(power);
    }
    
    @Override
    public boolean keyReleased(int key, int scanCode, int modifiers) {
        if (InputHandler.renderShiftVarInScreenUI(minecraft, key, scanCode)) {
            shift = false;
        }
        return super.keyReleased(key, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        super.onClose();
        ActionsOverlayGui.getInstance().revealActionNames();
        clearInvalidKeybinds();
        // TODO only save individual control schemes
        HudControlSettings.getInstance().saveAll();
    }
    
    private static class ActionData {
        private final ActionVisibilitySwitch actionSwitch;
        private final ControlScheme.Hotbar hotbar;
        
        private ActionData(ActionVisibilitySwitch actionSwitch, ControlScheme.Hotbar hotbar) {
            this.actionSwitch = actionSwitch;
            this.hotbar = hotbar;
        }
    }
    
    
    
    
    
    private ControlScheme keybinds;
    private Set<ControlScheme> editedKeybindLists = new HashSet<>();
    private ActionKeybindEntry selectedKey;
    private Optional<ActionKeybindEntry> hoveredKeybindSlot = Optional.empty();
    private final Map<ActionKeybindEntry, KeybindButtonsHolder> keybindButtons = new LinkedHashMap<>();
    private void renderKeybindsList(MatrixStack matrixStack, int mouseX, int mouseY) {
        hoveredKeybindSlot = getKeybindSlotAt(mouseX, mouseY);
        
        for (Map.Entry<ActionKeybindEntry, KeybindButtonsHolder> entry : keybindButtons.entrySet()) {
            KeyBinding keybind = entry.getKey().getKeybind();
            KeybindButtonsHolder keybindUi = entry.getValue();
            
            keybindUi.keybindButton.setMessage(keybind.getTranslatedKeyMessage());
            boolean conflicting = false;
            boolean keyCodeModifierConflict = true; // less severe form of conflict, like SHIFT conflicting with SHIFT+G
            if (!keybind.isUnbound()) {
                for (KeyBinding otherKey : minecraft.options.keyMappings) { // TODO also check other keybinds
                    if (otherKey != keybind && keybind.same(otherKey)) {
                        conflicting = true;
                        keyCodeModifierConflict &= otherKey.hasKeyCodeModifierConflict(keybind);
                    }
                }
            }

            if (selectedKey != null && selectedKey.getKeybind() == keybindUi.keybindEntry.getKeybind()) {
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
    }
    
    private boolean mouseClickedEditingKeybind(int buttonId) {
        if (selectedKey != null) {
            KeyModifier keyModifier = KeyModifier.getActiveModifier();
            
            selectedKey.getKeybind().setKeyModifierAndCode(keyModifier, InputMappings.Type.MOUSE.getOrCreate(buttonId));
            selectedKey = null;
            KeyBinding.resetMapping();
            return true;
        }
        
        return false;
    }
    
    private boolean keyPressedEditingKeybind(int keyCode, int scanCode) {
        if (selectedKey != null) {
            KeyBinding keybind = selectedKey.getKeybind();
            KeyModifier keyModifier = KeyModifier.getActiveModifier();
            
            if (keyCode == 256) {
                keybind.setKeyModifierAndCode(keyModifier, InputMappings.UNKNOWN);
            } else {
                keybind.setKeyModifierAndCode(keyModifier, InputMappings.getKey(keyCode, scanCode));
            }
            
            if (!KeyModifier.isKeyCodeModifier(keybind.getKey())) {
                selectedKey = null;
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
    
    private void clickKeybindSlot(Action<?> draggedAction, int mouseX, int mouseY) {
        getKeybindSlotAt(mouseX, mouseY).ifPresent(slot -> slot.setAction(draggedAction));
    }
    
    private void createBlankKeybindEntry() {
        // FIXME
        ActionKeybindEntry entry = keybinds.addKeybindEntry(PressActionType.CLICK, null, -1);
        _addKeybindEntryToUi(entry);
    }
    
    private void setCustomKeybind(Action<?> action, InputMappings.Type inputType, int key) {
//        Optional<ActionKeybindEntry> keyAlreadyBound = keybindButtons.keySet().stream()
//                .filter(entry -> {
//                    InputMappings.Input input = entry.getKeybind().getKey();
//                    return input.getType() == inputType && input.getValue() == key;
//                })
//                .findFirst();
//        Optional<ActionKeybindEntry> actionAlreadyHasKey = keybindButtons.keySet().stream()
//                .filter(entry -> {
//                    return entry.getAction() == action;
//                })
//                .findFirst();
//        
//        if (keyAlreadyBound.isPresent()) {
//            keyAlreadyBound.get().setAction(action);
//            return;
//        }
//        
//        if (actionAlreadyHasKey.isPresent()) {
//            actionAlreadyHasKey.get().setKeybind(inputType, key);
//            return;
//        }
//        
//        ActionKeybindEntry entry = keybinds.addKeybindEntry(PressActionType.CLICK, action, inputType, key);
//        _addKeybindEntryToUi(entry);
    }
    
    private void setKeybindsList(PowerTypeControlSchemes powerTypeControls) {
        keybindButtons.keySet().forEach(this::removeKeybindEntryFromUi);
        keybindButtons.clear();
        
        this.keybinds = powerTypeControls.getCurrentCtrlScheme();
        selectedKey = null;
        editedKeybindLists.add(keybinds);
        
        for (ActionKeybindEntry keybindEntry : keybinds.getCustomKeybinds()) {
            _addKeybindEntryToUi(keybindEntry);
        }
    }
    
    private void removeKeybindEntryFromUi(ActionKeybindEntry entry) {
        KeybindButtonsHolder buttons = keybindButtons.get(entry);
        if (buttons != null) {
            for (Widget button : buttons.buttons) {
                removeButton(button);
            }
        }
    }
    
    private void _addKeybindEntryToUi(ActionKeybindEntry entry) {
        int i = keybindButtons.size();
        int x = getWindowX() + 10;
        int y = getWindowY() + 62 + i * 22;

        Widget keyBindingButton = addButton(new Button(
                x + 22, y, 
                95, 20, entry.getKeybind().getTranslatedKeyMessage(), button -> {
            HudLayoutEditingScreen.this.selectedKey = entry;
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
        });
        
//        Widget modeButton = addButton(new Button(x, y, 24, 20, StringTextComponent.EMPTY, button -> {
//            
//        }));
        
        KeybindButtonsHolder buttons = new KeybindButtonsHolder(entry,
                ImmutableList.of(keyBindingButton), keyBindingButton, x, y);
        keybindButtons.put(entry, buttons);
    }
    
    private boolean removeButton(Widget button) {
        buttons.remove(button);
        return children.remove(button);
    }
    
    private static class KeybindButtonsHolder {
        private final ActionKeybindEntry keybindEntry;
        private final List<Widget> buttons;
        private final Widget keybindButton;
        private int x;
        private int y;
        
        KeybindButtonsHolder(ActionKeybindEntry keybindEntry, List<Widget> buttons, Widget keybindButton, int x, int y) {
            this.keybindEntry = keybindEntry;
            this.buttons = buttons;
            this.keybindButton = keybindButton;
            this.x = x;
            this.y = y;
        }
        
        int getActionSlotX() {
            return x;
        }
        
        int getActionSlotY() {
            return y + 1;
        }
    }
    
    private void clearInvalidKeybinds() {
        // FIXME clear only the keybinds that were added while the window was opened
//        editedKeybindLists.forEach(ActionsControlScheme::clearInvalidKeybinds);
    }
}
