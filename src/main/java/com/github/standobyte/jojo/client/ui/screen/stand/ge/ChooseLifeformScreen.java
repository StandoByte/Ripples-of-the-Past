package com.github.standobyte.jojo.client.ui.screen.stand.ge;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.GoldExperienceChooseLifeform;
import com.github.standobyte.jojo.action.stand.GoldExperienceCreateLifeform;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientModSettings;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.ui.screen.ScreenCloseMode;
import com.github.standobyte.jojo.client.ui.screen.WasdAllowingScreen;
import com.github.standobyte.jojo.client.ui.screen.widgets.ImageVanillaButton;
import com.github.standobyte.jojo.client.ui.tooltip.CustomTooltipRender;
import com.github.standobyte.jojo.client.ui.tooltip.ITooltipLine;
import com.github.standobyte.jojo.client.ui.tooltip.IconTooltipLine;
import com.github.standobyte.jojo.client.ui.tooltip.MultiTooltipLine;
import com.github.standobyte.jojo.client.ui.tooltip.TextTooltipLine;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClAllGELifeformsButtonPacket;
import com.github.standobyte.jojo.util.mc.EntityTypeToInstance;
import com.github.standobyte.jojo.util.mod.ModInteractionUtil;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class ChooseLifeformScreen extends WasdAllowingScreen {
    protected PlayerUtilCap playerUISettings;
    
    private static String savedSearchFilter = "";
    
    private TextFieldWidget searchField;
    private Button clearSearchFieldButton;
    
    public static void openWindowOnClick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) {
            ViewMode type = ClientModSettings.getSettingsReadOnly().viewModeGE;
            if (type == null) {
                type = ViewMode.GRID;
                ClientModSettings.edit(s -> s.viewModeGE = ViewMode.GRID);
            }
            Screen screen;
            switch (type) {
            case GRID:
                screen = new ChooseLifeformGridScreen(InputHandler.lastActionKey);
                mc.setScreen(screen);
                break;
            case LIST:
                screen = new ChooseLifeformListScreen(InputHandler.lastActionKey);
                mc.setScreen(screen);
                break;
            }
        }
    }
    
    public enum ViewMode {
        GRID,
        LIST
    }
    
    
    
    public ChooseLifeformScreen(KeyBinding keyHeld) {
        super(StringTextComponent.EMPTY);
        this.keyHeld = keyHeld;
    }
    
    
    @Override
    protected void init() {
        super.init();
        playerUISettings = minecraft.player.getCapability(PlayerUtilCapProvider.CAPABILITY).resolve().get();
    }
    
    protected void addSearchField() {
        searchField = new TextFieldWidget(minecraft.font, width - 101, height - 76, 84, 20, 
                searchField, new TranslationTextComponent("jojo.ge_lifeform.search_field"));
        searchField.visible = false;
        searchField.setResponder(this::filterEntries);
        addWidget(searchField);
        
        addButton(clearSearchFieldButton = new ImageButton(width - 12, height - 70, 8, 7, 40, 112, 8, LIFEFORM_CHOOSE_LOCATION, 128, 128, 
                button -> {
                    searchField.setValue("");
                }));
        clearSearchFieldButton.visible = searchField.visible;
        searchField.setValue(savedSearchFilter);
    }
    
    protected abstract void refreshEntityTypes();
    
    protected void addCommonWidgets(ViewMode currentMode) {
        Minecraft mc = getMinecraft();
        Button gridModeButton = new ImageVanillaButton(
                width - 101, height - 48, 20, 20, 48, 68, 
                LIFEFORM_CHOOSE_LOCATION, 128, 128, 
                button -> {
                    ClientModSettings.edit(s -> s.viewModeGE = ViewMode.GRID);
                    mc.setScreen(new ChooseLifeformGridScreen(null));
                },
                (matrixStack, button, mouseX, mouseY) -> {
                    // TODO tooltip
                },
                StringTextComponent.EMPTY);
        
        Button listModeButton = new ImageVanillaButton(
                width - 76, height - 48, 20, 20, 68, 68, 
                LIFEFORM_CHOOSE_LOCATION, 128, 128, 
                button -> {
                    ClientModSettings.edit(s -> s.viewModeGE = ViewMode.LIST);
                    mc.setScreen(new ChooseLifeformListScreen(null));
                },
                (matrixStack, button, mouseX, mouseY) -> {
                    // TODO tooltip
                },
                StringTextComponent.EMPTY);
        
        switch (currentMode) {
        case GRID:
            gridModeButton.active = false;
            break;
        case LIST:
            listModeButton.active = false;
            break;
        }
        
        addButton(listModeButton);
        addButton(gridModeButton);
        
        Button unlockAllButton = new Button(width - 101, height - 24, 95, 20, new TranslationTextComponent("jojo.ge_lifeform.unlock_all"), 
                button -> {
                    GoldExperienceChooseLifeform.unlockAllEntityTypes(mc.player);
                    PacketManager.sendToServer(new ClAllGELifeformsButtonPacket());
                    refreshEntityTypes();
                });
        unlockAllButton.visible = mc.player.abilities.instabuild;
        addButton(unlockAllButton);
    }
    
    public static final ResourceLocation LIFEFORM_CHOOSE_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/lifeform_choose.png");
    
    public static final Comparator<String> MOD_NAMES_ORDER = (name1, name2) -> {
        boolean mc1 = "Minecraft".equals(name1);
        boolean mc2 = "Minecraft".equals(name2);
        if (mc1 ^ mc2) {
            return mc1 ? -1 : 1;
        }
        return name1.compareTo(name2);
    };
    
    public static final Comparator<EntityType<?>> FUCK_GENERICS = Comparator.comparing(
            type -> ModInteractionUtil.getModName(type.getRegistryName()),
            MOD_NAMES_ORDER);
    public static final Comparator<EntityType<?>> ENTITY_NAME_COMPARE = Comparator.comparing(t -> t.getDescription().getString());
    

    protected abstract void filterEntries(String field);
    
    @Override
    public void setFocused(@Nullable IGuiEventListener pListener) {
        if (pListener == null || pListener == searchField) {
            doSetFocused(pListener);
        }
        else {
            IGuiEventListener focused = getFocused();
            if (searchField != null && focused == searchField) {
                searchField.setFocus(true);
            }
        }
    }
