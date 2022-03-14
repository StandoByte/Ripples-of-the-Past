package com.github.standobyte.jojo.entity.stand;

import com.github.standobyte.jojo.util.MathUtil;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;

public class StandRelativeOffset {
    private double left;
    private double forward;
    private boolean doYOffset;
    private double y;
//    private float yRotOffset;
    
    public StandRelativeOffset(double left, double forward) {
        this(left, forward, false, 0);
    }
    
    public StandRelativeOffset(double left, double forward, double y) {
        this(left, forward, true, y);
    }
    
    private StandRelativeOffset(double left, double forward, boolean doYOffset, double y) {
        this.left = left;
        this.forward = forward;
        this.doYOffset = doYOffset;
        this.y = y;
    }
    
    public void addHorizontalOffset(double left, double forward) {
        setHorizontalOffset(this.left + left, this.forward + forward);
    }
    
    public void setHorizontalOffset(double left, double forward) {
        this.left = left;
        this.forward = forward;
    }
    
    Vector3d getAbsoluteVec(StandRelativeOffset offsetDefault, float yRot) {
        return MathUtil.relativeCoordsToAbsolute(left, doYOffset ? y : offsetDefault.y, forward, yRot);
    }
    

    public void writeToBuf(PacketBuffer buf) {
        buf.writeDouble(left);
        buf.writeDouble(forward);
        buf.writeBoolean(doYOffset);
        buf.writeDouble(y);
    }
    
    public static StandRelativeOffset readFromBuf(PacketBuffer buf) {
        return new StandRelativeOffset(buf.readDouble(), buf.readDouble(), buf.readBoolean(), buf.readDouble());
    }
}
