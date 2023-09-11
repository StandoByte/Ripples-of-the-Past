package com.github.standobyte.jojo.client.ui.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.github.standobyte.jojo.client.ui.screen.GridList.IGridElement;
import com.github.standobyte.jojo.util.mod.JojoModUtil.Direction2D;

public class GridList<T extends IGridElement> {
    private final List<List<T>> elementGrid;
    private final int rowMaxCount;

    public static <O, T extends IGridElement> GridList<T> create(Iterable<O> originalObjects, int maxColumnSize, ElementSupplier<O, T> createElement) {
        List<List<T>> columnsList = new ArrayList<>();
        List<T> column = null;
        int columnIndex = 0;
        int rowIndex = 0;
        
        for (O obj : originalObjects) {
            T elem = createElement.create(obj, rowIndex, columnIndex);
            if (column == null) {
                column = new ArrayList<>(maxColumnSize);
            }
            column.add(elem);
            rowIndex++;

            if (rowIndex >= maxColumnSize) {
                columnsList.add(column);
                column = null;
                rowIndex = 0;
                columnIndex++;
            }
        }
        
        if (column != null && column.size() > 0) {
            columnsList.add(column);
        }
        
        return new GridList<T>(columnsList, maxColumnSize);
    }

    public static <T extends IGridElement> GridList<T> create(Iterable<T> elements, int maxColumnSize) {
        return create(elements, maxColumnSize, (obj, row, column) -> obj);
    }
    
    private GridList(List<List<T>> elementGrid, int rowsMaxCount) {
        this.elementGrid = elementGrid;
        this.rowMaxCount = rowsMaxCount;
    }
    
    public Optional<T> get(int row, int column) {
        if (row < 0 || row >= rowMaxCount || column < 0 || column >= elementGrid.size()) {
            return Optional.empty();
        }
        List<T> elementsColumn = elementGrid.get(column);
        if (row >= elementsColumn.size()) {
            return Optional.empty();
        }
        return Optional.of(elementsColumn.get(row));
    }
    
    public void forEach(Consumer<T> action) {
        for (List<T> column : elementGrid) {
            for (T element : column) {
                action.accept(element);
            }
        }
    }
    
    public boolean isEmpty() {
        return getColumnsCount() == 0;
    }
    
    public int getColumnsCount() {
        return elementGrid.size();
    }
    
    public int getColumnSize(int column) {
        if (column < 0 || column >= getColumnsCount()) {
            return -1;
        }
        return elementGrid.get(column).size();
    }
    
    public Optional<T> move(Optional<T> initial, Direction2D direction, boolean toTheEdge) {
        if (isEmpty() || !initial.isPresent()) {
            return Optional.empty();
        }

        T initialObj = initial.get();
        int column = initialObj.getColumn();
        int row = initialObj.getRow();
        
        int columnsCount = getColumnsCount();
        int currentColumnSize = elementGrid.get(column).size();
        
        switch (direction) {
        case LEFT:
            column = toTheEdge ? 0 : (column - 1 + columnsCount) % columnsCount;
            row = Math.min(row, elementGrid.get(column).size() - 1);
            break;
        case RIGHT:
            column = toTheEdge ? columnsCount - 1 : (column + 1) % columnsCount;
            row = Math.min(row, elementGrid.get(column).size() - 1);
            break;
        case UP:
            row = toTheEdge ? 0 : (row - 1 + currentColumnSize) % currentColumnSize;
            break;
        case DOWN:
            row = toTheEdge ? currentColumnSize - 1 : (row + 1) % currentColumnSize;
            break;
        }
        
        return get(row, column);
    }
    
    public Optional<T> findFirst(Predicate<T> predicate) {
        for (List<T> column : elementGrid) {
            for (T element : column) {
                if (predicate.test(element)) {
                    return Optional.of(element);
                }
            }
        }
        
        return Optional.empty();
    }
    
    
    @FunctionalInterface
    public static interface ElementSupplier<T, U extends IGridElement> {
        U create(T obj, int row, int column);
    }
    
    public static interface IGridElement {
        int getRow();
        int getColumn();
    }
}
