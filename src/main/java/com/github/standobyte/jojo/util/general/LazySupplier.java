package com.github.standobyte.jojo.util.general;

import java.util.function.Supplier;

public class LazySupplier<T> implements Supplier<T> {
    private final Supplier<T> init;
    private T value;
    private boolean initialized = false;
    
    public LazySupplier(Supplier<T> init) {
        this.init = init;
    }
    
    @Override
    public T get() {
        if (!initialized) {
            value = init.get();
            initialized = true;
        }
        return value;
    }

}
