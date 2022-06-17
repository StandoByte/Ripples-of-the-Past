package com.github.standobyte.jojo.entity.stand.stands;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityType;

import net.minecraft.world.World;

public class TheWorldEntity extends StandEntity {
    
    public TheWorldEntity(StandEntityType<TheWorldEntity> type, World world) {
        super(type, world);
    }
    
    private boolean prevLerp = false;
    @Override
    public void lerpTo(double lerpX, double lerpY, double lerpZ, float lerpYRot, float lerpXRot, int lerpSteps, boolean teleport) {
    	if (prevLerp) {
    		if (lerpX == getX()) lerpX = this.lerpX;
    		if (lerpY == getY()) lerpY = this.lerpY;
    		if (lerpZ == getZ()) lerpZ = this.lerpZ;
    	}
		super.lerpTo(lerpX, lerpY, lerpZ, lerpYRot, lerpXRot, lerpSteps, teleport);
    	prevLerp = true;
    }
}
