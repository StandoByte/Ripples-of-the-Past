package com.github.standobyte.jojo.util.general;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LazyUnmodifiableArrayList<E> implements List<E> {
    private final List<Supplier<? extends E>> supplierList;
    private List<E> list = null;
    
    public static <E> LazyUnmodifiableArrayList<E> of(List<Supplier<? extends E>> list) {
        return new LazyUnmodifiableArrayList<>(list);
    }
    
    private LazyUnmodifiableArrayList(List<Supplier<? extends E>> supplierList) {
        if (supplierList == null) throw new NullPointerException();
        this.supplierList = supplierList;
    }
    
    private void resolveList() {
        if (list == null) {
            list = supplierList.stream().map(Supplier::get).collect(Collectors.toCollection(ArrayList::new));
        }
    }



    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        resolveList();
        return list.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        resolveList();
        return list.containsAll(c);
    }

    @Override
    public E get(int index) {
        resolveList();
        return list.get(index);
    }

    @Override
    public int indexOf(Object o) {
        resolveList();
        return list.indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        resolveList();
        return list.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        resolveList();
        return new Iterator<E>() {
            private final Iterator<? extends E> i = list.iterator();

            public boolean hasNext() {return i.hasNext();}
            public E next()          {return i.next();}
            public void remove() {
                throw new UnsupportedOperationException();
            }
            @Override
            public void forEachRemaining(Consumer<? super E> action) {
                // Use backing collection version
                i.forEachRemaining(action);
            }
        };
    }

    @Override
    public int lastIndexOf(Object o) {
        resolveList();
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator()   {return listIterator(0);}

    @Override
    public ListIterator<E> listIterator(final int index) {
        return new ListIterator<E>() {
            private final ListIterator<? extends E> i
                = list.listIterator(index);

            public boolean hasNext()     {return i.hasNext();}
            public E next()              {return i.next();}
            public boolean hasPrevious() {return i.hasPrevious();}
            public E previous()          {return i.previous();}
            public int nextIndex()       {return i.nextIndex();}
            public int previousIndex()   {return i.previousIndex();}

            public void remove() {
                throw new UnsupportedOperationException();
            }
            public void set(E e) {
                throw new UnsupportedOperationException();
            }
            public void add(E e) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void forEachRemaining(Consumer<? super E> action) {
                i.forEachRemaining(action);
            }
        };
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    // Override default methods in Collection
    @Override
    public void forEach(Consumer<? super E> action) {
        resolveList();
        list.forEach(action);
    }
    
    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Spliterator<E> spliterator() {
        resolveList();
        return (Spliterator<E>)list.spliterator();
    }
    
    @Override
    public Stream<E> stream() {
        resolveList();
        return (Stream<E>)list.stream();
    }
    
    @Override
    public Stream<E> parallelStream() {
        resolveList();
        return (Stream<E>)list.parallelStream();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sort(Comparator<? super E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        resolveList();
        return list.size();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        resolveList();
        return Collections.unmodifiableList(list.subList(fromIndex, toIndex));
    }

    @Override
    public Object[] toArray() {
        resolveList();
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        resolveList();
        return list.toArray(a);
    }
}
