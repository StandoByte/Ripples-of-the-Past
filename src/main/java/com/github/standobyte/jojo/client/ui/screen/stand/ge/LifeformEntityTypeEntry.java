package com.github.standobyte.jojo.client.ui.screen.stand.ge;

import java.util.List;

import com.github.standobyte.jojo.util.mc.entitysubtype.EntitySubtype;

import net.minecraft.entity.EntityType;

public class LifeformEntityTypeEntry {
    public final EntityType<?> entityType;
    private final List<EntitySubtype<?>> subtypes;
    private EntitySubtype<?> currentSubtype;
    private int currentSubtypeIndex;
    
    public LifeformEntityTypeEntry(EntityType<?> entityType, List<EntitySubtype<?>> subtypes) {
        this.entityType = entityType;
        this.subtypes = subtypes;
        this.currentSubtypeIndex = 0;
        this.currentSubtype = subtypes.get(0);
    }
    
    public EntitySubtype<?> getCurrentSubtype() {
        return currentSubtype;
    }
    
    public boolean hasMultipleSubtypes() {
        return subtypes.size() > 1;
    }
    
    public void scrollSubtypes(boolean right) {
        this.currentSubtypeIndex = (currentSubtypeIndex + (right ? 1 : -1) + subtypes.size()) % subtypes.size();
        this.currentSubtype = subtypes.get(currentSubtypeIndex);
    }
    
}
