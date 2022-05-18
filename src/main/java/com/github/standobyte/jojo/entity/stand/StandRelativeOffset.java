package com.github.standobyte.jojo.entity.stand;

import com.github.standobyte.jojo.util.utils.MathUtil;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;

public class StandRelativeOffset {
    private double left;
    private double forward;
    private boolean doYOffset;
    private double y;
    private boolean useXRot = false;
//    private float yRotOffset;
    
    public static StandRelativeOffset noYOffset(double left, double forward) {
        return new StandRelativeOffset(left, 0, forward, false);
    }
    
    public static StandRelativeOffset withYOffset(double left, double y, double forward) {
        return new StandRelativeOffset(left, y, forward, true);
    }
    
    public StandRelativeOffset copy() {
        return new StandRelativeOffset(this.left, this.y, this.forward, this.doYOffset);
    }
    
    public StandRelativeOffset copy(Double left, Double y, Double forward) {
        return new StandRelativeOffset(
                left == null ? this.left : left, 
                y == null ? this.y : y, 
                forward == null ? this.forward : forward, 
                this.doYOffset || y != null);
    }
    
    private StandRelativeOffset(double left, double y, double forward, boolean doYOffset) {
        this.left = left;
        this.forward = forward;
        this.doYOffset = doYOffset;
        this.y = y;
    }
    
    public StandRelativeOffset withXRot() {
        this.useXRot = true;
        return this;
    }
    
    Vector3d getAbsoluteVec(StandRelativeOffset offsetDefault, float yRot, float xRot) {
        Vector3d vec;
        if (useXRot) {
            vec = new Vector3d(left, 0, forward).xRot(-xRot * MathUtil.DEG_TO_RAD).yRot(-yRot * MathUtil.DEG_TO_RAD);
        }
        else {
            vec = MathUtil.relativeCoordsToAbsolute(left, doYOffset ? y : offsetDefault.y, forward, yRot);
        }
        return vec;
    }
    
    Vector3d toRelativeVec() {
        return new Vector3d(left, y, forward);
    }
    
    void setFromRelativeVec(Vector3d vec) {
        this.left = vec.x;
        this.y = vec.y;
        this.forward = vec.z;
    }
    
    public double getLeft() {
        return left;
    }
    
    public double getForward() {
        return forward;
    }
    

    public void writeToBuf(PacketBuffer buf) {
        buf.writeDouble(left);
        buf.writeDouble(y);
        buf.writeDouble(forward);
        buf.writeBoolean(doYOffset);
    }
    
    public static StandRelativeOffset readFromBuf(PacketBuffer buf) {
        return new StandRelativeOffset(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readBoolean());
    }
}
