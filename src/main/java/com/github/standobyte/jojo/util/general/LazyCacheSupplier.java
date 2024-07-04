package com.github.standobyte.jojo.util.general;

import java.util.function.Supplier;

public class LazyCacheSupplier<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private T cache;
    
    public LazyCacheSupplier(Supplier<T> supplier) {
        this.supplier = supplier;
    }
    
    public T get() {
        if (cache == null) {
            cache = supplier.get();
        }
        return cache;
    }
    

}
