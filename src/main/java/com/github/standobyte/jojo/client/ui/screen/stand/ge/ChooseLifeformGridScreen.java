package com.github.standobyte.jojo.client.ui.screen.stand.ge;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.action.stand.GoldExperienceChooseLifeform;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.InputHandler.MouseButton;
import com.github.standobyte.jojo.client.ui.screen.GridList;
import com.github.standobyte.jojo.client.ui.screen.GridList.ElemMoveMode;
import com.github.standobyte.jojo.client.ui.tooltip.ITooltipLine;
import com.github.standobyte.jojo.client.ui.tooltip.TextTooltipLine;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.entitysubtype.EntitySubtype;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

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
            EntitySubtype<?> chosenLifeform = playerUISettings.getGEChosenLifeformType();
            if (chosenLifeform != null) {
                entityIconsGrid.setSelected(entityIconsGrid.findFirst(
                        widget -> widget.entityType.entityType == chosenLifeform.vanillaType));
                entityIconsGrid.setLeftMostColumn(savedColumn);
            }
            
            firstInit = false;
        }
        
        addCommonWidgets(ViewMode.GRID);
        addSearchField();
    }
    
    @Override
    public void refreshEntityTypes() {
        PlayerUtilCap metLifeforms = minecraft.player.getCapability(PlayerUtilCapProvider.CAPABILITY).resolve().get();
        
        List<LifeformEntityTypeEntry> entries = EntitySubtype.values()
                .filter(subtype -> metLifeforms.metEntityType(subtype)
                        && GoldExperienceChooseLifeform.isValidLifeform(subtype, minecraft.level))
                .collect(Collectors.groupingBy(subtype -> subtype.vanillaType))
                .entrySet()
                .stream()
                .map(entry -> new LifeformEntityTypeEntry(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(entry -> entry.entityType, ENTITY_MOD_NAME_COMPARE.thenComparing(ENTITY_NAME_COMPARE)))
                .collect(Collectors.toList());
        
        initSelectionGrid(entries);
//        ignoreMouseUntilMoved = true;
    }
    
    
    private void initSelectionGrid(List<LifeformEntityTypeEntry> entityTypes) {
        int xMin = 36;
        int xMax = width - 136;
        int xMiddle = width / 2;
        
        entityIconsGrid = GridList.create(entityTypes, 
                entityType -> {
                    SelectorWidget widget = new SelectorWidget(entityType);
                    widget.isNew = playerUISettings.isGELifeformNew(entityType.entityType);
                    widget.isInFavorite = playerUISettings.isGELifeformInFavorites(entityType.entityType);
                    return widget;
                }, 
                Math.max((height - 46) / 30, 1), this, this::addButton);
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
    
    private Predicate<EntityType<?>> searchBarFilter;
    @Override
    protected void searchBarFilter(@Nullable Predicate<EntityType<?>> filter) {
        this.searchBarFilter = filter;
        doFilter();
    }
    
    @Override
    protected void onFilterRadioButton() {
        doFilter();
    }
    
    protected void doFilter() {
        Predicate<EntityType<?>> filter;
        switch (filterList.getSelectedValue()) {
        case FAVORITES:
            filter = playerUISettings::isGELifeformInFavorites;
            break;
        case NEW:
            filter = playerUISettings::isGELifeformNew;
            break;
        default:
            filter = e -> true;
            break;
        }
        if (searchBarFilter != null) {
            filter = filter.and(searchBarFilter);
        }
        entityIconsGrid.setFilter(GeneralUtil.mapPredicate(filter, widget -> widget.entityType.entityType));
    }
    
    
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        updateHoveredElement(mouseX, mouseY);
        entityIconsGrid.renderGrid(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        entityIconsGrid.getSelected().ifPresent(widget -> renderHoveredTooltip(matrixStack, widget.entityType.getCurrentSubtype(), mouseX, mouseY));
    }
    
    @Override
    protected List<ITooltipLine> makeHoveredTooltip(EntitySubtype<?> entityType) {
        List<ITooltipLine> tooltip = super.makeHoveredTooltip(entityType);
        tooltip.add(new TextTooltipLine(StringTextComponent.EMPTY));
        
        ITextComponent favHint = (playerUISettings.isGELifeformInFavorites(entityType.vanillaType) ? 
                new TranslationTextComponent("jojo.ge_lifeform.grid_fav_remove") : new TranslationTextComponent("jojo.ge_lifeform.grid_fav_add"))
                .withStyle(TextFormatting.DARK_GRAY, TextFormatting.ITALIC);
        int width = tooltip.stream().mapToInt(line -> line.getWidth(minecraft.font)).max().orElse(-1);
        if (width > -1) {
            minecraft.font.getSplitter().splitLines(favHint, width, Style.EMPTY)
                .forEach(line -> tooltip.add(new TextTooltipLine(line)));
        }
        else {
            tooltip.add(new TextTooltipLine(favHint));
        }
        
        return tooltip;
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
    protected void renderHoveredTooltip(MatrixStack matrixStack, EntitySubtype<?> entityType, int mouseX, int mouseY) {
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
                if (hovered.isInFavorite) {
                    playerUISettings.GELifeformRemoveFav(hovered.entityType.entityType);
                    hovered.isInFavorite = false;
                }
                else {
                    playerUISettings.GELifeformAddFav(hovered.entityType.entityType);
                    hovered.isInFavorite = true;
                }
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
            playerUISettings.setGEChosenLifeformType(widget.entityType.getCurrentSubtype(), true);
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
        private final LifeformEntityTypeEntry entityType;
        private boolean isSelected;
        private boolean isHidden;
        private boolean isNew;
        private boolean isInFavorite;
        
        private SelectorWidget(LifeformEntityTypeEntry entityType) {
            super(0, 0, 24, 24, entityType.entityType.getDescription());
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
            
            if (isNew) {
                blit(matrixStack, x, y, 24, 24, 24, 24, 128, 128);
            }
            else {
                blit(matrixStack, x, y, 0, 0, 24, 24, 128, 128);
            }

            EntityTypeIcon.renderIcon(entityType.getCurrentSubtype(), matrixStack, x + 4, y + 4);
            
            if (isHidden) {
                RenderSystem.color4f(1, 1, 1, 1);
            }
            
            if (isSelected) {
                mc.getTextureManager().bind(LIFEFORM_CHOOSE_LOCATION);
                blit(matrixStack, x, y, 24, 0, 24, 24, 128, 128);
            }
            else if (this.entityType.getCurrentSubtype() == playerUISettings.getGEChosenLifeformType()) {
                mc.getTextureManager().bind(LIFEFORM_CHOOSE_LOCATION);
                blit(matrixStack, x, y, 0, 24, 24, 24, 128, 128);
            }
            
            if (isInFavorite) {
                mc.getTextureManager().bind(LIFEFORM_CHOOSE_LOCATION);
                blit(matrixStack, x + width - 5, y - 3, 119, 9, 9, 9, 128, 128);
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
