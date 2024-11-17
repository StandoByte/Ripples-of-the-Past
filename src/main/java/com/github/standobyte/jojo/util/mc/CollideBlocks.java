package com.github.standobyte.jojo.util.mc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.AxisRotation;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;

public class CollideBlocks {

    public static BlockCollisionResult collideBoundingBox(Vector3d pVec, AxisAlignedBB pCollisionBox, IWorldReader pLevel, ISelectionContext pSelectionContext) {
        BlockCollisionResult collision = new BlockCollisionResult();
        double x = pVec.x;
        double y = pVec.y;
        double z = pVec.z;
        collision.movementX = x;
        collision.movementY = y;
        collision.movementZ = z;
        if (y != 0) {
            y = collide(Direction.Axis.Y, pCollisionBox, pLevel, y, pSelectionContext, collision.blocks);
            if (y != 0) {
                pCollisionBox = pCollisionBox.move(0, y, 0);
            }
        }

        boolean zAxisFirst = Math.abs(x) < Math.abs(z);
        if (zAxisFirst && z != 0) {
            z = collide(Direction.Axis.Z, pCollisionBox, pLevel, z, pSelectionContext, collision.blocks);
//            if (z != 0) {
//                pCollisionBox = pCollisionBox.move(0, 0, z);
//            }
        }

        if (x != 0) {
            x = collide(Direction.Axis.X, pCollisionBox, pLevel, x, pSelectionContext, collision.blocks);
//            if (!flag && x != 0) {
//                pCollisionBox = pCollisionBox.move(x, 0, 0);
//            }
        }

        if (!zAxisFirst && z != 0) {
            z = collide(Direction.Axis.Z, pCollisionBox, pLevel, z, pSelectionContext, collision.blocks);
        }
        
        collision.x = x;
        collision.y = y;
        collision.z = z;
        return collision;
    }

    private static double collide(Direction.Axis pMovementAxis, AxisAlignedBB pCollisionBox, 
            IWorldReader pLevelReader, double pDesiredOffset, ISelectionContext pSelectionContext, Collection<Pair<BlockPos, VoxelShape>> blockPosCollide) {
        return collide(pCollisionBox, pLevelReader, pDesiredOffset, pSelectionContext, AxisRotation.between(pMovementAxis, Direction.Axis.Z), blockPosCollide);
    }

    public static double collide(AxisAlignedBB pCollisionBox, IWorldReader pLevelReader, 
            double pDesiredOffset, ISelectionContext pSelectionContext, AxisRotation pRotationAxis, Collection<Pair<BlockPos, VoxelShape>> blockPosCollide) {
        if (!(pCollisionBox.getXsize() < 1.0E-6D) && !(pCollisionBox.getYsize() < 1.0E-6D) && !(pCollisionBox.getZsize() < 1.0E-6D)) {
            if (Math.abs(pDesiredOffset) < 1.0E-7D) {
                return 0.0D;
            } else {
                AxisRotation axisrotation = pRotationAxis.inverse();
                Direction.Axis direction$axis = axisrotation.cycle(Direction.Axis.X);
                Direction.Axis direction$axis1 = axisrotation.cycle(Direction.Axis.Y);
                Direction.Axis direction$axis2 = axisrotation.cycle(Direction.Axis.Z);
                BlockPos.Mutable blockpos = new BlockPos.Mutable();
                int i = MathHelper.floor(pCollisionBox.min(direction$axis) - 1.0E-7D) - 1;
                int j = MathHelper.floor(pCollisionBox.max(direction$axis) + 1.0E-7D) + 1;
                int k = MathHelper.floor(pCollisionBox.min(direction$axis1) - 1.0E-7D) - 1;
                int l = MathHelper.floor(pCollisionBox.max(direction$axis1) + 1.0E-7D) + 1;
                double d0 = pCollisionBox.min(direction$axis2) - 1.0E-7D;
                double d1 = pCollisionBox.max(direction$axis2) + 1.0E-7D;
                boolean flag = pDesiredOffset > 0.0D;
                int i1 = flag ? MathHelper.floor(pCollisionBox.max(direction$axis2) - 1.0E-7D) - 1 : MathHelper.floor(pCollisionBox.min(direction$axis2) + 1.0E-7D) + 1;
                int j1 = lastC(pDesiredOffset, d0, d1);
                int k1 = flag ? 1 : -1;
                int l1 = i1;

                boolean collision = false;
                double minOffsetCollided = pDesiredOffset;
                double desiredOffset_abs = Math.abs(pDesiredOffset);
                double minOffset_abs = desiredOffset_abs;

                Collection<Pair<BlockPos, VoxelShape>> collidedWith = new ArrayList<>();
                while(true) {
                    if (flag) {
                        if (l1 > j1) {
                            break;
                        }
                    } else if (l1 < j1) {
                        break;
                    }

                    for(int i2 = i; i2 <= j; ++i2) {
                        for(int j2 = k; j2 <= l; ++j2) {
                            int k2 = 0;
                            if (i2 == i || i2 == j) {
                                ++k2;
                            }

                            if (j2 == k || j2 == l) {
                                ++k2;
                            }

                            if (l1 == i1 || l1 == j1) {
                                ++k2;
                            }

                            if (k2 < 3) {
                                blockpos.set(axisrotation, i2, j2, l1);
                                BlockState blockstate = pLevelReader.getBlockState(blockpos);
                                if ((k2 != 1 || blockstate.hasLargeCollisionShape()) && (k2 != 2 || blockstate.is(Blocks.MOVING_PISTON))) {
                                    VoxelShape collisionShape = blockstate.getCollisionShape(pLevelReader, blockpos, pSelectionContext);
                                    double offsetCollided = collisionShape.collide(direction$axis2, pCollisionBox.move(-blockpos.getX(), -blockpos.getY(), -blockpos.getZ()), pDesiredOffset);
                                    double offset_abs = Math.abs(offsetCollided);
                                    if (offset_abs < 1.0E-7D) {
                                        offsetCollided = 0;
                                        offset_abs = 0;
                                    }
                                    if (!collision && offset_abs < desiredOffset_abs) {
                                        collision = true;
                                    }
                                    
                                    if (offset_abs <= minOffset_abs) {
                                        if (offset_abs < minOffset_abs) {
                                            collidedWith.clear();
                                            minOffsetCollided = offsetCollided;
                                            minOffset_abs = offset_abs;
                                        }
                                        else if (collision) {
                                            collidedWith.add(Pair.of(blockpos.immutable(), collisionShape));
                                        }
                                    }
                                    j1 = lastC(minOffsetCollided, d0, d1);
                                }
                            }
                        }
                    }

                    l1 += k1;
                }
                
                if (!collidedWith.isEmpty()) {
                    blockPosCollide.addAll(collidedWith);
                }
                return minOffsetCollided;
            }
        } else {
            return pDesiredOffset;
        }
    }

