package com.github.standobyte.jojo.util;

public class Container<T> {
    private T object;
    
    public Container(T object) {
        this.object = object;
    }
    
    public void set(T object) {
        this.object = object;
    }
    
    public T get() {
        return object;
    }

}