//    
//    private void filterEntries(String field) {
//        boolean emptyQuery = field == null || field.isEmpty();
//        Predicate<EntityType<?>> filter = emptyQuery ? null : 
//            entityType -> {
//                String searchLC = field.toLowerCase();
//                return entityType.getDescription().getString().toLowerCase().contains(searchLC)
//                || ModInteractionUtil.getModName(entityType.getRegistryName()).toLowerCase().contains(searchLC)
//                || entityType.getRegistryName().toString().contains(searchLC);
//            };
//        entityIconsGrid.setFilter(GeneralUtil.mapPredicate(filter, widget -> widget.entityType));
//        entityIconsGrid.setShowHidden(!emptyQuery);
//    }
//    
//    
//    
    private int ticksKeyHeld = 0;
    private final KeyBinding keyHeld;
    private ScreenCloseMode mode = ScreenCloseMode.CLICK;
    private boolean holdsButton = true;
    
    @Override
    public void tick() {
        super.tick();
        if (holdsButton) {
            if (!isKeyBeingHeld()) {
                holdsButton = false;
            }
            else if (++ticksKeyHeld == 5) {
                mode = ScreenCloseMode.HOLD;
            }
        }
    }
    
    private boolean isKeyBeingHeld() {
        if (keyHeld == null) return false;
        
        long window = minecraft.getWindow().getWindow();
        int value = keyHeld.getKey().getValue();
        int state = value < 8 ? GLFW.glfwGetMouseButton(window, value) : GLFW.glfwGetKey(window, value);
        return state == 1;
    }
    
    
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
//        chosenLifeformCache = getEntriesUiData(minecraft.player).map(
//                entityData -> entityData.getGEChosenLifeformType()).orElse(null);
        if (searchField != null) {
            searchField.render(matrixStack, mouseX, mouseY, partialTicks);
        }
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        
        if (!holdsButton && mode == ScreenCloseMode.HOLD) {
            chooseHoveredAndClose();
        }
    }
    
    protected void chooseHoveredAndClose() {
        
        onClose();
    }