    private static int lastC(double pDesiredOffset, double pMin, double pMax) {
        return pDesiredOffset > 0.0D ? MathHelper.floor(pMax + pDesiredOffset) + 1 : MathHelper.floor(pMin + pDesiredOffset) - 1;
    }
    
    public static class BlockCollisionResult {
        public Collection<Pair<BlockPos, VoxelShape>> blocks = new ArrayList<>();
        public double movementX;
        public double movementY;
        public double movementZ;
        public double x;
        public double y;
        public double z;
    }
    
    
    
    
    
    public static Collection<BlockPos> getBlocksOutlineTowards(AxisAlignedBB collisionBox, Vector3d vec, IWorldReader world, boolean sort) {
        List<BlockPos> blocks = new ArrayList<>();
        double vecLengthSqr = vec.lengthSqr();
        Vector3d center1 = collisionBox.getCenter();
        
        if (vecLengthSqr == 0) {
            int x1 = MathHelper.floor(collisionBox.minX);
            int y1 = MathHelper.floor(collisionBox.minY);
            int z1 = MathHelper.floor(collisionBox.minZ);
            int x2 = MathHelper.ceil(collisionBox.maxX);
            int y2 = MathHelper.ceil(collisionBox.maxY);
            int z2 = MathHelper.ceil(collisionBox.maxZ);
            for (int x = x1; x <= x2; x++) {
                for (int y = y1; y <= y2; y++) {
                    for (int z = z1; z <= z2; z++) {
                        blocks.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        else {
            BlockPos.Mutable blockPos = new BlockPos.Mutable();
            AxisAlignedBB box2 = collisionBox.move(vec);
            int x1 = MathHelper.floor(Math.min(collisionBox.minX, box2.minX));
            int y1 = MathHelper.floor(Math.min(collisionBox.minY, box2.minY));
            int z1 = MathHelper.floor(Math.min(collisionBox.minZ, box2.minZ));
            int x2 = MathHelper.ceil(Math.max(collisionBox.maxX, box2.maxX));
            int y2 = MathHelper.ceil(Math.max(collisionBox.maxY, box2.maxY));
            int z2 = MathHelper.ceil(Math.max(collisionBox.maxZ, box2.maxZ));
            for (int x = x1; x <= x2; x++) {
                for (int y = y1; y <= y2; y++) {
                    for (int z = z1; z <= z2; z++) {
                        blockPos.set(x, y, z);
                        if (!world.isEmptyBlock(blockPos)) {
                            Vector3d blockRelPos = Vector3d.atCenterOf(blockPos).subtract(center1);
                            double projScale = vec.dot(blockRelPos) / vecLengthSqr;
                            Vector3d projOnAxisVec = vec.scale(projScale);
                            if (collisionBox.move(projOnAxisVec).intersects(
                                    blockPos.getX(),     blockPos.getY(),     blockPos.getZ(), 
                                    blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1)) {
                                blocks.add(blockPos.immutable());
                            }
                        }
                    }
                }
            }
        }
        
        if (sort) {
            return blocks.stream()
            .sorted(Comparator.comparingDouble(blockPos -> blockPos.distSqr(center1.x, center1.y, center1.z, true)))
            .collect(Collectors.toList());
        }
        return blocks;
    }
}
