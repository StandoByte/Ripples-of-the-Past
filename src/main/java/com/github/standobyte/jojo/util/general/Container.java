package com.github.standobyte.jojo.util.general;

import java.util.function.Supplier;

public class Container<T> implements Supplier<T> {
    private T object;
    
    public Container(T object) {
        this.object = object;
    }
    
    public void set(T object) {
        this.object = object;
    }
    
    @Override
    public T get() {
        return object;
    }

}
