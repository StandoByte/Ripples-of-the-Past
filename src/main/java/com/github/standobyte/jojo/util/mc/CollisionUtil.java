package com.github.standobyte.jojo.util.mc;

import java.util.Objects;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.CubeCoordinateIterator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ICollisionReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

public class CollisionUtil {

    public static Vector3d collide(Entity entity, Vector3d offsetVec) {
        return collide(entity, entity.getBoundingBox(), offsetVec, null);
    }
    
    public static Vector3d collide(Entity entity, AxisAlignedBB collisionBox, Vector3d offsetVec) {
        return collide(entity, collisionBox, offsetVec, null);
    }
    
    public static Vector3d collide(Entity entity, AxisAlignedBB collisionBox, Vector3d offsetVec, @Nullable ISelectionContext selectionContext) {
        if (selectionContext == null) selectionContext = ISelectionContext.of(entity);
        VoxelShape worldBorder = entity.level.getWorldBorder().getCollisionShape();
        Stream<VoxelShape> worldBorderCollision = VoxelShapes.joinIsNotEmpty(worldBorder, VoxelShapes.create(collisionBox.deflate(1.0E-7D)), IBooleanFunction.AND) ? Stream.empty() : Stream.of(worldBorder);
        Stream<VoxelShape> entityCollisions = entity.level.getEntityCollisions(entity, collisionBox.expandTowards(offsetVec), e -> true);
        ReuseableStream<VoxelShape> collisions = new ReuseableStream<>(Stream.concat(entityCollisions, worldBorderCollision));
        Vector3d vector3d = offsetVec.lengthSqr() == 0 ? offsetVec : collideBoundingBoxHeuristically(entity, offsetVec, collisionBox, entity.level, selectionContext, collisions);
        boolean flag = offsetVec.x != vector3d.x;
        boolean flag2 = offsetVec.z != vector3d.z;
        boolean flag3 = entity.isOnGround() || offsetVec.y != vector3d.y && offsetVec.y < 0.0D;
        if (entity.maxUpStep > 0.0F && flag3 && (flag || flag2)) {
            Vector3d vector3d1 = collideBoundingBoxHeuristically(entity, new Vector3d(offsetVec.x, entity.maxUpStep, offsetVec.z), collisionBox, entity.level, selectionContext, collisions);
            Vector3d vector3d2 = collideBoundingBoxHeuristically(entity, new Vector3d(0, entity.maxUpStep, 0), collisionBox.expandTowards(offsetVec.x, 0.0D, offsetVec.z), entity.level, selectionContext, collisions);
            if (vector3d2.y < entity.maxUpStep) {
                Vector3d vector3d3 = collideBoundingBoxHeuristically(entity, new Vector3d(offsetVec.x, 0.0D, offsetVec.z), collisionBox.move(vector3d2), entity.level, selectionContext, collisions).add(vector3d2);
                if (Entity.getHorizontalDistanceSqr(vector3d3) > Entity.getHorizontalDistanceSqr(vector3d1)) {
                    vector3d1 = vector3d3;
                }
            }
            
            if (Entity.getHorizontalDistanceSqr(vector3d1) > Entity.getHorizontalDistanceSqr(vector3d)) {
                return vector3d1.add(collideBoundingBoxHeuristically(entity, new Vector3d(0.0D, -vector3d1.y + offsetVec.y, 0.0D), collisionBox.move(vector3d1), entity.level, selectionContext, collisions));
            }
        }
        
        return vector3d;
    }

    public static Vector3d collideBoundingBoxHeuristically(@Nullable Entity pEntity, Vector3d pVec, AxisAlignedBB pCollisionBox, World pLevel, ISelectionContext pContext, ReuseableStream<VoxelShape> pPotentialHits) {
       boolean flag = pVec.x == 0.0D;
       boolean flag1 = pVec.y == 0.0D;
       boolean flag2 = pVec.z == 0.0D;
       if ((!flag || !flag1) && (!flag || !flag2) && (!flag1 || !flag2)) {
          ReuseableStream<VoxelShape> reuseablestream = new ReuseableStream<>(Stream.concat(
                  pPotentialHits.getStream(), 
                  StreamSupport.stream(new CustomContextVoxelShapeSpliterator(pLevel, pEntity, pContext, pCollisionBox.expandTowards(pVec)), false)));
          return collideBoundingBoxLegacy(pVec, pCollisionBox, reuseablestream);
       } else {
          return collideBoundingBox(pVec, pCollisionBox, pLevel, pContext, pPotentialHits);
       }
    }



