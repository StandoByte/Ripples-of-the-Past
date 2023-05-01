package com.github.standobyte.jojo.client.ui.screen.hudlayout;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.InputHandler.MouseButton;
import com.github.standobyte.jojo.client.resources.CustomResources;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui;
import com.github.standobyte.jojo.client.ui.screen.CustomButton;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClLayoutHotbarPacket;
import com.github.standobyte.jojo.network.packets.fromclient.ClLayoutQuickAccessPacket;
import com.github.standobyte.jojo.power.ActionHotbarData;
import com.github.standobyte.jojo.power.ActionHotbarData.ActionSwitch;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPower.ActionType;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

/* 
 * FIXME (layout editing) !! close the window when the player's power being changed/replaced
 */
// TODO saving layout variants
@SuppressWarnings("deprecation")
public class HudLayoutEditingScreen extends Screen {
    private static final ResourceLocation WINDOW = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/layout_editing.png");
    private static final int WINDOW_WIDTH = 200;
    private static final int WINDOW_HEIGHT = 124;
    
    private static PowerClassification selectedTab = null;
    private IPower<?, ?> selectedPower;
    private List<IPower<?, ?>> powersPresent = new ArrayList<>();
    
    private Optional<ActionData<?>> draggedAction = Optional.empty();
    private Optional<ActionData<?>> hoveredAction = Optional.empty();
    private boolean isQuickActionSlotHovered;
    
    private Map<PowerClassification, Set<ActionType>> editedLayouts = Util.make(new EnumMap<>(PowerClassification.class), map -> {
        for (PowerClassification power : PowerClassification.values()) {
            map.put(power, EnumSet.noneOf(ActionType.class));
        }
    });
    private Map<PowerClassification, Action<?>> changedQuickAccessSlots = new EnumMap<>(PowerClassification.class);

    public HudLayoutEditingScreen() {
        super(new TranslationTextComponent("jojo.screen.edit_hud_layout"));
    }
    
