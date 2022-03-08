package com.github.standobyte.jojo.action;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class ActionTarget {
    private final TargetType type;
    private final BlockPos blockPos;
    private final Direction face;
    private Entity entity;
    private final int entityId;
    private final Vector3d targetPos;
    
    public static final ActionTarget EMPTY = new ActionTarget();
    
    private ActionTarget() {
        type = TargetType.EMPTY;
        this.blockPos = null;
        this.face = null;
        this.entity = null;
        this.entityId = -1;
        this.targetPos = null;
    }
    
    public ActionTarget(@Nonnull BlockPos blockPos, @Nonnull Direction face) {
        type = TargetType.BLOCK;
        this.blockPos = blockPos;
        this.face = face;
        this.entity = null;
        this.entityId = -1;
        this.targetPos = new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.5D, 0.5D, 0.5D);
    }
    
    public ActionTarget(@Nonnull Entity entity) {
        if (entity != null) {
            type = TargetType.ENTITY;
            this.blockPos = null;
            this.face = null;
            this.entity = entity;
            this.entityId = -1;
            this.targetPos = null;
        }
        else {
            type = TargetType.EMPTY;
            this.blockPos = null;
            this.face = null;
            this.entity = null;
            this.entityId = -1;
            this.targetPos = null;
        }
    }
    
    public ActionTarget(int entityId) {
        type = TargetType.ENTITY;
        this.blockPos = null;
        this.face = null;
        this.entity = null;
        this.entityId = entityId;
        this.targetPos = null;
    }
    
    public static ActionTarget fromRayTraceResult(RayTraceResult result) {
        switch (result.getType()) {
        case BLOCK:
            BlockRayTraceResult blockResult = (BlockRayTraceResult) result;
            return new ActionTarget(blockResult.getBlockPos(), blockResult.getDirection());
        case ENTITY:
            return new ActionTarget(((EntityRayTraceResult) result).getEntity());
        default:
            return ActionTarget.EMPTY;
        }
    }
    
    public TargetType getType() {
        return type;
    }
    
    public BlockPos getBlockPos() {
        return blockPos;
    }
    
    public Direction getFace() {
        return face;
    }
    
    public Entity getEntity(World world) {
        if (entity == null) {
            entity = world.getEntity(entityId);
        }
        return entity;
    }
    
    public Vector3d getTargetPos() {
        return type == TargetType.ENTITY ? entity.getEyePosition(1.0F) : targetPos;
    }
    

    public void writeToBuf(PacketBuffer buf) {
        TargetType type = getType();
        buf.writeEnum(type);
        switch (type) {
        case ENTITY:
            buf.writeInt(getEntity(null).getId());
            break;
        case BLOCK:
            buf.writeBlockPos(getBlockPos());
            buf.writeEnum(getFace());
            break;
        default:
        }
    }
    
    public static ActionTarget readFromBuf(PacketBuffer buf) {
        TargetType type = buf.readEnum(TargetType.class);
        switch (type) {
        case ENTITY:
            return new ActionTarget(buf.readInt());
        case BLOCK:
            return new ActionTarget(buf.readBlockPos(), buf.readEnum(Direction.class));
        default:
            return ActionTarget.EMPTY;
        }
    }

    public static enum TargetType {
        EMPTY,
        BLOCK,
        ENTITY
    }
}
