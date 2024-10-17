package com.github.standobyte.jojo.util.mc.damage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.capability.entity.EntityUtilCap;
import com.github.standobyte.jojo.capability.entity.EntityUtilCapProvider;
import com.github.standobyte.jojo.entity.damaging.projectile.BlockShardEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.AxisRotation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.INBTSerializable;

public class KnockbackCollisionImpact implements INBTSerializable<CompoundNBT> {
    private final Entity entity;
    private final LivingEntity asLiving;
    
    private LivingEntity attackerStandUser;
    private Vector3d knockbackVec = null;
    private double knockbackImpactStrength;
    private double minCos;
    private boolean hadImpactWithBlock = false;
    private boolean dropBlockItems = true;
    
    public KnockbackCollisionImpact(Entity entity) {
        this.entity = entity;
        this.asLiving = entity instanceof LivingEntity ? (LivingEntity) entity : null;
    }
    
    
    /**
     * @return true if the block collision needs to be recalculated
     */
    public boolean collideBreakBlocks(Vector3d movementVec, Vector3d collidedVec, World world) {
        if (knockbackVec == null || movementVec.lengthSqr() < 1E-07) {
            return false;
        }
        
        boolean canBreakBlocks = JojoModUtil.breakingBlocksEnabled(world);
        boolean collidedWithBlocks = !movementVec.equals(collidedVec);
        collideBoundingBox(entity, movementVec, collidedWithBlocks, canBreakBlocks);
        return canBreakBlocks && collidedWithBlocks;
    }
    
    public void onPunchSetKnockbackImpact(Vector3d knockbackVec, Entity attacker) {
        this.knockbackImpactStrength = knockbackVec.length();
        this.knockbackVec = knockbackVec.scale(1 / knockbackImpactStrength);
        this.minCos = 1;
        this.hadImpactWithBlock = false;
        this.attackerStandUser = attacker instanceof LivingEntity ? (StandUtil.getStandUser((LivingEntity) attacker)) : null;
        this.dropBlockItems = !(attackerStandUser instanceof PlayerEntity && ((PlayerEntity) attackerStandUser).abilities.instabuild);
    }
    
    public void tick() {
        if (knockbackVec != null) {
            if (knockbackImpactStrength <= 0) {
                setKnockbackImpactStrength(0);
                return;
            }
            
            Vector3d deltaMovement = entity.getDeltaMovement();
            if (Math.abs(deltaMovement.x) < 1E-7 && Math.abs(deltaMovement.z) < 1E-7) {
                setKnockbackImpactStrength(0);
                return;
            }
            
            double deltaMovementLen = deltaMovement.length();
            Vector3d deltaMovementNormalized = deltaMovement.scale(1 / deltaMovementLen);
            double cos = deltaMovementNormalized.dot(knockbackVec);
            if (cos <= 0) {
                setKnockbackImpactStrength(0);
                return;
            }
            
            minCos = Math.min(minCos, cos);
            knockbackImpactStrength = Math.min(knockbackImpactStrength, deltaMovementLen);
        }
    }
    
    public void setKnockbackImpactStrength(double strength) {
        if (strength <= 0) {
            this.knockbackVec = null;
            this.knockbackImpactStrength = 0;
        }
        else {
            this.knockbackImpactStrength = strength;
        }
    }
    
    public double getKnockbackImpactStrength() {
        return knockbackImpactStrength * minCos;
    }
    
    public void setHadImpactWithBlock() {
        hadImpactWithBlock = true;
    }
    