    @Override
    protected void init() {
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
                    if (selectedTab == null) {
                        selectedTab = powerClassification;
                    }
                }
            });
        }
        
        selectedPower = IPower.getPlayerPower(minecraft.player, selectedTab);
        
        addButton(new CustomButton(getWindowX() + WINDOW_WIDTH - 30, getWindowY() + WINDOW_HEIGHT - 30, 24, 24, 
                button -> {
                    PacketManager.sendToServer(ClLayoutHotbarPacket.resetLayout(selectedPower.getPowerClassification()));
                    selectedPower.getActionsLayout().resetLayout();
                    editedLayouts.put(selectedPower.getPowerClassification(), EnumSet.noneOf(ActionType.class));
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
                blit(matrixStack, x, y, 0, 184 + getYImage(isHovered()) * 24, width, height);
            }
        });
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        hoveredAction = getActionAt(mouseX, mouseY);
        isQuickActionSlotHovered = isQuickAccessActionSlotAt(mouseX, mouseY);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        renderTabButtons(matrixStack, false);
        renderWindow(matrixStack);
        renderTabButtons(matrixStack, true);
        renderSlots(matrixStack, mouseX, mouseY);
        renderDragged(matrixStack, mouseX, mouseY);
        renderToolTips(matrixStack, mouseX, mouseY);
        buttons.forEach(button -> button.render(matrixStack, mouseX, mouseY, partialTick));
        drawText(matrixStack);
    }
    

    private void renderWindow(MatrixStack matrixStack) {
        RenderSystem.enableBlend();
        minecraft.getTextureManager().bind(WINDOW);
        blit(matrixStack, getWindowX(), getWindowY(), 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        blit(matrixStack, getWindowX() + 9, getWindowY() + 3, 232, 3, 9, 16);
        blit(matrixStack, getWindowX() + 9, getWindowY() + 39, 232, 39, 9, 16);
        blit(matrixStack, getWindowX() + 9, getWindowY() + 75, 232, 75, 9, 16);
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
            minecraft.getTextureManager().bind(powersPresent.get(i).getType().getIconTexture());
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

    private static final int HOTBARS_X = 8;
    private static final int ATTACKS_HOTBAR_Y = 20;
    private static final int ABILITIES_HOTBAR_Y = 56;
    private static final int QUICK_ACCESS_Y = 92;
    private <P extends IPower<P, ?>> void renderSlots(MatrixStack matrixStack, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        P iSuckAtThis = (P) selectedPower;
        int x = HOTBARS_X + getWindowX();
        renderHotbar(iSuckAtThis, ActionType.ATTACK, matrixStack, x, ATTACKS_HOTBAR_Y + getWindowY(), mouseX, mouseY);
        renderHotbar(iSuckAtThis, ActionType.ABILITY, matrixStack, x, ABILITIES_HOTBAR_Y + getWindowY(), mouseX, mouseY);
        renderActionSlot(matrixStack, x, QUICK_ACCESS_Y + getWindowY(), mouseX, mouseY, 
                iSuckAtThis, iSuckAtThis.getActionsLayout().getQuickAccessAction(), 
                true, 
                draggedAction.isPresent(), 
                isQuickActionSlotHovered && draggedAction.isPresent(), 
                true);
        RenderSystem.disableBlend();
    }
    
    private <P extends IPower<P, ?>> void renderHotbar(P power, ActionType hotbar,
            MatrixStack matrixStack, int hotbarX, int hotbarY,
            int mouseX, int mouseY) {
        int i = 0;
        for (ActionSwitch<P> actionSwitch : power.getActions(hotbar).getLayoutView()) {
            renderActionSlot(matrixStack, hotbarX + i * 18, hotbarY, mouseX, mouseY, 
                    power, actionSwitch.getAction(), 
                    actionSwitch.isEnabled(), 
                    draggedAction.map(dragged -> dragged.hotbar == hotbar).orElse(false), 
                    hoveredAction.map(slot -> slot.actionSwitch == actionSwitch).orElse(false), 
                    draggedAction.map(dragged -> dragged.actionSwitch != actionSwitch).orElse(true));
            i++;
        }
    }
    
    private <P extends IPower<P, ?>> void renderActionSlot(MatrixStack matrixStack, 
            int x, int y, int mouseX, int mouseY, 
            P power, Action<P> action, 
            boolean isEnabled, boolean fitsForDragged, boolean isHoveredOver, boolean renderActionIcon) {
        minecraft.getTextureManager().bind(WINDOW);
        int texX = isHoveredOver ? 82 : 64;
        if (fitsForDragged) {
            texX += 36;
        }
        blit(matrixStack, x, y, texX, 238, 18, 18);

        if (renderActionIcon && action != null) {
            if (shift) {
                action = action.getShiftVariationIfPresent();
            }
            TextureAtlasSprite textureAtlasSprite = CustomResources.getActionSprites().getSprite(action, power);
            minecraft.getTextureManager().bind(textureAtlasSprite.atlas().location());
            if (!isEnabled) {
                RenderSystem.color4f(0.0F, 0.0F, 0.0F, 0.25F);
            }
            blit(matrixStack, x + 1, y + 1, 0, 16, 16, textureAtlasSprite);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    
    private <P extends IPower<P, ?>> void renderDragged(MatrixStack matrixStack, int mouseX, int mouseY) {
        draggedAction.ifPresent(dragged -> {
            RenderSystem.translatef(0.0F, 0.0F, 32.0F);
            this.setBlitOffset(200);
            Action<P> action = (Action<P>) dragged.actionSwitch.getAction();
            if (shift) {
                action = action.getShiftVariationIfPresent();
            }
            TextureAtlasSprite textureAtlasSprite = CustomResources.getActionSprites()
                    .getSprite(action, (P) selectedPower);
            blit(matrixStack, mouseX - 8, mouseY - 8, 0, 16, 16, textureAtlasSprite);
            this.setBlitOffset(0);
            RenderSystem.translatef(0.0F, 0.0F, -32.0F);
        });
    }
    
    private Optional<ActionData<?>> getActionAt(int mouseX, int mouseY) {
        return getHotbarAt(mouseY - getWindowY())
                .flatMap(hotbar -> getSlotInHotbar(hotbar, mouseX - getWindowX())
                .flatMap(action -> Optional.of(new ActionData<>(action, hotbar))));
    }
    
    private Optional<ActionType> getHotbarAt(int mouseY) {
        if (mouseY >= ATTACKS_HOTBAR_Y && mouseY < ATTACKS_HOTBAR_Y + 18) return Optional.of(ActionType.ATTACK);
        if (mouseY >= ABILITIES_HOTBAR_Y && mouseY < ABILITIES_HOTBAR_Y + 18) return Optional.of(ActionType.ABILITY);
        return Optional.empty();
    }
    
    private Optional<ActionSwitch<?>> getSlotInHotbar(ActionType hotbar, int mouseX) {
        mouseX -= HOTBARS_X;
        if (mouseX < 0) return Optional.empty();
        List<? extends ActionSwitch<?>> layout = selectedPower.getActions(hotbar).getLayoutView();
        int slot = mouseX / 18;
        return slot < layout.size() ? Optional.of(layout.get(slot)) : Optional.empty();
    }
    
    private boolean isQuickAccessActionSlotAt(int mouseX, int mouseY) {
        mouseX -= getWindowX();
        mouseY -= getWindowY();
        return mouseX >= HOTBARS_X && mouseX < HOTBARS_X + 18 && mouseY >= QUICK_ACCESS_Y && mouseY < QUICK_ACCESS_Y + 18;
    }
    
    private void renderToolTips(MatrixStack matrixStack, int mouseX, int mouseY) {
        if (draggedAction.isPresent()) return;
        int tab = getTabButtonAt(mouseX, mouseY);
        if (tab >= 0 && tab < powersPresent.size()) {
            renderTooltip(matrixStack, powersPresent.get(tab).getName(), mouseX, mouseY);
        }
        else {
            renderActionNameTooltip(matrixStack, mouseX, mouseY);
        }
    }
    
    private <P extends IPower<P, ?>> void renderActionNameTooltip(MatrixStack matrixStack, int mouseX, int mouseY) {
        P power = (P) selectedPower;
        hoveredAction.ifPresent(slot -> {
            renderActionName(matrixStack, power, (Action<P>) slot.actionSwitch.getAction(), mouseX, mouseY, slot.actionSwitch.isEnabled());
        });
        if (isQuickActionSlotHovered) {
            renderActionName(matrixStack, power, power.getActionsLayout().getQuickAccessAction(), mouseX, mouseY, true);
        }
    }
    
    private <P extends IPower<P, ?>> void renderActionName(MatrixStack matrixStack, P power, Action<P> action, int mouseX, int mouseY, boolean isEnabled) {
        if (action == null) return;
        IFormattableTextComponent name;
        
        if (action.isUnlocked(power)) {
            if (shift) {
                action = action.getShiftVariationIfPresent();
            }
            name = action.getTranslatedName(power, action.getTranslationKey(power, ActionTarget.EMPTY));
        }
        
        else {
            name = action.getNameLocked(power).withStyle(TextFormatting.GRAY, TextFormatting.ITALIC);
        }
        
        if (!isEnabled) {
            name.withStyle(TextFormatting.DARK_GRAY);
        }
        renderTooltip(matrixStack, name, mouseX, mouseY);
    }
    
    private void drawText(MatrixStack matrixStack) {
        int x = getWindowX();
        int y = getWindowY();
        minecraft.font.draw(matrixStack, new TranslationTextComponent("jojo.screen.edit_hud_layout.attacks"), 
                x + 22, y + 8, 0x404040);
        minecraft.font.draw(matrixStack, new TranslationTextComponent("jojo.screen.edit_hud_layout.abilities"), 
                x + 22, y + 44, 0x404040);
        minecraft.font.draw(matrixStack, new TranslationTextComponent("jojo.screen.edit_hud_layout.mmb"), 
                x + 22, y + 80, 0x404040);
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
        Optional<ActionData<?>> clickedSlot = getActionAt(mouseX, mouseY);
        
        if (draggedAction.isPresent()) {
            clickedSlot.ifPresent(clicked -> {
                if (draggedAction.get().hotbar == clicked.hotbar) {
                    selectedPower.getActions(clicked.hotbar).editLayout(
                            layout -> layout.swapActions(draggedAction.get().actionSwitch, clicked.actionSwitch));
                    markLayoutEdited(clicked.hotbar);
                }
            });
            if (isQuickActionSlotHovered) {
                setQuickAccess(selectedPower, draggedAction.get().actionSwitch.getAction());
            }
            draggedAction = Optional.empty();
            return true;
        }
        
        else {
            int tab = getTabButtonAt(mouseX, mouseY);
            if (tab >= 0 && tab < powersPresent.size()) {
                selectedPower = powersPresent.get(tab);
                selectedTab = selectedPower.getPowerClassification();
                return true;
            }
            if (clickedSlot.isPresent()) {
                ActionType hotbar = clickedSlot.get().hotbar;
                switch (button) {
                case LEFT:
                    draggedAction = clickedSlot;
                    return true;
                case RIGHT:
                    ActionSwitch<?> slot = clickedSlot.get().actionSwitch;
                    selectedPower.getActions(hotbar).editLayout(layout -> slot.setIsEnabled(!slot.isEnabled()));                    
                    markLayoutEdited(hotbar);
                    
                    if (slot.isEnabled() && selectedPower == ActionsOverlayGui.getInstance().getCurrentPower()
                            && isActionVisible(slot.getAction(), selectedPower)) {
                        int slotIndex = selectedPower.getActions(hotbar).getEnabled().indexOf(slot.getAction());
                        if (slotIndex >= 0) {
                            ActionsOverlayGui.getInstance().selectAction(hotbar, slotIndex);
                        }
                    }
                    return true;
                case MIDDLE:
                    setQuickAccess(selectedPower, clickedSlot.get().actionSwitch.getAction());
                    return true;
                default:
                    return false;
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    private <P extends IPower<P, ?>> boolean isActionVisible(Action<P> action, IPower<?, ?> power) {
        return action.getVisibleAction((P) power) != null;
    }
    
    private boolean shift = false;
    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (minecraft.options.keyShift.matches(key, scanCode)) {
            shift = true;
        }
        if (InputHandler.getInstance().editHotbars.matches(key, scanCode)) {
            onClose();
            return true;
        } 
        else {
            int numKey = getNumKey(key, scanCode);
            if (numKey > -1) {
                Optional<ActionData<?>> toMove = draggedAction;
                if (!toMove.isPresent()) toMove = hoveredAction;
                if (toMove.map(action -> {
                    ActionHotbarData<?> actionsHotbar = selectedPower.getActions(action.hotbar);
                    if (numKey < actionsHotbar.getLayoutView().size()) {
                        actionsHotbar.editLayout(
                                layout -> layout.swapActions(action.actionSwitch, actionsHotbar.getLayoutView().get(numKey)));
                        markLayoutEdited(action.hotbar);
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
        }
        return super.keyPressed(key, scanCode, modifiers);
    }
    
    private int getNumKey(int key, int scanCode) {
        for (int i = 0; i < 9; ++i) {
            if (minecraft.options.keyHotbarSlots[i].isActiveAndMatches(InputMappings.getKey(key, scanCode))) {
                return i;
            }
        }
        return -1;
    }
    
    private void markLayoutEdited(ActionType hotbar) {
        editedLayouts.get(selectedPower.getPowerClassification()).add(hotbar);
    }

    private <P extends IPower<P, ?>> void setQuickAccess(IPower<?, ?> power, Action<P> action) {
        ((P) power).getActionsLayout().setQuickAccessAction(action);
        changedQuickAccessSlots.put(power.getPowerClassification(), action);
    }
    
    @Override
    public boolean keyReleased(int key, int scanCode, int modifiers) {
        if (minecraft.options.keyShift.matches(key, scanCode)) {
            shift = false;
        }
        return super.keyReleased(key, scanCode, modifiers);
    }

    // FIXME (layout editing) protection from client packet data
    @Override
    public void onClose() {
        super.onClose();
        editedLayouts.forEach((power, hotbars) -> {
            hotbars.forEach(hotbar -> {
                PacketManager.sendToServer(ClLayoutHotbarPacket.withLayout(power, hotbar, 
                        IPower.getPlayerPower(minecraft.player, power).getActions(hotbar).getLayout()));
            });
        });
        changedQuickAccessSlots.forEach((power, action) -> {
            PacketManager.sendToServer(new ClLayoutQuickAccessPacket(power, action));
        });
    }
    
    private static class ActionData<P extends IPower<P, ?>> {
        private final ActionSwitch<P> actionSwitch;
        private final ActionType hotbar;
        
        private ActionData(ActionSwitch<P> actionSwitch, ActionType hotbar) {
            this.actionSwitch = actionSwitch;
            this.hotbar = hotbar;
        }
    }
}
