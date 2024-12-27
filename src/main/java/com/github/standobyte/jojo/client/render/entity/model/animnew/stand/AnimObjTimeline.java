package com.github.standobyte.jojo.client.render.entity.model.animnew.stand;

import java.util.Comparator;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.floats.Float2ObjectArrayMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;

public class AnimObjTimeline<V> {
    private Float2ObjectMap<V> timeline = new Float2ObjectArrayMap<>();
    
    public void add(float keyframeTimeInSeconds, V value) {
        timeline.put(keyframeTimeInSeconds, value);
    }
    
    public void sort() {
        if (!timeline.isEmpty()) {
            timeline = timeline.float2ObjectEntrySet().stream()
                    .sorted(Comparator.comparingDouble(Float2ObjectMap.Entry::getFloatKey))
                    .collect(Float2ObjectArrayMap::new, 
                            (map, entry) -> map.put(entry.getFloatKey(), entry.getValue()), 
                            (map1, map2) -> map1.putAll(map2));
        }
    }
    
    @Nullable
    public V getCurValue(float timeInSeconds) {
        for (Float2ObjectMap.Entry<V> entry : timeline.float2ObjectEntrySet()) {
            if (entry.getFloatKey() <= timeInSeconds) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    public Iterable<Float2ObjectMap.Entry<V>> getEntries() {
        return timeline.float2ObjectEntrySet();
    }
}
