package com.github.standobyte.jojo.client.ui.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

import com.github.standobyte.jojo.util.mod.JojoModUtil.Direction2D;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;

public class GridList<T extends Widget & GridList.IGridElement> {
    @SuppressWarnings("unused")
    private final Screen screen;
    private final Button scrollLeftButton;
    private final Button scrollRightButton;
    private final List<T> allElements;
    private final int maxColumnSize;
    private int visibleElementsCount;
    
    private Optional<T> selected = Optional.empty();
    
    public int x;
    public int y;
    public int columnWidth;
    public int columnGap;
    public int rowHeight;
    public int rowGap;
    private OptionalInt maxWidth = OptionalInt.empty();
    private int leftMostColumn = 0;
    
    @Nullable private Predicate<T> filter;
    private boolean showHidden;
    
    public static <O, T extends Widget & GridList.IGridElement> GridList<T> create(Iterable<O> originalObjects, 
            Function<O, T> createElement, int maxColumnSize, 
            Screen screen, Consumer<Button> addButtons) {
        List<T> elements = new ArrayList<>();
        
        for (O obj : originalObjects) {
            T elem = createElement.apply(obj);
            elements.add(elem);
        }
        
        GridList<T> gridList = new GridList<T>(elements, maxColumnSize, screen);
        addButtons.accept(gridList.scrollLeftButton);
        addButtons.accept(gridList.scrollRightButton);
        return gridList;
    }

    public static <T extends Widget & GridList.IGridElement> GridList<T> create(Iterable<T> elements, int maxColumnSize, 
            Screen screen, Consumer<Button> addButtons) {
        return create(elements, Function.identity(), maxColumnSize, screen, addButtons);
    }
    