    public static Vector3d collideBoundingBoxLegacy(Vector3d pVec, AxisAlignedBB pCollisionBox, ReuseableStream<VoxelShape> pPotentialHits) {
       double d0 = pVec.x;
       double d1 = pVec.y;
       double d2 = pVec.z;
       if (d1 != 0.0D) {
          d1 = VoxelShapes.collide(Direction.Axis.Y, pCollisionBox, pPotentialHits.getStream(), d1);
          if (d1 != 0.0D) {
             pCollisionBox = pCollisionBox.move(0.0D, d1, 0.0D);
          }
       }

       boolean flag = Math.abs(d0) < Math.abs(d2);
       if (flag && d2 != 0.0D) {
          d2 = VoxelShapes.collide(Direction.Axis.Z, pCollisionBox, pPotentialHits.getStream(), d2);
          if (d2 != 0.0D) {
             pCollisionBox = pCollisionBox.move(0.0D, 0.0D, d2);
          }
       }

       if (d0 != 0.0D) {
          d0 = VoxelShapes.collide(Direction.Axis.X, pCollisionBox, pPotentialHits.getStream(), d0);
          if (!flag && d0 != 0.0D) {
             pCollisionBox = pCollisionBox.move(d0, 0.0D, 0.0D);
          }
       }

       if (!flag && d2 != 0.0D) {
          d2 = VoxelShapes.collide(Direction.Axis.Z, pCollisionBox, pPotentialHits.getStream(), d2);
       }

       return new Vector3d(d0, d1, d2);
    }
     
     public static class CustomContextVoxelShapeSpliterator extends AbstractSpliterator<VoxelShape> {
         @Nullable
         private final Entity source;
         private final AxisAlignedBB box;
         private final ISelectionContext context;
         private final CubeCoordinateIterator cursor;
         private final BlockPos.Mutable pos;
         private final VoxelShape entityShape;
         private final ICollisionReader collisionGetter;
         private boolean needsBorderCheck;
         private final BiPredicate<BlockState, BlockPos> predicate;

         public CustomContextVoxelShapeSpliterator(ICollisionReader pGetter, @Nullable Entity pEntity, ISelectionContext pContext, AxisAlignedBB pCollisionBox) {
            this(pGetter, pEntity, pContext, pCollisionBox, (p_241459_0_, p_241459_1_) -> {
               return true;
            });
         }

         public CustomContextVoxelShapeSpliterator(ICollisionReader pCollisionGetter, @Nullable Entity pSource, ISelectionContext pContext, AxisAlignedBB pBox, BiPredicate<BlockState, BlockPos> pPredicate) {
            super(Long.MAX_VALUE, 1280);
            this.context = pContext; // this line is the only difference from the vanilla VoxelShapeSpliterator, and we need the custom ISelectionContext for things like BarrierBlockWallClimbMixin#changeCollisionShape
//            this.context = pSource == null ? ISelectionContext.empty() : ISelectionContext.of(pSource);
            this.pos = new BlockPos.Mutable();
            this.entityShape = VoxelShapes.create(pBox);
            this.collisionGetter = pCollisionGetter;
            this.needsBorderCheck = pSource != null;
            this.source = pSource;
            this.box = pBox;
            this.predicate = pPredicate;
            int i = MathHelper.floor(pBox.minX - 1.0E-7D) - 1;
            int j = MathHelper.floor(pBox.maxX + 1.0E-7D) + 1;
            int k = MathHelper.floor(pBox.minY - 1.0E-7D) - 1;
            int l = MathHelper.floor(pBox.maxY + 1.0E-7D) + 1;
            int i1 = MathHelper.floor(pBox.minZ - 1.0E-7D) - 1;
            int j1 = MathHelper.floor(pBox.maxZ + 1.0E-7D) + 1;
            this.cursor = new CubeCoordinateIterator(i, k, i1, j, l, j1);
         }

         public boolean tryAdvance(Consumer<? super VoxelShape> p_tryAdvance_1_) {
            return this.needsBorderCheck && this.worldBorderCheck(p_tryAdvance_1_) || this.collisionCheck(p_tryAdvance_1_);
         }

         boolean collisionCheck(Consumer<? super VoxelShape> pConsumer) {
            while(true) {
               if (this.cursor.advance()) {
                  int i = this.cursor.nextX();
                  int j = this.cursor.nextY();
                  int k = this.cursor.nextZ();
                  int l = this.cursor.getNextType();
                  if (l == 3) {
                     continue;
                  }

                  IBlockReader iblockreader = this.getChunk(i, k);
                  if (iblockreader == null) {
                     continue;
                  }

                  this.pos.set(i, j, k);
                  BlockState blockstate = iblockreader.getBlockState(this.pos);
                  if (!this.predicate.test(blockstate, this.pos) || l == 1 && !blockstate.hasLargeCollisionShape() || l == 2 && !blockstate.is(Blocks.MOVING_PISTON)) {
                     continue;
                  }

                  VoxelShape voxelshape = blockstate.getCollisionShape(this.collisionGetter, this.pos, this.context);
                  if (voxelshape == VoxelShapes.block()) {
                     if (!this.box.intersects((double)i, (double)j, (double)k, (double)i + 1.0D, (double)j + 1.0D, (double)k + 1.0D)) {
                        continue;
                     }

                     pConsumer.accept(voxelshape.move((double)i, (double)j, (double)k));
                     return true;
                  }

                  VoxelShape voxelshape1 = voxelshape.move((double)i, (double)j, (double)k);
                  if (!VoxelShapes.joinIsNotEmpty(voxelshape1, this.entityShape, IBooleanFunction.AND)) {
                     continue;
                  }

                  pConsumer.accept(voxelshape1);
                  return true;
               }

               return false;
            }
         }

