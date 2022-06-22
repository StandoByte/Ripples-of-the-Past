package com.github.standobyte.jojo.capability.entity;

import com.github.standobyte.jojo.util.utils.TimeUtil;

import net.minecraft.entity.Entity;

public class EntityUtilCap {
    private final Entity entity;
    
    private boolean stoppedInTime = false;
    
    public EntityUtilCap(Entity entity) {
        this.entity = entity;
    }
    
    public void updateEntityTimeStop(boolean stopInTime) {
    	if (stopInTime) {
			stoppedInTime = true;
			entity.canUpdate(false);
    	}
    	else if (stoppedInTime) {
    		entity.canUpdate(true);
    	}
    }
    
    public boolean wasStoppedInTime() {
    	return stoppedInTime;
    }
    
    void nbtSetWasStoppedInTime(boolean wasStoppedInTime) {
    	if (wasStoppedInTime) {
        	stoppedInTime = true;
    		wasStoppedInTime = TimeUtil.isTimeStopped(entity.level, entity.blockPosition());
        	updateEntityTimeStop(wasStoppedInTime);
    	}
    }
}
