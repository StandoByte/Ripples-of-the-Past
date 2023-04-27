package com.github.standobyte.jojo.util.general;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

public class GraphAdjacencyList<T extends Object> {
    private Map<T, Set<T>> adjacencies = new HashMap<>();
    
    
    public void create(BiPredicate<T, T> adjacency, List<T> objects) {
        clear();
        objects.forEach(object -> adjacencies.put(object, new HashSet<T>()));
        
        ListIterator<T> i = objects.listIterator();
        ListIterator<T> j;
        while (i.hasNext()) {
            T objectA = i.next();
            j = objects.listIterator(i.nextIndex());
            while (j.hasNext()) {
                T objectB = j.next();
                if (adjacency.test(objectA, objectB)) {
                    adjacencies.get(objectA).add(objectB);
                    adjacencies.get(objectB).add(objectA);
                }
            }
        }
    }
    
    public void clear() {
        adjacencies.clear();
    }
    
    public boolean areAdjacent(T a, T b) {
        if (!adjacencies.containsKey(a)) {
            throw new IllegalArgumentException("The object is not a part of the graph!");
        }
        return getAllAdjacent(a).contains(b);
    }
    
    public Set<T> getAllAdjacent(T object) {
        if (adjacencies.isEmpty()) {
            throw new IllegalStateException("The graph adjacency list has not been initialized!");
        }
        if (!adjacencies.containsKey(object)) {
            throw new IllegalArgumentException("The object is not a part of the graph!");
        }
        return adjacencies.get(object);
    }

}
