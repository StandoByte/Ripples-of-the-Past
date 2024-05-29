package com.github.standobyte.jojo.client.render.item;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableInt;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class InventoryItemHighlight {
    private static final Map<ResourceLocation, MutableInt> HIGHLIGHT_TIMER = new HashMap<>();
    
    public static void highlightItem(Item item, int ticks) {
        if (item != null && ticks > 0) {
            HIGHLIGHT_TIMER.put(item.getRegistryName(), new MutableInt(ticks));
        }
    }
    
    public static void tick() {
        Iterator<Map.Entry<ResourceLocation, MutableInt>> iter = HIGHLIGHT_TIMER.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<ResourceLocation, MutableInt> entry = iter.next();
            if (entry.getValue().decrementAndGet() < 0) {
                iter.remove();
            }
        }
    }
    
    public static float getHighlightAmount(Item item, float partialTick) {
        MutableInt ticks = HIGHLIGHT_TIMER.get(item.getRegistryName());
        if (ticks != null && ticks.intValue() >= 0) {
            float x = ticks.intValue() + partialTick;
            x = x % 20 / 10;
            if (x > 1) {
                x = 2 - x;
            }
            x = MathHelper.sqrt(x);
            
            return x;
        }
        
        return -1;
    }
    
}
