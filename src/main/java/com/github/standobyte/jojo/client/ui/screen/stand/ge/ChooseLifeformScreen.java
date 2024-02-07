package com.github.standobyte.jojo.client.ui.screen.stand.ge;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.GoldExperienceChooseLifeform;
import com.github.standobyte.jojo.action.stand.GoldExperienceCreateLifeform;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.InputHandler.MouseButton;
import com.github.standobyte.jojo.client.ui.screen.GridList;
import com.github.standobyte.jojo.client.ui.screen.GridList.ElemMoveMode;
import com.github.standobyte.jojo.client.ui.screen.widgets.FilterList;
import com.github.standobyte.jojo.client.ui.screen.ScreenCloseMode;
import com.github.standobyte.jojo.client.ui.screen.WasdAllowingScreen;
import com.github.standobyte.jojo.client.ui.tooltip.CustomTooltipRender;
import com.github.standobyte.jojo.client.ui.tooltip.ITooltipLine;
import com.github.standobyte.jojo.client.ui.tooltip.IconTooltipLine;
import com.github.standobyte.jojo.client.ui.tooltip.MultiTooltipLine;
import com.github.standobyte.jojo.client.ui.tooltip.TextTooltipLine;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClAllGELifeformsButtonPacket;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.EntityTypeToInstance;
import com.github.standobyte.jojo.util.mod.JojoModUtil.Direction2D;
import com.google.common.collect.Streams;
import com.github.standobyte.jojo.util.mod.ModInteractionUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;

public class ChooseLifeformScreen extends WasdAllowingScreen {
    public static final ResourceLocation LIFEFORM_CHOOSE_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/lifeform_choose.png");

    private GridList<SelectorWidget> entityIconsGrid;
    
    @Nullable private EntityType<?> chosenLifeformCache;
    
    private int firstMouseX;
    private int firstMouseY;
    private boolean ignoreMouseUntilMoved;
    private boolean firstInit = true;
    
    private static boolean restoreSavedMousePos;
    private static int savedMouseX;
    private static int savedMouseY;
    private static int savedColumn;
    private static String savedSearchFilter = "";
    
    private FilterList<FilterEntry> filterList;
    private TextFieldWidget searchField;
    private Button clearSearchFieldButton;
    private Button unlockAllButton;
    
    public ChooseLifeformScreen(KeyBinding keyHeld) {
        super(StringTextComponent.EMPTY);
        this.keyHeld = keyHeld;
    }
    
    
    @Override
    protected void init() {
        super.init();
        
        initEntityTypes();

        ITextComponent messageListShow = new TranslationTextComponent("jojo.ge_lifeform.filter_list.show");
        ITextComponent messageListHide = new TranslationTextComponent("jojo.ge_lifeform.filter_list.hide");
        addButton(new ImageButton(width - 101, height - 48, 20, 20, 48, 88, 20, LIFEFORM_CHOOSE_LOCATION, 128, 128, 
                button -> {
                    filterList.visible = !filterList.visible;
                    button.setMessage(filterList.visible ? messageListHide : messageListShow);
                }, 
                ClientUtil.buttonMessageTooltip(this), messageListShow));
        
        addButton(new ImageButton(width - 76, height - 48, 20, 20, 68, 88, 20, LIFEFORM_CHOOSE_LOCATION, 128, 128, 
                button -> {
                    Predicate<SelectorWidget> filter = entityIconsGrid.getFilter();
                    entityIconsGrid.forEach(widget -> {
                        if (filter == null || filter.test(widget)) {
                            showEntry(widget, false);
                        }
                    });
                }, ClientUtil.buttonMessageTooltip(this), new TranslationTextComponent("jojo.ge_lifeform.show_all")));
        
        addButton(new ImageButton(width - 51, height - 48, 20, 20, 88, 88, 20, LIFEFORM_CHOOSE_LOCATION, 128, 128, 
                button -> {
                    Predicate<SelectorWidget> filter = entityIconsGrid.getFilter();
                    entityIconsGrid.forEach(widget -> {
                        if (filter == null || filter.test(widget)) {
                            hideEntry(widget);
                        }
                    });
                }, ClientUtil.buttonMessageTooltip(this), new TranslationTextComponent("jojo.ge_lifeform.hide_all")));
        
        addButton(new ImageButton(width - 26, height - 48, 20, 20, 108, 88, 20, LIFEFORM_CHOOSE_LOCATION, 128, 128, 
                button -> {
                    setSearchFieldVisible(!searchField.visible);
                }, ClientUtil.buttonMessageTooltip(this), new TranslationTextComponent("jojo.ge_lifeform.search_field")));
        
        addButton(unlockAllButton = new Button(width - 101, height - 24, 95, 20, new TranslationTextComponent("jojo.ge_lifeform.unlock_all"), 
                button -> {
                    GoldExperienceChooseLifeform.unlockAllEntityTypes(minecraft.player);
                    PacketManager.sendToServer(new ClAllGELifeformsButtonPacket());
                    initEntityTypes();
                }));
        unlockAllButton.visible = minecraft.player.abilities.instabuild;
        
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
        
        if (firstInit) {
            getEntriesUiData(minecraft.player).ifPresent(playerData -> {
                EntityType<?> chosenLifeform = playerData.getGEChosenLifeformType();
                if (chosenLifeform != null) {
                    entityIconsGrid.setSelected(entityIconsGrid.findFirst(
                            widget -> widget.entityType == chosenLifeform));
                    entityIconsGrid.setLeftMostColumn(savedColumn);
                    if (restoreSavedMousePos) {
                        ClientUtil.setMousePos(savedMouseX, savedMouseY);
                        restoreSavedMousePos = false;
                    }
                }
            });
            
            firstInit = false;
        }
    }
    
    
    