    public boolean getHadImpactWithBlock() {
        return hadImpactWithBlock;
    }
    
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        if (knockbackVec != null) {
            MCUtil.nbtPutVec3d(nbt, "Vec", knockbackVec);
            nbt.putDouble("Power", knockbackImpactStrength);
            nbt.putDouble("MinCos", minCos);
            nbt.putBoolean("HadBlockImpact", hadImpactWithBlock);
            nbt.putBoolean("DropItems", dropBlockItems);
        }
        return nbt;
    }
    
    public void deserializeNBT(CompoundNBT nbt) {
        knockbackVec = MCUtil.nbtGetVec3d(nbt, "Vec");
        if (knockbackVec != null) {
            knockbackImpactStrength = nbt.getDouble("Power");
            minCos = nbt.getDouble("MinCos");
            hadImpactWithBlock = nbt.getBoolean("HadBlockImpact");
            dropBlockItems = nbt.getBoolean("DropItems");
        }
    }
    
    
    
    public void collideBoundingBox(Entity entity, Vector3d movementVec, boolean collideBlocks, boolean canBreakBlocks) {
        World world = entity.level;
        if (world.isClientSide()) return;
        
        AxisAlignedBB aabb = entity.getBoundingBox().inflate(0.25);
        ISelectionContext selectionContext = ISelectionContext.of(entity);
        ServerWorld serverWorld = (ServerWorld) world;
        
        VoxelShape worldBorder = world.getWorldBorder().getCollisionShape();
        ReuseableStream<VoxelShape> worldBorderCollision = new ReuseableStream<>(
                VoxelShapes.joinIsNotEmpty(worldBorder, VoxelShapes.create(aabb.deflate(1.0E-7D)), IBooleanFunction.AND) ? Stream.empty() : Stream.of(worldBorder));
        
        ReuseableStream<Pair<Entity, VoxelShape>> potentialEntityCollisions = new ReuseableStream<>(getEntityCollisions(world, entity, aabb.expandTowards(movementVec), 
                EntityPredicates.NO_CREATIVE_OR_SPECTATOR.and(
                        e -> e.isPickable()
                        && (attackerStandUser == null || MCUtil.canHarm(attackerStandUser, e))
                        && !(entity instanceof LivingEntity && !MCUtil.canHarm((LivingEntity) entity, e))
                        )));
        Collection<Entity> entitiesCollided = new ArrayList<>();
        collideEntities(aabb, movementVec, world, 
                worldBorderCollision, potentialEntityCollisions, 
                selectionContext, entitiesCollided);

        if (!entitiesCollided.isEmpty()) {
            Vector3d vec = entity.getDeltaMovement();

            entitiesCollided.forEach(targetEntity -> {
                DamageUtil.hurtThroughInvulTicks(targetEntity, new EntityDamageSource("entityFlewInto", entity), 
                        (float) getKnockbackImpactStrength() * 5);
                if (targetEntity instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) targetEntity;
                    living.knockback((float) getKnockbackImpactStrength(), -vec.x, -vec.z);
                }
            });
        }
        
        
        // that will do for now maybe (А ВОТ ХУЙ ТАМ)
        if (movementVec.y < 0) {
            movementVec = new Vector3d(movementVec.x, 0, movementVec.z);
        }
        
        MutableBoolean didGlassBleeding = new MutableBoolean();
        float bleedingChance = asLiving != null ? BlockShardEntity.glassShardBleedingChance(asLiving) : 0;
        
        MutableFloat wallDamage = new MutableFloat(0);
        
        if (collideBlocks) {
            Collection<Pair<BlockPos, VoxelShape>> blocks;
            Collection<Pair<BlockPos, VoxelShape>> blocksCollision;
            Collection<Pair<BlockPos, VoxelShape>> blocksCanBreak;
            blocksCollision = collideNoBreakingBlocks(movementVec, aabb, serverWorld, worldBorder, selectionContext);
            if (canBreakBlocks) {
                blocksCanBreak = collideBreakBlocks(movementVec, aabb, serverWorld, worldBorder, selectionContext);
                blocks = blocksCanBreak;
            }
            else {
                blocks = blocksCollision;
            }
            
            Collection<BlockPos> blocksToDestroy = new ArrayList<>();
            float initialImpact = (float) getKnockbackImpactStrength();
            
            blocks.stream()
            .distinct()
            .sorted(Comparator.comparingDouble(block -> {
                AxisAlignedBB blockBB = block.getRight().bounds();
                return MCUtil.getManhattanDist(blockBB, entity.getBoundingBox());
            }))
            .map(Pair::getLeft)
            .allMatch(blockPos -> {
                BlockState blockState = world.getBlockState(blockPos);
                float hardness = StandStatFormulas.getStandBreakBlockHardness(blockState, world, blockPos);
                float useImpactStrength = 0;
                if (hardness >= 0) {
                    useImpactStrength = hardness * 0.05f;
                    
                    if (canBreakBlocks && getKnockbackImpactStrength() >= useImpactStrength) {
                        blocksToDestroy.add(blockPos);
                    }
                }
                else if (hardness < 0) {
                    useImpactStrength = 1;
                }
                if (useImpactStrength > 0) {
                    setHadImpactWithBlock();
                    float impactLeft = (float) getKnockbackImpactStrength();
                    if (impactLeft < useImpactStrength) {
                        useImpactStrength = (impactLeft + useImpactStrength) / 2;
                    }
                    useImpactStrength = Math.min(impactLeft, useImpactStrength);
                    
                    if (blockPos.getY() + 1 > entity.position().y) {
                        float damage = useImpactStrength * 4;
                        if (!canBreakBlocks) {
                            damage *= initialImpact;
                        }
                        wallDamage.add(damage);
                    }


                    blockState.entityInside(serverWorld, blockPos, entity);

                    // episode #158 of me being on the spectrum
                    if (!didGlassBleeding.booleanValue() && asLiving != null 
                            && BlockShardEntity.isGlassBlock(blockState)
                            && asLiving.getRandom().nextFloat() < bleedingChance) {
                        didGlassBleeding.setTrue();
                        BlockShardEntity.glassShardBleeding(asLiving);
                    }
                    if (blockState.getMaterial() == Material.CACTUS) {
                        DamageUtil.hurtThroughInvulTicks(entity, DamageSource.CACTUS, 1);
                    }
                    if (entity.isOnFire()) {
                        MCUtil.blockCatchFire(world, blockPos, blockState, null, asLiving);
                    }


                    setKnockbackImpactStrength(getKnockbackImpactStrength() - Math.max(useImpactStrength, 0.05f));
                }
                
                return getKnockbackImpactStrength() > 0;
            });
            
            if (canBreakBlocks) {
                MCUtil.destroyBlocksInBulk(blocksToDestroy, serverWorld, entity instanceof LivingEntity ? (LivingEntity) entity : null, dropBlockItems);
            }
            
            if (wallDamage.floatValue() > 0) {
                DamageUtil.hurtThroughInvulTicks(entity, DamageSource.FLY_INTO_WALL, wallDamage.floatValue());
            }
        }
        
    }
    
    
    private static Stream<Pair<Entity, VoxelShape>> getEntityCollisions(World world, @Nullable Entity pEntity, AxisAlignedBB pArea, Predicate<Entity> pFilter) {
        if (pArea.getSize() < 1.0E-7D) {
            return Stream.empty();
        } else {
            AxisAlignedBB axisalignedbb = pArea.inflate(1.0E-7D);
            return world.getEntities(pEntity, axisalignedbb, pFilter.and(target -> {
                return target.isPickable();
            })).stream().map(entity -> Pair.of(entity, VoxelShapes.create(entity.getBoundingBox())));
        }
    }
    
    private static void collideEntities(AxisAlignedBB aabb, Vector3d movementVec, World world, 
            ReuseableStream<VoxelShape> worldBorderCollision, ReuseableStream<Pair<Entity, VoxelShape>> potentialEntityCollisions, 
            ISelectionContext selectionContext, Collection<Entity> entityCollision) {
        double x = movementVec.x;
        double y = movementVec.y;
        double z = movementVec.z;
        
        if (y != 0) {
            y = collideEntitiesAxis(Direction.Axis.Y, aabb, world, y, 
                    worldBorderCollision, potentialEntityCollisions, 
                    selectionContext, entityCollision);
            if (y != 0) {
                aabb = aabb.move(0, y, 0);
            }
        }

        boolean zFirst = Math.abs(x) < Math.abs(z);
        if (zFirst && z != 0) {
            z = collideEntitiesAxis(Direction.Axis.Z, aabb, world, z, 
                    worldBorderCollision, potentialEntityCollisions, 
                    selectionContext, entityCollision);
            if (z != 0) {
                aabb = aabb.move(0, 0, z);
            }
        }

        if (x != 0) {
            x = collideEntitiesAxis(Direction.Axis.X, aabb, world, x, 
                    worldBorderCollision, potentialEntityCollisions, 
                    selectionContext, entityCollision);
            if (!zFirst && x != 0) {
                aabb = aabb.move(x, 0, 0);
            }
        }

        if (!zFirst && z != 0) {
            z = collideEntitiesAxis(Direction.Axis.Z, aabb, world, z, 
                    worldBorderCollision, potentialEntityCollisions, 
                    selectionContext, entityCollision);
        }
    }
    
    private static double collideEntitiesAxis(Direction.Axis movementAxis, AxisAlignedBB collisionBox, World world, double desiredOffset, 
            ReuseableStream<VoxelShape> worldBorderCollision, ReuseableStream<Pair<Entity, VoxelShape>> potentialEntityCollisions, 
            ISelectionContext pSelectionContext, Collection<Entity> entityCollision) {
        if (!(collisionBox.getXsize() < 1.0E-6D) && !(collisionBox.getYsize() < 1.0E-6D) && !(collisionBox.getZsize() < 1.0E-6D)) {
            if (Math.abs(desiredOffset) < 1.0E-7D) {
                return 0;
            } else {
                AxisRotation pRotationAxis = AxisRotation.between(movementAxis, Direction.Axis.Z);
                AxisRotation axisrotation = pRotationAxis.inverse();
                Direction.Axis direction$axis2 = axisrotation.cycle(Direction.Axis.Z);

                MutableDouble worldBorderCollideOffset = new MutableDouble(desiredOffset);
                worldBorderCollision.getStream().forEach(voxelShape -> {
                    worldBorderCollideOffset.setValue(voxelShape.collide(direction$axis2, collisionBox, worldBorderCollideOffset.doubleValue()));
                });
                desiredOffset = worldBorderCollideOffset.doubleValue();
                
                double maxOffset = desiredOffset;
                MutableDouble collidedOffset = new MutableDouble(maxOffset);
                potentialEntityCollisions.getStream().forEach(entityVoxelShape -> {
                    double entityCollideResult = entityVoxelShape.getRight().collide(direction$axis2, collisionBox, collidedOffset.doubleValue());
                    if (entityCollideResult != maxOffset) {
                        entityCollision.add(entityVoxelShape.getLeft());
                        collidedOffset.setValue(entityCollideResult);
                    }
                });
                
                return desiredOffset;
            }
        } else {
            return desiredOffset;
        }
    }
    
    
    // FIXME when the knockback is high, the entity flies to a different direction for some reason
    private Collection<Pair<BlockPos, VoxelShape>> collideBreakBlocks(Vector3d movementVec, AxisAlignedBB aabb, ServerWorld world, 
            VoxelShape worldBorder, ISelectionContext selectionContext) {
        Collection<Pair<BlockPos, VoxelShape>> blocksCollided = new ArrayList<>();

        Vector3d step;
        AxisAlignedBB loopCollisionBB = aabb;
        double xAbs = Math.abs(movementVec.x);
        double yAbs = Math.abs(movementVec.y);
        double zAbs = Math.abs(movementVec.z);
        double iterations;
        if (xAbs >= yAbs && xAbs >= zAbs) {
            iterations = xAbs;
        }
        else if (yAbs >= zAbs && yAbs >= xAbs) {
            iterations = yAbs;
        }
        else /*if (zAbs >= xAbs && zAbs >= yAbs)*/ {
            iterations = zAbs;
        }
        
        step = movementVec.scale(1.0 / iterations);

        for (int i = 0; i < iterations; i++) {
            addBlocksInsideBB(loopCollisionBB, world, 
                    worldBorder, selectionContext, 
                    blocksCollided);

            if (i + 1 > iterations) {
                step = step.scale(iterations - i);
            }
            loopCollisionBB = loopCollisionBB.move(step);
        }
        
        return blocksCollided;
    }
    
    private Collection<Pair<BlockPos, VoxelShape>> collideNoBreakingBlocks(Vector3d movementVec, AxisAlignedBB aabb, ServerWorld world, 
            VoxelShape worldBorder, ISelectionContext selectionContext) {
        ReuseableStream<VoxelShape> worldBorderCollision = new ReuseableStream<>(
                VoxelShapes.joinIsNotEmpty(worldBorder, VoxelShapes.create(aabb.deflate(1.0E-7D)), IBooleanFunction.AND) ? Stream.empty() : Stream.of(worldBorder));
        
        double x = movementVec.x;
        double y = movementVec.y;
        double z = movementVec.z;
        
        Collection<Pair<BlockPos, VoxelShape>> blocksCollided = new ArrayList<>();
        
        if (y != 0) {
            y = collide(Direction.Axis.Y, aabb, world, y, 
                    worldBorderCollision, selectionContext, blocksCollided);
            if (y != 0) {
                aabb = aabb.move(0, y, 0);
            }
        }

        boolean zFirst = Math.abs(x) < Math.abs(z);
        if (zFirst && z != 0) {
            z = collide(Direction.Axis.Z, aabb, world, z, 
                    worldBorderCollision, selectionContext, blocksCollided);
            if (z != 0) {
                aabb = aabb.move(0, 0, z);
            }
        }

        if (x != 0) {
            x = collide(Direction.Axis.X, aabb, world, x, 
                    worldBorderCollision, selectionContext, blocksCollided);
            if (!zFirst && x != 0) {
                aabb = aabb.move(x, 0, 0);
            }
        }

        if (!zFirst && z != 0) {
            z = collide(Direction.Axis.Z, aabb, world, z, 
                    worldBorderCollision, selectionContext, blocksCollided);
        }
        
        return blocksCollided;
    }
    
    
    private static void addBlocksInsideBB(AxisAlignedBB collisionBox, World world, 
            VoxelShape worldBorder, ISelectionContext selectionContext, 
            Collection<Pair<BlockPos, VoxelShape>> blockCollision) {
        BlockPos.Mutable blockPos = new BlockPos.Mutable();
        int x0 = MathHelper.floor(collisionBox.minX);
        int y0 = MathHelper.floor(collisionBox.minY);
        int z0 = MathHelper.floor(collisionBox.minZ);
        int x1 = MathHelper.ceil(collisionBox.maxX);
        int y1 = MathHelper.ceil(collisionBox.maxY);
        int z1 = MathHelper.ceil(collisionBox.maxZ);
        
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    blockPos.set(x, y, z);
                    BlockState blockstate = world.getBlockState(blockPos);
                    VoxelShape collisionShape = blockstate.getCollisionShape(world, blockPos, selectionContext);
                    if (!collisionShape.isEmpty()) {
                        blockCollision.add(Pair.of(new BlockPos(blockPos), collisionShape));
                    }
                }
            }
        }
    }
    
    private static double collide(Direction.Axis movementAxis, AxisAlignedBB collisionBox, World world, double desiredOffset, 
            ReuseableStream<VoxelShape> worldBorderCollision, ISelectionContext pSelectionContext, Collection<Pair<BlockPos, VoxelShape>> blockCollision) {
        if (!(collisionBox.getXsize() < 1.0E-6D) && !(collisionBox.getYsize() < 1.0E-6D) && !(collisionBox.getZsize() < 1.0E-6D)) {
            if (Math.abs(desiredOffset) < 1.0E-7D) {
                return 0;
            } else {
                AxisRotation pRotationAxis = AxisRotation.between(movementAxis, Direction.Axis.Z);
                AxisRotation axisrotation = pRotationAxis.inverse();
                Direction.Axis direction$axis = axisrotation.cycle(Direction.Axis.X);
                Direction.Axis direction$axis1 = axisrotation.cycle(Direction.Axis.Y);
                Direction.Axis direction$axis2 = axisrotation.cycle(Direction.Axis.Z);
                BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
                int i = MathHelper.floor(collisionBox.min(direction$axis) - 1.0E-7D) - 1;
                int j = MathHelper.floor(collisionBox.max(direction$axis) + 1.0E-7D) + 1;
                int k = MathHelper.floor(collisionBox.min(direction$axis1) - 1.0E-7D) - 1;
                int l = MathHelper.floor(collisionBox.max(direction$axis1) + 1.0E-7D) + 1;
                double d0 = collisionBox.min(direction$axis2) - 1.0E-7D;
                double d1 = collisionBox.max(direction$axis2) + 1.0E-7D;
                boolean flag = desiredOffset > 0;
                int i1 = flag ? MathHelper.floor(collisionBox.max(direction$axis2) - 1.0E-7D) - 1 : MathHelper.floor(collisionBox.min(direction$axis2) + 1.0E-7D) + 1;
                int j1 = lastC(desiredOffset, d0, d1);
                int k1 = flag ? 1 : -1;
                int l1 = i1;

//                MutableDouble worldBorderCollideOffset = new MutableDouble(desiredOffset);
//                worldBorderCollision.getStream().forEach(voxelShape -> {
//                    worldBorderCollideOffset.setValue(voxelShape.collide(direction$axis2, collisionBox, worldBorderCollideOffset.doubleValue()));
//                });
//                desiredOffset = worldBorderCollideOffset.doubleValue();
//                double minOffset = desiredOffset;
                
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
                                blockpos$mutable.set(axisrotation, i2, j2, l1);
                                BlockState blockstate = world.getBlockState(blockpos$mutable);
                                if ((k2 != 1 || blockstate.hasLargeCollisionShape()) && (k2 != 2 || blockstate.is(Blocks.MOVING_PISTON))) {
                                    VoxelShape collisionShape = blockstate.getCollisionShape(world, blockpos$mutable, pSelectionContext);
                                    double collidedOffset = collisionShape.collide(direction$axis2, collisionBox.move((double)(-blockpos$mutable.getX()), (double)(-blockpos$mutable.getY()), (double)(-blockpos$mutable.getZ())), desiredOffset);
                                    if (Math.abs(collidedOffset) < 1.0E-7D) {
                                        blockCollision.add(Pair.of(new BlockPos(blockpos$mutable), collisionShape));
//                                        minOffset = 0;
                                    }
                                    if (collidedOffset != desiredOffset) {
                                        blockCollision.add(Pair.of(new BlockPos(blockpos$mutable), collisionShape));
                                    }

                                    j1 = lastC(collidedOffset, d0, d1);
//                                    minOffset = Math.min(minOffset, collidedOffset);
                                }
                            }
                        }
                    }

                    l1 += k1;
                }
                
