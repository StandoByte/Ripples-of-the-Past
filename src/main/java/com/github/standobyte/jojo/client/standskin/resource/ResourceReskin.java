package com.github.standobyte.jojo.client.standskin.resource;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

public abstract class ResourceReskin<T> {
    private final Map<ResourceLocation, T> cache = new HashMap<>();
    
    public T getOrDefault(ResourceLocation path, T defaultValue) {
        T value;
        if (cache.containsKey(path)) {
            value = cache.get(path);
        }
        else {
            value = createValueLazy(path);
            cache.put(path, value);
        }
        return value != null ? value : defaultValue;
    }
    
    public void cacheValue(ResourceLocation path, T value) {
        cache.put(path, value);
    }
    
    protected abstract T createValueLazy(ResourceLocation path);
    
}