    private static final int CHECKBOX_SIZE = 20;
    private void initEntityTypes() {
        LazyOptional<PlayerUtilCap> metEntityTypesCap = minecraft.player.getCapability(PlayerUtilCapProvider.CAPABILITY);
        
        List<EntityType<?>> entityTypes = ForgeRegistries.ENTITIES.getValues()
                .stream()
                .filter(type -> 
                    GeneralUtil.orElseFalse(metEntityTypesCap, cap -> cap.didPlayerMeetEntityType(type))
                    && GoldExperienceChooseLifeform.isValidLifeform(type, minecraft.level))
                .sorted(widgetSortComparator())
                .collect(Collectors.toList());
        
        initSelectionGrid(entityTypes);
        ignoreMouseUntilMoved = true;
        
        List<FilterEntry> filterListEntries = Streams.mapWithIndex(
                entityTypes.stream(), 
                (entityType, i) -> new FilterEntry(entityType, 
                        new LifeformFilterListCheckbox(width - 28, -1, CHECKBOX_SIZE, CHECKBOX_SIZE, 
                                entityType.getDescription(), 
                                
                                () -> ChooseLifeformScreen.getEntriesUiData(ClientUtil.getClientPlayer())
                                        .map(cap -> !cap.isGELifeformHidden(entityType)).orElse(false),
                                
                                stateBeingSet -> {
                                    if (stateBeingSet) {
                                        showEntry(entityType, true);
                                    }
                                    else {
                                        hideEntry(entityType);
                                    }
                                })))
                .collect(Collectors.toCollection(ArrayList::new));
        
        filterList = new FilterList<>(filterListEntries, width - 108, 36, 100, height - 58, CHECKBOX_SIZE);
        addWidget(filterList);
    }
    
    private void initSelectionGrid(List<EntityType<?>> entityTypes) {
        int xMin = 36;
        int xMax = width - 136;
        int xMiddle = width / 2;
        
        entityIconsGrid = GridList.create(entityTypes, SelectorWidget::new, Math.max((height - 46) / 30, 1), this, this::addButton);
        entityIconsGrid.forEach(widget -> widget.setHidden(getEntriesUiData(minecraft.player).map(
                playerData -> playerData.isGELifeformHidden(widget.entityType)).orElse(true)));
        
        int columnsCount = entityIconsGrid.getColumnsCount();
        int columnsCanFit = (xMax - xMin) / 30;
        int x;
        if (columnsCanFit <= columnsCount) {
            x = xMin;
        }
        else {
            x = MathHelper.clamp(xMiddle - (columnsCount * 15 - 3), xMin, xMax);
        }
        entityIconsGrid.x = x;
        entityIconsGrid.y = 8;
        entityIconsGrid.columnWidth = 24;
        entityIconsGrid.columnGap = 6;
        entityIconsGrid.rowHeight = 24;
        entityIconsGrid.rowGap = 6;
        entityIconsGrid.setMaxWidth(xMax + 36 - x);
    }
    
    private Comparator<EntityType<?>> widgetSortComparator() {
        return Comparator.comparing(type -> type.getDescription().getString(), String::compareTo);
    }
    
    
    private static void saveMousePos(int mouseX, int mouseY) {
        restoreSavedMousePos = true;
        savedMouseX = mouseX;
        savedMouseY = mouseY;
    }
    
    
    
