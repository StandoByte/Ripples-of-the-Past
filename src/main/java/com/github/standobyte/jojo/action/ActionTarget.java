package com.github.standobyte.jojo.action;

import java.util.Optional;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
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
            this.entityId = entity.getId();
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
    
    public ActionTarget(int entityId, World world) {
        this(world.getEntity(entityId));
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

    public Entity getEntity() {
        return entity;
    }

    public Vector3d getTargetPos(boolean targetEntityEyeHeight) {
        return type == TargetType.ENTITY && entity != null ? 
                targetEntityEyeHeight ? entity.getEyePosition(1.0F) : entity.position()
                        : targetPos;
    }
    
    public Optional<AxisAlignedBB> getBoundingBox(World world) {
        AxisAlignedBB aabb = null;
        switch (type) {
        case ENTITY:
            aabb = getEntity().getBoundingBox();
            break;
        case BLOCK:
            BlockState blockState = world.getBlockState(blockPos);
            VoxelShape blockShape = blockState.getShape(world, blockPos);
            if (!blockShape.isEmpty()) {
                aabb = blockShape.bounds().move(blockPos);
            }
            break;
        default:
            break;
        }
        return Optional.ofNullable(aabb);
    }
    

    public void writeToBuf(PacketBuffer buf) {
        TargetType type = getType();
        buf.writeEnum(type);
        switch (type) {
        case ENTITY:
            buf.writeInt(entityId);
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
    
    public static ActionTarget readFromBuf(PacketBuffer buf, World clientWorld) {
        ActionTarget target = readFromBuf(buf);
        return target.resolveEntityId(clientWorld);
    }
    
    public ActionTarget resolveEntityId(World world) {
        if (getType() == TargetType.ENTITY) {
            this.entity = world.getEntity(entityId);
            return this.entity != null ? this : ActionTarget.EMPTY;
        }
        return this;
    }
    
    public ActionTarget copy() {
        switch (type) {
        case EMPTY:
            return ActionTarget.EMPTY;
        case BLOCK:
            return new ActionTarget(blockPos, face);
        case ENTITY:
            return new ActionTarget(entityId);
        default:
            return null;
        }
    }
    
    private ActionTarget(int entityIdOnly) {
        type = TargetType.ENTITY;
        this.blockPos = null;
        this.face = null;
        this.entity = null;
        this.entityId = entityIdOnly;
        this.targetPos = null;
    }
    
    @Override
    public boolean equals(Object object) {
        return this == object || object instanceof ActionTarget && this.sameTarget((ActionTarget) object);
    }
    
    public boolean sameTarget(ActionTarget target) {
        if (target != null && this.type == target.type) {
            switch (type) {
            case BLOCK:
                return this.blockPos.equals(target.blockPos);
            case ENTITY:
                int idThis = this.entity != null ? this.entity.getId() : this.entityId;
                int idThat = target.entity != null ? target.entity.getId() : target.entityId;
                return idThis == idThat;
            default:
                return true;
            }
        }
        return false;
    }
    
    public static enum TargetType {
        EMPTY,
        BLOCK,
        ENTITY
    }
}
