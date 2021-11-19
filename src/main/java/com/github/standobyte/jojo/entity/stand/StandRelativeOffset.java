package com.github.standobyte.jojo.entity.stand;

import com.github.standobyte.jojo.util.MathUtil;

import net.minecraft.util.math.vector.Vector3d;

public class StandRelativeOffset {
    private final double leftDefault;
    private final double forwardDefault;
    private final double yDefault;
    double left;
    double forward;
    double y;
    float yRotOffset;
    
    public StandRelativeOffset(double leftDefault, double forwardDefault, double yDefault) {
        this.leftDefault = leftDefault;
        left = leftDefault;
        this.forwardDefault = forwardDefault;
        forward = forwardDefault;
        this.yDefault = yDefault;
        y = yDefault;
    }
    
    Vector3d getAbsoluteVec(float yRot) {
        return MathUtil.relativeCoordsToAbsolute(left, y, forward, yRot);
    }
    
    void reset() {
        left = leftDefault;
        forward = forwardDefault;
        y = yDefault;
        yRotOffset = 0;
    }
    
    boolean isDefault() {
        return left == leftDefault && forward == forwardDefault && y == yDefault;
    }
}
