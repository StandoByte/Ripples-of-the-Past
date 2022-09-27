package com.github.standobyte.jojo.capability.entity;

import java.util.LinkedList;
import java.util.Queue;

import com.github.standobyte.jojo.util.utils.JojoModUtil;
import com.github.standobyte.jojo.util.utils.TimeUtil;

import net.minecraft.entity.Entity;

public class EntityUtilCap {
    private final Entity entity;
    
    private boolean stoppedInTime = false;
    private Queue<Runnable> runOnTimeResume = new LinkedList<>();
    
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
    		runOnTimeResume.forEach(Runnable::run);
    		runOnTimeResume.clear();
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
    
    
    
    public static void queueOnTimeResume(Entity entity, Runnable action) {
        JojoModUtil.ifPresentOrElse(entity.getCapability(EntityUtilCapProvider.CAPABILITY).resolve(), 
                cap -> {
                    if (cap.stoppedInTime) {
                        cap.runOnTimeResume.add(action);
                    }
                    else if (entity.canUpdate()) {
                        action.run();
                    }
                }, 
                action);
    }
}