    private GridList(List<T> elementsList, int maxColumnSize, Screen screen) {
        this.screen = screen;
        this.allElements = elementsList;
        this.maxColumnSize = maxColumnSize;
        this.visibleElementsCount = (int) elementsList.stream().filter(e -> e.visible).count();
        this.scrollLeftButton =  new Button(-1, -1, 20, 20, new StringTextComponent("<"), b -> scrollColumns(-1));
        this.scrollRightButton = new Button(-1, -1, 20, 20, new StringTextComponent(">"), b -> scrollColumns(1));
    }
    
    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth > 0 ? OptionalInt.of(maxWidth) : OptionalInt.empty();
    }
    
    private int getMaxRenderedColumns() {
        return maxWidth.isPresent() ? maxWidth.getAsInt() / (columnWidth + columnGap) : 999999;
    }
    

    
    public void updateGridLayout() {
        int xDiff = columnWidth + columnGap;
        int yDiff = rowHeight + rowGap;
        
        MutableInt visibleElements = new MutableInt();
        MutableBoolean elementOutOfBounds = new MutableBoolean(false);
        
        for (T element : allElements) {
            boolean passesFilter = filter == null || filter.test(element);
            boolean hiddenCheck = showHidden || !element.isHidden();
            if (passesFilter && hiddenCheck) {
                int index = visibleElements.getValue();
                
                int row = index % maxColumnSize;
                int column = index / maxColumnSize;
                element.setRow(row);
                element.setColumn(column);
                element.x = this.x + column * xDiff;
                element.y = this.y + row * yDiff;
                if (maxWidth.isPresent()) {
                    element.x -= leftMostColumn * xDiff;
                }
                boolean outOfBounds = elemOutOfBounds(element);
                if (outOfBounds) {
                    element.visible = false;
                    elementOutOfBounds.setTrue();
                }
                else {
                    element.visible = true;
                }
                
                visibleElements.increment();
            }
            else {
                element.visible = false;
                element.setRow(-1);
                element.setColumn(-1);
                element.x = -999;
                element.y = -999;
            }
        }
        
        this.visibleElementsCount = visibleElements.getValue();
        
        scrollLeftButton.x = this.x - scrollLeftButton.getWidth() - 4;
        scrollLeftButton.y = this.y + maxColumnSize * (rowHeight + rowGap) - rowGap - scrollLeftButton.getHeight();
        scrollRightButton.x = this.x + maxWidth.orElse(0) + 4;
        scrollRightButton.y = this.y + maxColumnSize * (rowHeight + rowGap) - rowGap - scrollRightButton.getHeight();
        scrollLeftButton.visible = scrollRightButton.visible = elementOutOfBounds.booleanValue();
        scrollLeftButton.active = leftMostColumn > 0;
        scrollRightButton.active = leftMostColumn < getColumnsCount() - getMaxRenderedColumns() - 1;
    }
    
    private void doRender(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.enableBlend();
        
        for (T element : allElements) {
            if (element.visible) {
                element.render(matrixStack, mouseX, mouseY, partialTicks);
            }
        }
        
        RenderSystem.disableBlend();
    }
    
    public void renderGrid(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        updateGridLayout();
        doRender(matrixStack, mouseX, mouseY, partialTicks);
    }
    
    public Optional<T> getVisibleAt(int row, int column) {
        for (T element : allElements) {
            if (element.visible && element.getRow() == row && element.getColumn() == column) {
                return Optional.of(element);
            }
        }
        
        return Optional.empty();
    }
    
    private boolean elemOutOfBounds(T element) {
        int column = element.getColumn();
        return maxWidth.isPresent() && (column - leftMostColumn < 0 || column - leftMostColumn > getMaxRenderedColumns());
    }
    
    public void forEach(Consumer<T> action) {
        for (T element : allElements) {
            action.accept(element);
        }
    }
    
    public boolean isMouseInsideGrid(double mouseX, double mouseY) {
        int mouseColumn = MathHelper.floor((mouseX - x + columnGap * 0.5) / (columnWidth + columnGap));
        int mouseRow    = MathHelper.floor((mouseY - y + rowGap * 0.5)    / (rowHeight   + rowGap));
        int columnsCount = Math.min(getMaxRenderedColumns() + 1, getColumnsCount());
        int rowsCount = getColumnSize(mouseColumn);
        return mouseColumn >= 0 && mouseColumn < columnsCount
                && mouseRow >= 0 && mouseRow < rowsCount;
    }
    
    public boolean onMouseScroll(double mouseX, double mouseY, double delta) {
        return scrollLeftButton.visible && scrollColumns(delta < 0 ? 1 : -1);
    }
    
    public boolean isEmpty() {
        return visibleElementsCount == 0;
    }
    
    public int getColumnsCount() {
        int columns = visibleElementsCount / maxColumnSize;
        if (visibleElementsCount % maxColumnSize > 0) columns++;
        return columns;
    }
    
    public int getColumnSize(int column) {
        int columnsCount;
        if (column < 0 || column >= (columnsCount = getColumnsCount())) {
            return -1;
        }
        if (column < columnsCount - 1) {
            return maxColumnSize;
        }
        return (visibleElementsCount - 1) % maxColumnSize + 1;
    }
    
    public void setFilter(@Nullable Predicate<T> filter) {
        this.filter = filter;
        updateGridLayout();
    }
    
    @Nullable
    public Predicate<T> getFilter() {
        return filter;
    }
    
    public void setShowHidden(boolean showHidden) {
        this.showHidden = showHidden;
    }
    
    public void moveSelection(Direction2D direction, ElemMoveMode mode) {
        if (isEmpty()) {
            return;
        }
        if (!selected.isPresent()) {
            selected = getVisibleAt(0, 0);
            if (mode == ElemMoveMode.NEIGHBOR_WRAP) {
                mode = ElemMoveMode.NEIGHBOR;
            }
        }

        T initialObj = selected.get();
        List<T> visible = allElements.stream().filter(widget -> widget.visible)
                .collect(Collectors.toList());
        int i = visible.indexOf(initialObj);
        if (i < 0) {
            return;
        }

        int row = i % maxColumnSize;
        int column = i / maxColumnSize;
        int thisColumnSize = getColumnSize(column);
        int columnsCount = getColumnsCount();
        if (column < columnsCount - 1 && getColumnSize(columnsCount - 1) <= row) {
            columnsCount--;
        }
        
        // cursed
        switch (direction) {
        case LEFT:
            switch (mode) {
            case NEIGHBOR:
                column = Math.max(column - 1, 0);
                break;
            case NEIGHBOR_WRAP:
                column = (column - 1 + columnsCount) % columnsCount;
                break;
            case EDGE:
                column = 0;
                break;
            }
            break;
            
        case RIGHT:
            switch (mode) {
            case NEIGHBOR:
                column = Math.min(column + 1, columnsCount - 1);
                break;
            case NEIGHBOR_WRAP:
                column = (column + 1) % columnsCount;
                break;
            case EDGE:
                column = columnsCount - 1;
                break;
            }
            break;
            
        case UP:
            switch (mode) {
            case NEIGHBOR:
                row = Math.max(row - 1, 0);
                break;
            case NEIGHBOR_WRAP:
                row = (row - 1 + thisColumnSize) % thisColumnSize;
                break;
            case EDGE:
                row = 0;
                break;
            }
            break;
            
        case DOWN:
            switch (mode) {
            case NEIGHBOR:
                row = Math.min(row + 1, thisColumnSize - 1);
                break;
            case NEIGHBOR_WRAP:
                row = (row + 1) % thisColumnSize;
                break;
            case EDGE:
                row = thisColumnSize - 1;
                break;
            }
            break;
        }
        
        Optional<T> newPosElem = getVisibleAt(row, column);
        if (newPosElem.isPresent()) {
            setSelected(newPosElem);
        }
    }
    
    public enum ElemMoveMode {
        NEIGHBOR,
        NEIGHBOR_WRAP,
        EDGE
    }
    
    public Optional<T> getSelected() {
        return selected;
    }
    
    public void setSelected(Optional<T> element) {
        this.selected = element;
        if (element.isPresent() && maxWidth.isPresent()) {
            int column = element.get().getColumn();
            this.leftMostColumn = MathHelper.clamp(leftMostColumn, Math.max(column - getMaxRenderedColumns(), 0), column);
        }
    }
    
    public void setSelected(T element) {
        if (element == null) {
            setSelected(Optional.empty());
        }
        if (element.visible && element.active
                && !getSelected().map(curSelected -> curSelected == element).orElse(false)) {
            setSelected(Optional.of(element));
        }
    }
    
    private boolean scrollColumns(int add) {
        if (maxWidth.isPresent()) {
            int prev = this.leftMostColumn;
            setLeftMostColumn(leftMostColumn + add);
            if (prev != this.leftMostColumn) {
                updateGridLayout();
                getSelected().ifPresent(selected -> {
                    if (elemOutOfBounds(selected)) {
                        setSelected(getVisibleAt(
                                selected.getRow(), 
                                MathHelper.clamp(selected.getColumn(), leftMostColumn, leftMostColumn + getMaxRenderedColumns())));
                    }
                });
                return true;
            }
        }
        return false;
    }
    
    public void setLeftMostColumn(int leftColumn) {
        this.leftMostColumn = MathHelper.clamp(leftColumn, 0, Math.max(getColumnsCount() - getMaxRenderedColumns() - 1, 0));
    }
    
    public int getLeftMostColumn() {
        return leftMostColumn;
    }
    
    
    
    public Optional<T> findFirst(Predicate<T> predicate) {
        for (T element : allElements) {
            if (predicate.test(element)) {
                return Optional.of(element);
            }
        }
        
        return Optional.empty();
    }
    
    
    
    public static interface IGridElement {
        int getColumn();
        int getRow();
        void setColumn(int column);
        void setRow(int row);
        
//        void setShouldRender(boolean shouldRender);
//        boolean shouldRender();
        
        void setHidden(boolean isHidden);
        boolean isHidden();
    }
}
