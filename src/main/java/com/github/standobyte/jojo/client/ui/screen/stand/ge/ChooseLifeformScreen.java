package com.github.standobyte.jojo.client.ui.screen.stand.ge;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.GoldExperienceChooseLifeform;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.InputHandler.MouseButton;
import com.github.standobyte.jojo.client.ui.screen.GridList;
import com.github.standobyte.jojo.client.ui.screen.GridList.ElemMoveMode;
import com.github.standobyte.jojo.client.ui.screen.ScreenCloseMode;
import com.github.standobyte.jojo.client.ui.screen.WasdAllowingScreen;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClAllGELifeformsButtonPacket;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.EntityTypeToInstance;
import com.github.standobyte.jojo.util.mc.MobAggroCategory;
import com.github.standobyte.jojo.util.mod.JojoModUtil.Direction2D;
import com.mojang.blaze3d.matrix.MatrixStack;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;

public class ChooseLifeformScreen extends WasdAllowingScreen {
    public static EntityType<?> chosenTypeTmp = null;
    public static Set<EntityType<?>> hiddenEntriesTmp = new HashSet<>();
    
    public static final ResourceLocation LIFEFORM_CHOOSE_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/lifeform_choose.png");

    private GridList<SelectorWidget> entityIconsGrid;
    
    private int firstMouseX;
    private int firstMouseY;
    private boolean ignoreMouseUntilMoved;
    
    private Button filterListButton;
    private FilterList filterList;
    private Button unlockAllButton;
    
    public ChooseLifeformScreen() {
        this(InputHandler.getInstance().tmp.getKey().getValue());
    }
    
    public ChooseLifeformScreen(int keyHeld) {
        super(StringTextComponent.EMPTY);
        this.keyHeld = keyHeld;
    }
    
    
    @Override
    protected void init() {
        super.init();
        
        initEntityTypes();
        
        addButton(filterListButton = new Button(width - 100, height - 48, 96, 20, new TranslationTextComponent("jojo.ge_lifeform.filter_list"), 
                button -> filterList.visible = !filterList.visible));
        
        addButton(unlockAllButton = new Button(width - 100, height - 24, 96, 20, new TranslationTextComponent("jojo.ge_lifeform.unlock_all"), 
                button -> {
                    GoldExperienceChooseLifeform.learnAllEntityTypes(minecraft.player);
                    PacketManager.sendToServer(new ClAllGELifeformsButtonPacket());
                    initEntityTypes();
                }));
        unlockAllButton.visible = minecraft.player.abilities.instabuild;
        
        if (chosenTypeTmp != null) {
            entityIconsGrid.setSelected(entityIconsGrid.findFirst(widget -> widget.visible && widget.entityType == chosenTypeTmp));
            entityIconsGrid.getSelected().ifPresent(widget -> {
                if (widget.visible) {
                    entityIconsGrid.updateGridLayout();
                    ClientUtil.setMousePos(widget.x + entityIconsGrid.columnWidth / 2, widget.y + entityIconsGrid.rowHeight / 2);
                }
            });
        }
    }
    
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
        
