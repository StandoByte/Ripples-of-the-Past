package com.github.standobyte.jojo.entity.stand;

import com.github.standobyte.jojo.util.general.MathUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;

public class StandRelativeOffset {
    private final double left;
    private final double forward;
    private final boolean doYOffset;
    private final double y;
    private final boolean useXRot;
//    private final float yRotOffset;
    
    public static StandRelativeOffset noYOffset(double left, double forward) {
        return new StandRelativeOffset(left, 0, forward, false, false);
    }
    
    public static StandRelativeOffset withYOffset(double left, double y, double forward) {
        return new StandRelativeOffset(left, y, forward, true, false);
    }
    
    public static StandRelativeOffset withXRot(double left, double forward) {
        return new StandRelativeOffset(left, 0, forward, false, true);
    }
    
    public StandRelativeOffset copy() {
        return new StandRelativeOffset(this.left, this.y, this.forward, this.doYOffset, this.useXRot);
    }
    
    public StandRelativeOffset copyScale(double leftScale, double yScale, double forwardScale) {
        return new StandRelativeOffset(this.left * leftScale, this.y * yScale, this.forward * forwardScale, this.doYOffset, this.useXRot);
    }
    
    private StandRelativeOffset(double left, double y, double forward, boolean doYOffset, boolean useXRot) {
        this.left = left;
        this.forward = forward;
        this.doYOffset = doYOffset;
        this.y = y;
        this.useXRot = useXRot;
    }
    
    public Vector3d getAbsoluteVec(StandRelativeOffset offsetDefault, float yRot, float xRot, StandEntity standEntity, LivingEntity user) {
        double yOffset = 0;
        if (standEntity.isArmsOnlyMode() && user.getPose() != Pose.STANDING) {
            yOffset = (user.getDimensions(user.getPose()).height - user.getDimensions(Pose.STANDING).height) * 0.85F;
        }
        Vector3d vec;
        if (useXRot) {
            vec = new Vector3d(left, 0, forward).xRot(-xRot * MathUtil.DEG_TO_RAD).yRot(-yRot * MathUtil.DEG_TO_RAD);
        }
        else {
            vec = MathUtil.relativeCoordsToAbsolute(left, (doYOffset ? y : offsetDefault.y) + yOffset, forward, yRot);
        }
        return vec;
    }
    
    public Vector3d toRelativeVec() {
        return new Vector3d(left, y, forward);
    }
    
    public StandRelativeOffset withRelativeVec(Vector3d vec) {
        return new StandRelativeOffset(vec.x, vec.y, vec.z, this.doYOffset, this.useXRot);
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
        buf.writeBoolean(useXRot);
    }
    
    public static StandRelativeOffset readFromBuf(PacketBuffer buf) {
        StandRelativeOffset offset = new StandRelativeOffset(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readBoolean(), buf.readBoolean());
        return offset;
    }
}
