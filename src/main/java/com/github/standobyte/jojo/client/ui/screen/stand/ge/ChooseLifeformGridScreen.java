package com.github.standobyte.jojo.client.ui.screen.stand.ge;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.action.stand.GoldExperienceChooseLifeform;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.InputHandler.MouseButton;
import com.github.standobyte.jojo.client.ui.screen.GridList;
import com.github.standobyte.jojo.client.ui.screen.GridList.ElemMoveMode;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil.Direction2D;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;

public class ChooseLifeformGridScreen extends ChooseLifeformScreen {
    private GridList<SelectorWidget> entityIconsGrid;
    private static int savedColumn;
    
    private int firstMouseX;
    private int firstMouseY;
    private boolean ignoreMouseUntilMoved;
    
    private boolean firstInit = true;
    
    public ChooseLifeformGridScreen(KeyBinding keyHeld) {
        super(keyHeld);
    }
    
    @Override
    protected void init() {
        super.init();
        refreshEntityTypes();
        
        if (firstInit) {
            EntityType<?> chosenLifeform = playerUISettings.getGEChosenLifeformType();
            if (chosenLifeform != null) {
                entityIconsGrid.setSelected(entityIconsGrid.findFirst(
                        widget -> widget.entityType == chosenLifeform));
                entityIconsGrid.setLeftMostColumn(savedColumn);
            }
            
            firstInit = false;
        }
        
        addCommonWidgets(ViewMode.GRID);
        addSearchField();
    }
    
    @Override
    public void refreshEntityTypes() {
        LazyOptional<PlayerUtilCap> metEntityTypesCap = minecraft.player.getCapability(PlayerUtilCapProvider.CAPABILITY);
        
        List<EntityType<?>> entityTypes = ForgeRegistries.ENTITIES.getValues()
                .stream()
                .filter(type -> 
                    GeneralUtil.orElseFalse(metEntityTypesCap, cap -> cap.didPlayerMeetEntityType(type))
                    && GoldExperienceChooseLifeform.isValidLifeform(type, minecraft.level))
                .sorted(ENTITY_MOD_NAME_COMPARE.thenComparing(ENTITY_NAME_COMPARE))
                .collect(Collectors.toList());
        
        initSelectionGrid(entityTypes);
//        ignoreMouseUntilMoved = true;
    }
    
    
    private void initSelectionGrid(List<EntityType<?>> entityTypes) {
        int xMin = 36;
        int xMax = width - 136;
        int xMiddle = width / 2;
        
        entityIconsGrid = GridList.create(entityTypes, SelectorWidget::new, Math.max((height - 46) / 30, 1), this, this::addButton);
//        entityIconsGrid.forEach(widget -> widget.setHidden(playerUISettings.isGELifeformHidden(widget.entityType)));
        
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
    
    @Override
    protected void filterEntries(Predicate<EntityType<?>> filter) {
        entityIconsGrid.setFilter(GeneralUtil.mapPredicate(filter, widget -> widget.entityType));
    }
    
    
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        updateHoveredElement(mouseX, mouseY);
        entityIconsGrid.renderGrid(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        entityIconsGrid.getSelected().ifPresent(widget -> renderHoveredTooltip(matrixStack, widget.entityType, mouseX, mouseY));
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
    
    @Override
    protected void renderHoveredTooltip(MatrixStack matrixStack, EntityType<?> entityType, int mouseX, int mouseY) {
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
            SelectorWidget widget = entityIconsGrid.getSelected().get();
            x = widget.x + 18;
            y = widget.y + 16;
        }
        
        super.renderHoveredTooltip(matrixStack, entityType, x, y);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonId) {
        if (super.mouseClicked(mouseX, mouseY, buttonId)) {
            return true;
        }
        
        if (entityIconsGrid.getSelected().isPresent() && entityIconsGrid.isMouseInsideGrid(mouseX, mouseY)) {
            SelectorWidget hovered = entityIconsGrid.getSelected().get();
            MouseButton button = MouseButton.getButtonFromId(buttonId);
            if (button == null) return false;
            
            switch (button) {
            case LEFT:
                chooseHoveredAndClose();
                return true;
            case RIGHT:
                // TODO add/remove favorite
//                getEntriesUiData(minecraft.player).ifPresent(playerData -> {
//                    if (playerData.isGELifeformHidden(hovered.entityType)) {
//                        showEntry(hovered, false);
//                    }
//                    else {
//                        hideEntry(hovered);
//                    }
//                });
                return true;
            default:
                break;
            }
        }
        
        return false;
    }
    
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
        
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
    
    private static final Int2ObjectMap<Direction2D> ARROW_KEYS = Util.make(new Int2ObjectOpenHashMap<>(), map -> {
        map.put(GLFW.GLFW_KEY_LEFT,  Direction2D.LEFT);
        map.put(GLFW.GLFW_KEY_UP,    Direction2D.UP);
        map.put(GLFW.GLFW_KEY_RIGHT, Direction2D.RIGHT);
        map.put(GLFW.GLFW_KEY_DOWN,  Direction2D.DOWN);
    });
    protected boolean handleArrowKey(int pKeyCode, int pScanCode, int pModifiers) {
        if (!ARROW_KEYS.containsKey(pKeyCode)) return false;
        
        boolean control = (pModifiers & GLFW.GLFW_MOD_CONTROL) > 0;
        Direction2D direction = ARROW_KEYS.get(pKeyCode);
        entityIconsGrid.moveSelection(direction, control ? ElemMoveMode.EDGE : ElemMoveMode.NEIGHBOR_WRAP);
        
        return true;
    }
    
    
    @Override
    protected void chooseHoveredAndClose() {
        entityIconsGrid.getSelected().ifPresent(widget -> {
            playerUISettings.setGEChosenLifeformType(widget.entityType, true);
        });
        onClose();
    }
    
    @Override
    public void onClose() {
        super.onClose();
        savedColumn = entityIconsGrid.getLeftMostColumn();
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return entityIconsGrid.onMouseScroll(mouseX, mouseY, delta) || 
                super.mouseScrolled(mouseX, mouseY, delta);
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
            else if (this.entityType == playerUISettings.getGEChosenLifeformType()) {
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
}
