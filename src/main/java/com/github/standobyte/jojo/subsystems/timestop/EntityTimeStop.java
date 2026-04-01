package com.github.standobyte.jojo.subsystems.timestop;

import net.minecraft.entity.Entity;

public interface EntityTimeStop {
    boolean jojo_ripples$isStoppedInTime();
    void jojo_ripples$setStoppedInTime(boolean flag);
    void jojo_ripples$queueOnTimeResume(Runnable action);
    
    static boolean canUpdate(Entity entity) {
        return !((EntityTimeStop) entity).jojo_ripples$isStoppedInTime();
    }
}
