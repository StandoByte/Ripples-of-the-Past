package com.github.standobyte.jojo.client.renderer.entity.stand;

import java.util.Random;

import com.github.standobyte.jojo.entity.stand.StandEntity;

import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3d;

public class AdditionalArmSwing {
    private float anim;
    private final HandSide side;
    public final Vector3d offset;
    
    public AdditionalArmSwing(float anim, HandSide side, StandEntity stand) {
        this.anim = anim;
        this.side = side;
        Random random = stand.getRandom();
        double upOffset = (random.nextDouble() - 0.5) * 0.4D;
        double leftOffset = (random.nextDouble() - 0.0) * 0.2D;
        if (side == HandSide.RIGHT) {
            leftOffset *= -1;
        }
        offset = new Vector3d(leftOffset, upOffset, 0);
    }
    
    public float addDelta(float delta) {
        return anim += delta;
    }
    
    public HandSide getSide() {
        return side;
    }
    
    public float getAnim() {
        return anim;
    }
}