        MobAggroCategory.requestCategoryOnClient(entityTypes);
        filterList = new FilterList(entityTypes, width - 8, height - 52, 100, height - 128, this);
    }
    
    private void initSelectionGrid(List<EntityType<?>> entityTypes) {
        int xMin = 36;
        int xMax = width - 136;
        int xMiddle = width / 2;
        
        entityIconsGrid = GridList.create(entityTypes, SelectorWidget::new, Math.max((height - 46) / 30, 1), this, this::addButton);
        entityIconsGrid.forEach(widget -> widget.visible = !hiddenEntriesTmp.contains(widget.entityType));
        
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
    
    
    
    private int ticksKeyHeld = 0;
    private final int keyHeld;
    private ScreenCloseMode mode = ScreenCloseMode.CLICK;
    private boolean holdsButton = true;
    
    @Override
    public void tick() {
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
        return InputMappings.isKeyDown(minecraft.getWindow().getWindow(), keyHeld);
    }
    
    
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (!holdsButton && mode == ScreenCloseMode.HOLD) {
            chooseHoveredAndClose();
        }
        else {
            if (ignoreMouseUntilMoved) {
                firstMouseX = mouseX;
                firstMouseY = mouseY;
                ignoreMouseUntilMoved = false;
            }
            boolean movedMouse = firstMouseX != mouseX || firstMouseY != mouseY;
            
            entityIconsGrid.forEach(widget -> {
                if (widget.visible) {
                    if (movedMouse && widget.isHovered()) {
                        entityIconsGrid.setSelected(widget);
                    }
                    entityIconsGrid.getSelected().ifPresent(w -> {
                        widget.setSelected(widget == w);
                    });
                }
            });
            entityIconsGrid.renderGrid(matrixStack, mouseX, mouseY, partialTicks);
            super.render(matrixStack, mouseX, mouseY, partialTicks);
            
            renderHoveredTooltip(matrixStack);
            
            filterList.render(matrixStack, minecraft, mouseX, mouseY, partialTicks);
        }
    }

    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("0.0");
    private void renderHoveredTooltip(MatrixStack matrixStack) {
        entityIconsGrid.getSelected().ifPresent(widget -> {
            int x = widget.x;
            int y = widget.y;
            renderTooltip(matrixStack, widget.getMessage(), x, y);
            
            
            List<ITextComponent> rightSideInfo = new ArrayList<>();
            rightSideInfo.add(widget.getMessage());
            
            MobAggroCategory aggroCategory = MobAggroCategory.getCategoryOnClient(widget.entityType);
            if (aggroCategory != null) {
                rightSideInfo.add(aggroCategory.getName());
            }
            
            Entity entity = EntityTypeToInstance.getEntityInstance(widget.entityType, minecraft.level);
            String width = SIZE_FORMAT.format(entity.getBbWidth());
            String height = SIZE_FORMAT.format(entity.getBbHeight());
            rightSideInfo.add(new StringTextComponent(width + "x" + height + "x" + width + "m"));
            
            rightSideInfo.stream().map(line -> font.width(line)).max(Comparator.naturalOrder()).ifPresent(tooltipWidth -> {
                renderComponentTooltip(matrixStack, rightSideInfo, this.width + 4, 24);
            });
        });
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonId) {
        if (super.mouseClicked(mouseX, mouseY, buttonId)
                || filterList.mouseClicked(mouseX, mouseY, buttonId)) {
            return true;
        }
        
        if (entityIconsGrid.getSelected().isPresent() && entityIconsGrid.isMouseInsideGrid(mouseX, mouseY)) {
            SelectorWidget hovered = entityIconsGrid.getSelected().get();
            MouseButton button = MouseButton.getButtonFromId(buttonId);
            switch (button) {
            case LEFT:
                chooseHoveredAndClose();
                return true;
            case RIGHT:
                hideEntry(hovered.entityType);
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
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (handleArrowKey(pKeyCode, pScanCode, pModifiers)) {
            ignoreMouseUntilMoved = true;
            return true;
        }
        if (pKeyCode == GLFW.GLFW_KEY_ENTER || pKeyCode == GLFW.GLFW_KEY_KP_ENTER) {
            chooseHoveredAndClose();
            return true;
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
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
            chosenTypeTmp = widget.entityType;
        });
        minecraft.setScreen(null);
    }
    
    public void hideEntry(EntityType<?> entityType) {
        if (!hiddenEntriesTmp.contains(entityType)) {
            hiddenEntriesTmp.add(entityType);
            entityIconsGrid.findFirst(widget -> widget.entityType == entityType).ifPresent(
                    widget -> widget.visible = false);
            
            if (entityIconsGrid.getSelected().isPresent()) {
                SelectorWidget hovered = entityIconsGrid.getSelected().get();
                if (hovered.entityType == entityType) {
                    entityIconsGrid.setSelected(Optional.empty());
                }
            }
        }
    }
    
    public void showEntry(EntityType<?> entityType) {
        if (hiddenEntriesTmp.contains(entityType)) {
            hiddenEntriesTmp.remove(entityType);
            entityIconsGrid.findFirst(widget -> widget.entityType == entityType).ifPresent(
                    widget -> widget.visible = true);
            
            entityIconsGrid.setSelected(entityIconsGrid.findFirst(widget -> widget.entityType == entityType));
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (filterList.mouseScrolled(mouseX, mouseY, delta)
                || entityIconsGrid.mouseScrolled(mouseX, mouseY, delta)
                || super.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }
        
        
        return true;
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    private class SelectorWidget extends Widget implements GridList.IGridElement {
        private final EntityType<?> entityType;
        private boolean isSelected;
        private boolean shouldRender;
        
        private SelectorWidget(EntityType<?> entityType) {
            super(0, 0, 24, 24, entityType.getDescription());
            this.entityType = entityType;
        }
        
        @Override
        public void setShouldRender(boolean shouldRender) {
            this.shouldRender = shouldRender;
        }
        
        @Override
        public boolean shouldRender() {
            return shouldRender;
        }
        
        @Override
        public void renderButton(MatrixStack matrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
            Minecraft mc = Minecraft.getInstance();
            Minecraft.getInstance().getTextureManager().bind(LIFEFORM_CHOOSE_LOCATION);
            
            blit(matrixStack, x, y, 0, 0, 24, 24, 128, 128);

            EntityTypeIcon.renderIcon(entityType, matrixStack, x + 4, y + 4);
            
            if (isSelected()) {
                mc.getTextureManager().bind(LIFEFORM_CHOOSE_LOCATION);
                blit(matrixStack, x, y, 24, 0, 24, 24, 128, 128);
            }
            else if (this.entityType == chosenTypeTmp) {
                mc.getTextureManager().bind(LIFEFORM_CHOOSE_LOCATION);
                blit(matrixStack, x, y, 0, 24, 24, 24, 128, 128);
            }
        }
        
        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
            this.narrate();
        }
        
        public boolean isSelected() {
            return isHovered() || isSelected;
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
}