//                double maxOffset = desiredOffset;
//                MutableDouble collidedOffset = new MutableDouble(maxOffset);
//                potentialEntityCollisions.getStream().forEach(entityVoxelShape -> {
//                    double entityCollideResult = entityVoxelShape.getRight().collide(direction$axis2, collisionBox, collidedOffset.doubleValue());
//                    if (entityCollideResult != maxOffset) {
//                        entityCollision.add(entityVoxelShape.getLeft());
//                        collidedOffset.setValue(entityCollideResult);
//                    }
//                });
                
                return desiredOffset;
            }
        } else {
            return desiredOffset;
        }
    }
    
    private static int lastC(double pDesiredOffset, double pMin, double pMax) {
       return pDesiredOffset > 0.0D ? MathHelper.floor(pMax + pDesiredOffset) + 1 : MathHelper.floor(pMin + pDesiredOffset) - 1;
    }
    
    
    public static boolean isSoftMaterial(BlockState blockState) {
        Material material = blockState.getMaterial();
        return 
                material == Material.CLOTH_DECORATION || 
                material == Material.TOP_SNOW || 
                material == Material.WEB || 
                material == Material.CLAY || 
                material == Material.DIRT || 
                material == Material.GRASS || 
                material == Material.SAND || 
                material == Material.SPONGE || 
                material == Material.WOOL || 
                material == Material.LEAVES || 
                material == Material.CACTUS || 
                material == Material.SNOW || 
                material == Material.VEGETABLE;
    }
    
    
    
    public static Optional<KnockbackCollisionImpact> getHandler(Entity entity) {
        return entity.getCapability(EntityUtilCapProvider.CAPABILITY)
                .resolve().map(EntityUtilCap::getKbImpact);
    }
}
