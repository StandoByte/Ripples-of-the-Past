package com.github.standobyte.jojo.client.ui.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.util.mod.JojoModUtil.Direction2D;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.Widget;

public class GridList<T extends Widget> {
    private final List<T> allElements;
    private final int maxColumnSize;
    private int visibleElementsCount;
    
    private Optional<T> selected = Optional.empty();
    
    public int x;
    public int y;
    public int xGap;
    public int yGap;
    
    public static <O, T extends Widget> GridList<T> create(Iterable<O> originalObjects, int maxColumnSize, Function<O, T> createElement) {
        List<T> elements = new ArrayList<>();
        
        for (O obj : originalObjects) {
            T elem = createElement.apply(obj);
            elements.add(elem);
        }
        
        return new GridList<T>(elements, maxColumnSize);
    }

    public static <T extends Widget> GridList<T> create(Iterable<T> elements, int maxColumnSize) {
        return create(elements, maxColumnSize, Function.identity());
    }
    
    private GridList(List<T> elementsList, int maxColumnSize) {
        this.allElements = elementsList;
        this.maxColumnSize = maxColumnSize;
        this.visibleElementsCount = forEachVisible((element, i) -> {});
    }
    
    
    
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        visibleElementsCount = forEachVisible((element, i) -> {
            int row = i % maxColumnSize;
            int column = i / maxColumnSize;
            element.x = this.x + column * xGap;
            element.y = this.y + row * yGap;
            element.render(matrixStack, mouseX, mouseY, partialTicks);
        });
    }
    
    public Optional<T> getVisibleAt(int row, int column) {
        int i = column * maxColumnSize + row;
        if (i < 0 || i >= visibleElementsCount) {
            return Optional.empty();
        }
        
        for (T element : allElements) {
            if (element.visible && i-- == 0) {
                return Optional.of(element);
            }
        }
        
        return Optional.empty();
    }
    
    public void forEach(Consumer<T> action) {
        for (T element : allElements) {
            action.accept(element);
        }
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
        return visibleElementsCount % maxColumnSize;
    }
    
    public void moveSelection(Direction2D direction, ElemMoveMode mode) {
        if (isEmpty()) {
            return;
        }
        if (!selected.isPresent()) {
            selected = getVisibleAt(0, 0);
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
        if (getColumnSize(columnsCount - 1) <= row) {
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
    }
    
    public void setSelected(T element) {
        if (element == null) {
            setSelected(Optional.empty());
        }
        if (isElementVisible(element)) {
            setSelected(Optional.of(element));
        }
    }
    
    private int forEachVisible(BiConsumer<T, Integer> actionWithIndex) {
        int i = 0;
        for (T element : allElements) {
            if (isElementVisible(element)) {
                actionWithIndex.accept(element, i++);
            }
        }
        return i;
    }
    
    private boolean isElementVisible(T element) {
        return element.visible;
    }
    
    
    
    private OptionalInt maxColumnsRenderLimit = OptionalInt.empty();
    private int limitedColumnsFirst = 0;
    
//  public void setMaxColumns(int maxColumns) {
//      maxColumnsRenderLimit = maxColumns > 0 ? OptionalInt.of(maxColumns) : OptionalInt.empty();
//  }
    
    
    
    public Optional<T> findFirst(Predicate<T> predicate) {
        for (T element : allElements) {
            if (predicate.test(element)) {
                return Optional.of(element);
            }
        }
        
        return Optional.empty();
    }
    
    
    @FunctionalInterface
    public static interface ElementSupplier<T, U extends Widget> {
        U create(T obj);
    }
}
