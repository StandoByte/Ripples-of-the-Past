package com.github.standobyte.jojo.util.general;

import java.util.Comparator;

import javax.annotation.Nullable;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;

public class DiscardingSortedMultisetWrapper<E extends Comparable<? super E>> {
    private final SortedMultiset<E> wrappedMultiset;
    private final int maxCapacity;
    
    public DiscardingSortedMultisetWrapper(int maxCapacity) {
        this(maxCapacity, null);
    }
    
    public DiscardingSortedMultisetWrapper(int maxCapacity, Comparator<E> comparator) {
        if (maxCapacity < 1) {
            throw new IllegalArgumentException("The array can't have a non-positive maximum capacity.");
        }
        this.maxCapacity = maxCapacity;
        this.wrappedMultiset = TreeMultiset.create(comparator);
    }
    
    public boolean add(E element) {
        while (wrappedMultiset.size() > maxCapacity) {
            discardMin();
        }
        if (wrappedMultiset.size() == maxCapacity) {
            E min = wrappedMultiset.firstEntry().getElement();
            if (element.compareTo(min) < 0) {
                return false;
            }
            wrappedMultiset.remove(min);
        }
        return wrappedMultiset.add(element);
    }
    
    public void discardMin() {
        wrappedMultiset.pollFirstEntry();
    }
    
    public int size() {
        return wrappedMultiset.size();
    }
    
    public boolean isEmpty() {
        return wrappedMultiset.isEmpty();
    }

    @Nullable
    public E getMax() {
        Multiset.Entry<E> entry = wrappedMultiset.lastEntry();
        return entry == null ? null : entry.getElement();
    }
    
    public void clear() {
        wrappedMultiset.clear();
    }

    public SortedMultiset<E> getWrappedSet() {
        return Multisets.unmodifiableSortedMultiset(wrappedMultiset);
    }
}
