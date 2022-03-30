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
    
    public static StandRelativeOffset noYOffset(double left, double forward) {
        return new StandRelativeOffset(left, 0, forward, false);
    }
    
    public static StandRelativeOffset withYOffset(double left, double y, double forward) {
        return new StandRelativeOffset(left, y, forward, true);
    }
    
    public static StandRelativeOffset copy(StandRelativeOffset offset) {
        return new StandRelativeOffset(offset.left, offset.y, offset.forward, offset.doYOffset);
    }
    
    private StandRelativeOffset(double left, double y, double forward, boolean doYOffset) {
        this.left = left;
        this.forward = forward;
        this.doYOffset = doYOffset;
        this.y = y;
    }
    
    Vector3d getAbsoluteVec(StandRelativeOffset offsetDefault, float yRot) {
        return MathUtil.relativeCoordsToAbsolute(left, doYOffset ? y : offsetDefault.y, forward, yRot);
    }
    
    Vector3d toRelativeVec() {
        return new Vector3d(left, y, forward);
    }
    
    void setFromRelativeVec(Vector3d vec) {
        this.left = vec.x;
        this.y = vec.y;
        this.forward = vec.z;
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