    private void setSearchFieldVisible(boolean setVisible) {
        if (searchField.visible ^ setVisible) {
            searchField.visible = setVisible;
            clearSearchFieldButton.visible = setVisible;
            filterList.setYBottom(setVisible ? height - ((40 + searchField.getHeight()) / 20 * 20 + 18) : height - 58);
            
            setFocused(setVisible ? searchField : null);
            searchField.setFocus(setVisible);
        }
    }
    
    @Override
    public void setFocused(@Nullable IGuiEventListener pListener) {
        if (pListener == null || pListener == searchField) {
            doSetFocused(pListener);
        }
        else {
            IGuiEventListener focused = getFocused();
            if (focused == searchField) {
                searchField.setFocus(true);
            }
        }
    }
    
    private void filterEntries(String field) {
        boolean emptyQuery = field == null || field.isEmpty();
        Predicate<EntityType<?>> filter = emptyQuery ? null : 
            entityType -> {
                String searchLC = field.toLowerCase();
                return entityType.getDescription().getString().toLowerCase().contains(searchLC)
                || ModInteractionUtil.getModName(entityType.getRegistryName()).toLowerCase().contains(searchLC)
                || entityType.getRegistryName().toString().contains(searchLC);
            };
        entityIconsGrid.setFilter(GeneralUtil.mapPredicate(filter, widget -> widget.entityType));
        filterList.setFilter(GeneralUtil.mapPredicate(filter, entry -> entry.entityType));
        entityIconsGrid.setShowHidden(!emptyQuery);
    }
    
    
    
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
        if (!holdsButton && mode == ScreenCloseMode.HOLD) {
            chooseHoveredAndClose();
            saveMousePos(mouseX, mouseY);
        }
        else {
            chosenLifeformCache = getEntriesUiData(minecraft.player).map(
                    entityData -> entityData.getGEChosenLifeformType()).orElse(null);
            updateHoveredElement(mouseX, mouseY);
            entityIconsGrid.renderGrid(matrixStack, mouseX, mouseY, partialTicks);
            searchField.render(matrixStack, mouseX, mouseY, partialTicks);
            super.render(matrixStack, mouseX, mouseY, partialTicks);
            renderHoveredTooltip(matrixStack, mouseX, mouseY, partialTicks);
            filterList.render(matrixStack, minecraft, mouseX, mouseY, partialTicks);
        }
    }
    
    private void updateHoveredElement(int mouseX, int mouseY) {
        boolean mouseMoved = checkMouseMoved(mouseX, mouseY);
        entityIconsGrid.forEach(widget -> {
            if (widget.visible) {
                widget.updateIsHovered(mouseX, mouseY);
                if (mouseMoved && widget.isHovered() && !widget.isSelected) {
                    entityIconsGrid.setSelected(widget);
                }
                entityIconsGrid.getSelected().ifPresent(w -> {
                    widget.setSelected(widget == w);
                });
            }
        });
    }
    
    private boolean checkMouseMoved(int mouseX, int mouseY) {
        if (ignoreMouseUntilMoved) {
            firstMouseX = mouseX;
            firstMouseY = mouseY;
            ignoreMouseUntilMoved = false;
        }
        return firstMouseX != mouseX || firstMouseY != mouseY;
    }
    
    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("0.0");
    private void renderHoveredTooltip(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        entityIconsGrid.getSelected().ifPresent(widget -> {
            int x;
            int y;
            if (checkMouseMoved(mouseX, mouseY)) {
                if (entityIconsGrid.isMouseInsideGrid(mouseX, mouseY)) {
                    x = mouseX;
                    y = mouseY;
                }
                else {
                    return;
                }
            }
            else {
                x = widget.x + 18;
                y = widget.y + 16;
            }
            
            
            
            List<ITooltipLine> entityTypeInfo = new ArrayList<>();
            
            entityTypeInfo.add(new TextTooltipLine(widget.getMessage()));
            
            entityTypeInfo.add(new TextTooltipLine(new StringTextComponent(ModInteractionUtil.getModName(widget.entityType.getRegistryName()))
                    .withStyle(TextFormatting.BLUE, TextFormatting.ITALIC)));
            
            Entity entity = EntityTypeToInstance.getEntityInstance(widget.entityType, minecraft.level);
            String width = SIZE_FORMAT.format(entity.getBbWidth());
            String height = SIZE_FORMAT.format(entity.getBbHeight());
            double strength = GoldExperienceCreateLifeform.getAttackStrength(entity);
            int creationTicks = GoldExperienceCreateLifeform.getTicksToCreate(minecraft.player, ClientUtil.getStandPowerClCached(), entity);
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
                    new TextTooltipLine(new TranslationTextComponent("gold_experience.lifeform_time", creationSecs))));
            
//            entityTypeInfo.add(new TextTooltipLine(new StringTextComponent(String.valueOf(GoldExperienceCreateLifeform.getVolume(entity)))));
            
            entityTypeInfo.stream().map(line -> line.getWidth(font)).max(Comparator.naturalOrder()).ifPresent(tooltipWidth -> {
                CustomTooltipRender.renderWrappedToolTip(matrixStack, entityTypeInfo, x, y, font);
            });
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
        
        if (entityIconsGrid.getSelected().isPresent() && entityIconsGrid.isMouseInsideGrid(mouseX, mouseY)) {
            SelectorWidget hovered = entityIconsGrid.getSelected().get();
            MouseButton button = MouseButton.getButtonFromId(buttonId);
            if (button == null) return false;
            
            switch (button) {
            case LEFT:
                chooseHoveredAndClose();
                saveMousePos((int) mouseX, (int) mouseY);
                return true;
            case RIGHT:
                getEntriesUiData(minecraft.player).ifPresent(playerData -> {
                    if (playerData.isGELifeformHidden(hovered.entityType)) {
                        showEntry(hovered, false);
                    }
                    else {
                        hideEntry(hovered);
                    }
                });
                return true;
            default:
                break;
            }
        }
        
        return false;
    }
    
    private static final Int2ObjectMap<Direction2D> ARROW_KEYS = Util.make(new Int2ObjectOpenHashMap<>(), map -> {
        map.put(GLFW.GLFW_KEY_LEFT,  Direction2D.LEFT);
        map.put(GLFW.GLFW_KEY_UP,    Direction2D.UP);
        map.put(GLFW.GLFW_KEY_RIGHT, Direction2D.RIGHT);
        map.put(GLFW.GLFW_KEY_DOWN,  Direction2D.DOWN);
    });
    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (handleArrowKey(pKeyCode, pScanCode, pModifiers)) {
            ignoreMouseUntilMoved = true;
            return true;
        }
        if (pKeyCode == GLFW.GLFW_KEY_ENTER || pKeyCode == GLFW.GLFW_KEY_KP_ENTER) {
            chooseHoveredAndClose();
            return true;
        }
        
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
    
    private boolean handleArrowKey(int pKeyCode, int pScanCode, int pModifiers) {
        if (!ARROW_KEYS.containsKey(pKeyCode)) return false;
        
        boolean control = (pModifiers & GLFW.GLFW_MOD_CONTROL) > 0;
        Direction2D direction = ARROW_KEYS.get(pKeyCode);
        entityIconsGrid.moveSelection(direction, control ? ElemMoveMode.EDGE : ElemMoveMode.NEIGHBOR_WRAP);
        
        return true;
    }
    
    
    private void chooseHoveredAndClose() {
        entityIconsGrid.getSelected().ifPresent(widget -> {
            minecraft.player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(
                    cap -> cap.setGEChosenLifeformType(widget.entityType, true));
        });
        setSearchFieldVisible(false);
        onClose();
    }
    
    @Override
    public void onClose() {
        if (searchField.visible) {
            setSearchFieldVisible(false);
        }
        else {
            super.onClose();
            savedColumn = entityIconsGrid.getLeftMostColumn();
            savedSearchFilter = searchField.getValue();
        }
    }
    
    public void hideEntry(EntityType<?> entityType) {
        entityIconsGrid.findFirst(widget -> widget.entityType == entityType).ifPresent(
                widget -> hideEntry(widget));
    }
    
    public void hideEntry(SelectorWidget entityTypeWidget) {
        getEntriesUiData(minecraft.player).ifPresent(playerData -> {
            if (playerData.hideGELifeform(entityTypeWidget.entityType)) {
                entityTypeWidget.setHidden(true);
                
                if (entityIconsGrid.getSelected().isPresent()) {
                    SelectorWidget hovered = entityIconsGrid.getSelected().get();
                    if (hovered == entityTypeWidget) {
                        entityIconsGrid.setSelected(Optional.empty());
                    }
                }
            }
        });
    }
    
    public void showEntry(EntityType<?> entityType, boolean select) {
        entityIconsGrid.findFirst(widget -> widget.entityType == entityType).ifPresent(
                widget -> showEntry(widget, select));
    }
    
    public void showEntry(SelectorWidget entityTypeWidget, boolean select) {
        getEntriesUiData(minecraft.player).ifPresent(playerData -> {
            if (playerData.showGELifeform(entityTypeWidget.entityType)) {
                entityTypeWidget.setHidden(false);
                if (select) {
                    entityIconsGrid.setSelected(entityTypeWidget);
                }
            }
        });
    }
    
    static Optional<PlayerUtilCap> getEntriesUiData(PlayerEntity clientPlayer) {
        return clientPlayer.getCapability(PlayerUtilCapProvider.CAPABILITY).resolve();
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return entityIconsGrid.onMouseScroll(mouseX, mouseY, delta) || 
                super.mouseScrolled(mouseX, mouseY, delta);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public boolean acceptsKeyInput() {
        return !searchField.canConsumeInput();
    }
    
    private class SelectorWidget extends Widget implements GridList.IGridElement {
        private final EntityType<?> entityType;
        private boolean isSelected;
        private boolean isHidden;
        
        private SelectorWidget(EntityType<?> entityType) {
            super(0, 0, 24, 24, entityType.getDescription());
            this.entityType = entityType;
        }
        
        @Override
        public void setHidden(boolean isHidden) {
            this.isHidden = isHidden;
        }
        
        @Override
        public boolean isHidden() {
            return isHidden;
        }
        
        @SuppressWarnings("deprecation")
        @Override
        public void renderButton(MatrixStack matrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
            Minecraft mc = Minecraft.getInstance();
            Minecraft.getInstance().getTextureManager().bind(LIFEFORM_CHOOSE_LOCATION);
            
            if (isHidden) {
                RenderSystem.color4f(1, 1, 1, 0.25F);
            }
            
            blit(matrixStack, x, y, 0, 0, 24, 24, 128, 128);

            EntityTypeIcon.renderIcon(entityType, matrixStack, x + 4, y + 4);
            
            if (isHidden) {
                RenderSystem.color4f(1, 1, 1, 1);
            }
            
            if (isSelected) {
                mc.getTextureManager().bind(LIFEFORM_CHOOSE_LOCATION);
                blit(matrixStack, x, y, 24, 0, 24, 24, 128, 128);
            }
            else if (this.entityType == chosenLifeformCache) {
                mc.getTextureManager().bind(LIFEFORM_CHOOSE_LOCATION);
                blit(matrixStack, x, y, 0, 24, 24, 24, 128, 128);
            }
        }
        
        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
            this.narrate();
        }
        
        public void updateIsHovered(int mouseX, int mouseY) {
            isHovered = 
                    mouseX >= x - entityIconsGrid.columnGap / 2 && 
                    mouseX <  x + width + entityIconsGrid.columnGap / 2 && 
                    mouseY >= y - entityIconsGrid.rowGap / 2 && 
                    mouseY <  y + height + entityIconsGrid.rowGap / 2;
        }
        
        
        private int column;
        private int row;
        @Override
        public int getColumn() {
            return column;
        }

        @Override
        public int getRow() {
            return row;
        }

        @Override
        public void setColumn(int column) {
            this.column = column;
        }

        @Override
        public void setRow(int row) {
            this.row = row;
        }
    }
    
    
    public static class FilterEntry implements FilterList.Entry {
        public final EntityType<?> entityType;
        private final CheckboxButton checkbox;
        
        private FilterEntry(EntityType<?> entityType, CheckboxButton checkbox) {
            this.entityType = entityType;
            this.checkbox = checkbox;
        }
        
        @Override
        public void render(MatrixStack matrixStack, Minecraft mc, int mouseX, int mouseY, float partialTick) {
            mc.textureManager.bind(ChooseLifeformScreen.LIFEFORM_CHOOSE_LOCATION);
            ClientUtil.drawRightAlignedString(matrixStack, mc.font, 
                    checkbox.getMessage(), 
                    checkbox.x - 5, checkbox.y + (CHECKBOX_SIZE - mc.font.lineHeight) / 2, 
                    0xFFFFFF);
            checkbox.render(matrixStack, mouseX, mouseY, partialTick);
        }
        
        @Override
        public void setY(int y) {
            checkbox.y = y;
        }
        
        @Override
        public void addY(int addY) {
            checkbox.y += addY;
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int buttonId) {
            return checkbox.mouseClicked(mouseX, mouseY, buttonId);
        }

        @Override
        public void setVisible(boolean isVisible) {
            checkbox.visible = isVisible;
        }
    }
}