         @Nullable
         private IBlockReader getChunk(int pX, int pZ) {
            int i = pX >> 4;
            int j = pZ >> 4;
            return this.collisionGetter.getChunkForCollisions(i, j);
         }

         boolean worldBorderCheck(Consumer<? super VoxelShape> pConsumer) {
            Objects.requireNonNull(this.source);
            this.needsBorderCheck = false;
            WorldBorder worldborder = this.collisionGetter.getWorldBorder();
            AxisAlignedBB axisalignedbb = this.source.getBoundingBox();
            if (!isBoxFullyWithinWorldBorder(worldborder, axisalignedbb)) {
               VoxelShape voxelshape = worldborder.getCollisionShape();
               if (!isOutsideBorder(voxelshape, axisalignedbb) && isCloseToBorder(voxelshape, axisalignedbb)) {
                  pConsumer.accept(voxelshape);
                  return true;
               }
            }

            return false;
         }

         private static boolean isCloseToBorder(VoxelShape pShape, AxisAlignedBB pCollisionBox) {
            return VoxelShapes.joinIsNotEmpty(pShape, VoxelShapes.create(pCollisionBox.inflate(1.0E-7D)), IBooleanFunction.AND);
         }

         private static boolean isOutsideBorder(VoxelShape pShape, AxisAlignedBB pCollisionBox) {
            return VoxelShapes.joinIsNotEmpty(pShape, VoxelShapes.create(pCollisionBox.deflate(1.0E-7D)), IBooleanFunction.AND);
         }

         public static boolean isBoxFullyWithinWorldBorder(WorldBorder pBorder, AxisAlignedBB pCollisionBox) {
            double d0 = (double)MathHelper.floor(pBorder.getMinX());
            double d1 = (double)MathHelper.floor(pBorder.getMinZ());
            double d2 = (double)MathHelper.ceil(pBorder.getMaxX());
            double d3 = (double)MathHelper.ceil(pBorder.getMaxZ());
            return pCollisionBox.minX > d0 && pCollisionBox.minX < d2 && pCollisionBox.minZ > d1 && pCollisionBox.minZ < d3 && pCollisionBox.maxX > d0 && pCollisionBox.maxX < d2 && pCollisionBox.maxZ > d1 && pCollisionBox.maxZ < d3;
         }
      }

     
     public static Vector3d collideBoundingBox(Vector3d pVec, AxisAlignedBB pCollisionBox, IWorldReader pLevel, ISelectionContext pSelectionContext, ReuseableStream<VoxelShape> pPotentialHits) {
        double d0 = pVec.x;
        double d1 = pVec.y;
        double d2 = pVec.z;
        if (d1 != 0.0D) {
           d1 = VoxelShapes.collide(Direction.Axis.Y, pCollisionBox, pLevel, d1, pSelectionContext, pPotentialHits.getStream());
           if (d1 != 0.0D) {
              pCollisionBox = pCollisionBox.move(0.0D, d1, 0.0D);
           }
        }

        boolean flag = Math.abs(d0) < Math.abs(d2);
        if (flag && d2 != 0.0D) {
           d2 = VoxelShapes.collide(Direction.Axis.Z, pCollisionBox, pLevel, d2, pSelectionContext, pPotentialHits.getStream());
           if (d2 != 0.0D) {
              pCollisionBox = pCollisionBox.move(0.0D, 0.0D, d2);
           }
        }

        if (d0 != 0.0D) {
           d0 = VoxelShapes.collide(Direction.Axis.X, pCollisionBox, pLevel, d0, pSelectionContext, pPotentialHits.getStream());
           if (!flag && d0 != 0.0D) {
              pCollisionBox = pCollisionBox.move(d0, 0.0D, 0.0D);
           }
        }

        if (!flag && d2 != 0.0D) {
           d2 = VoxelShapes.collide(Direction.Axis.Z, pCollisionBox, pLevel, d2, pSelectionContext, pPotentialHits.getStream());
        }

        return new Vector3d(d0, d1, d2);
     }
    
}