//    
//    private void updateHoveredElement(int mouseX, int mouseY) {
//        boolean mouseMoved = checkMouseMoved(mouseX, mouseY);
//        entityIconsGrid.forEach(widget -> {
//            if (widget.visible) {
//                widget.updateIsHovered(mouseX, mouseY);
//                if (mouseMoved && widget.isHovered() && !widget.isSelected) {
//                    entityIconsGrid.setSelected(widget);
//                }
//                entityIconsGrid.getSelected().ifPresent(w -> {
//                    widget.setSelected(widget == w);
//                });
//            }
//        });
//    }
    
    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("0.0");
    protected void renderHoveredTooltip(MatrixStack matrixStack, EntityType<?> entityType, int mouseX, int mouseY) {
        List<ITooltipLine> entityTypeInfo = new ArrayList<>();
        
        entityTypeInfo.add(new TextTooltipLine(entityType.getDescription()));
        
        entityTypeInfo.add(new TextTooltipLine(new StringTextComponent(ModInteractionUtil.getModName(entityType.getRegistryName()))
                .withStyle(TextFormatting.BLUE, TextFormatting.ITALIC)));
        
        Entity entity = EntityTypeToInstance.getEntityInstance(entityType, minecraft.level);
        String width = SIZE_FORMAT.format(entity.getBbWidth());
        String height = SIZE_FORMAT.format(entity.getBbHeight());
        double strength = GoldExperienceCreateLifeform.getAttackStrength(entity);
        int creationTicks = GoldExperienceCreateLifeform.getTicksToCreate(minecraft.player, ClientUtil.getStandPowerClCached(), entity);
        boolean isCorrectBiome = GoldExperienceCreateLifeform.correctBiome(entity, minecraft.level, minecraft.player.blockPosition());
        String creationSecs = String.format("%.2f", (float) creationTicks / 20F);
        
        entityTypeInfo.add(new MultiTooltipLine(
                new IconTooltipLine(IconTooltipLine.Icon.VOLUME),
                new TextTooltipLine(new TranslationTextComponent("gold_experience.lifeform_size", width, height, width))));
        if (strength > 0) {
            entityTypeInfo.add(new MultiTooltipLine(
                    new IconTooltipLine(IconTooltipLine.Icon.STRENGTH),
                    new TextTooltipLine(new StringTextComponent(String.format("%.1f", strength)))));
        }
        entityTypeInfo.add(new MultiTooltipLine(
                new IconTooltipLine(IconTooltipLine.Icon.TIME),
                new TextTooltipLine(new TranslationTextComponent("gold_experience.lifeform_time", creationSecs)
                        .withStyle(isCorrectBiome ? TextFormatting.GREEN : TextFormatting.WHITE))));
        
//        entityTypeInfo.add(new TextTooltipLine(new StringTextComponent(String.valueOf(GoldExperienceCreateLifeform.getVolume(entity)))));
        
        entityTypeInfo.stream().map(line -> line.getWidth(font)).max(Comparator.naturalOrder()).ifPresent(tooltipWidth -> {
            CustomTooltipRender.renderWrappedToolTip(matrixStack, entityTypeInfo, mouseX, mouseY, font);
        });
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonId) {
        if (super.mouseClicked(mouseX, mouseY, buttonId)) {
            return true;
        }
        
        if (closeOnSecondClick(buttonId)) {
            return true;
        }
//        
//        if (entityIconsGrid.getSelected().isPresent() && entityIconsGrid.isMouseInsideGrid(mouseX, mouseY)) {
//            SelectorWidget hovered = entityIconsGrid.getSelected().get();
//            MouseButton button = MouseButton.getButtonFromId(buttonId);
//            if (button == null) return false;
//            
//            switch (button) {
//            case LEFT:
//                chooseHoveredAndClose();
//                saveMousePos((int) mouseX, (int) mouseY);
//                return true;
//            case RIGHT:
//                getEntriesUiData(minecraft.player).ifPresent(playerData -> {
//                    if (playerData.isGELifeformHidden(hovered.entityType)) {
//                        showEntry(hovered, false);
//                    }
//                    else {
//                        hideEntry(hovered);
//                    }
//                });
//                return true;
//            default:
//                break;
//            }
//        }
//        
        return false;
    }
    
    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (closeOnSecondClick(pKeyCode)) { // FIXME causes the window to open again
            return true;
        }
        
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
    
    private boolean closeOnSecondClick(int keyPressedCode) {
        if (mode == ScreenCloseMode.CLICK && keyHeld != null && keyPressedCode == keyHeld.getKey().getValue()) {
            keyHeld.setDown(false);
            onClose();
            return true;
        }
        
        return false;
    }
//    
//    private boolean handleArrowKey(int pKeyCode, int pScanCode, int pModifiers) {
//        if (!ARROW_KEYS.containsKey(pKeyCode)) return false;
//        
//        boolean control = (pModifiers & GLFW.GLFW_MOD_CONTROL) > 0;
//        Direction2D direction = ARROW_KEYS.get(pKeyCode);
//        entityIconsGrid.moveSelection(direction, control ? ElemMoveMode.EDGE : ElemMoveMode.NEIGHBOR_WRAP);
//        
//        return true;
//    }
//    
//    
//    protected void chooseHoveredAndClose() {
//        entityIconsGrid.getSelected().ifPresent(widget -> {
//            minecraft.player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(
//                    cap -> cap.setGEChosenLifeformType(widget.entityType, true));
//        });
//        onClose();
//    }
    
    @Override
    public void onClose() {
        super.onClose();
        if (searchField != null) {
            savedSearchFilter = searchField.getValue();
        }
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public boolean acceptsKeyInput() {
        return searchField == null || !searchField.canConsumeInput();
    }
    
}
