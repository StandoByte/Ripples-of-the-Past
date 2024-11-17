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

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandEntityHeavyAttack.HeavyPunchBlockInstance.HeavyPunchExplosion;
import com.github.standobyte.jojo.capability.entity.EntityUtilCap;
import com.github.standobyte.jojo.capability.entity.EntityUtilCapProvider;
import com.github.standobyte.jojo.entity.damaging.projectile.BlockShardEntity;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.CollideBlocks;
import com.github.standobyte.jojo.util.mc.CollideBlocks.BlockCollisionResult;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.damage.explosion.CustomExplosion;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.AxisRotation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;

public class KnockbackCollisionImpact implements INBTSerializable<CompoundNBT> {
    private final Entity entity;
    private final LivingEntity asLiving;

    private LivingEntity attacker;
    private LivingEntity attackerStandUser;
    private Vector3d knockbackVec = null;
    private double knockbackImpactStrength;
    private double minCos;
    private boolean hadImpactWithBlock = false;
    
    private float explosionRadius = 0;
    private DamageSource explosionDmgSource;
    private float explosionDamage;
    
    private float syoPunchBaseDamage = 0;
    private int scarletOverdriveFireTicks = 0;
    private IParticleData hamonParticles;
    
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
    
    public KnockbackCollisionImpact onPunchSetKnockbackImpact(Vector3d knockbackVec, LivingEntity attacker) {
        this.knockbackImpactStrength = knockbackVec.length();
        this.knockbackVec = knockbackVec.scale(1 / knockbackImpactStrength);
        this.minCos = 1;
        this.hadImpactWithBlock = false;
        this.attacker = attacker;
        this.attackerStandUser = attacker instanceof LivingEntity ? (StandUtil.getStandUser((LivingEntity) attacker)) : null;
        return this;
    }
    
    public KnockbackCollisionImpact withImpactExplosion(float radius, DamageSource aoeDamageSource, float aoeDamage) {
        this.explosionRadius = radius;
        if (aoeDamageSource != null) aoeDamageSource.setExplosion();
        this.explosionDmgSource = aoeDamageSource;
        this.explosionDamage = aoeDamage;
        return this;
    }
    
    public KnockbackCollisionImpact hamonDamage(float punchBaseDamage, int fireTicks, IParticleData sparkParticles) {
        this.syoPunchBaseDamage = punchBaseDamage;
        this.scarletOverdriveFireTicks = fireTicks;
        this.hamonParticles = sparkParticles;
        return this;
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
            nbt.putFloat("ExplosionRadius", explosionRadius);
            nbt.putFloat("ExplosionDamage", explosionDamage);
            nbt.putFloat("HamonPunchDmg", syoPunchBaseDamage);
            nbt.putInt("HamonFireTicks", scarletOverdriveFireTicks);
            if (hamonParticles instanceof ParticleType) {
                nbt.putString("HamonSparks", ((ParticleType<?>) hamonParticles).getRegistryName().toString());
            }
        }
        return nbt;
    }
    
    public void deserializeNBT(CompoundNBT nbt) {
        knockbackVec = MCUtil.nbtGetVec3d(nbt, "Vec");
        if (knockbackVec != null) {
            knockbackImpactStrength = nbt.getDouble("Power");
            minCos = nbt.getDouble("MinCos");
            hadImpactWithBlock = nbt.getBoolean("HadBlockImpact");
            explosionRadius = nbt.getFloat("ExplosionRadius");
            explosionDamage = nbt.getFloat("ExplosionDamage");
            syoPunchBaseDamage = nbt.getFloat("HamonPunchDmg");
            scarletOverdriveFireTicks = nbt.getInt("HamonFireTicks");
            hamonParticles = MCUtil.getNbtElement(nbt, "HamonSparks", StringNBT.class)
                    .map(StringNBT::getAsString).map(ResourceLocation::new)
                    .map(particleId -> {
                        if (ForgeRegistries.PARTICLE_TYPES.containsKey(particleId)) {
                            ParticleType<?> type = ForgeRegistries.PARTICLE_TYPES.getValue(particleId);
                            if (type instanceof BasicParticleType) {
                                return (BasicParticleType) type;
                            }
                        }
                        return null;
                    }).orElse(null);
        }
    }
    
    
    
    private void collideBoundingBox(Entity entity, Vector3d movementVec, boolean collideBlocks, boolean breakBlocks) {
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
                LivingEntity asLiving = targetEntity instanceof LivingEntity ? (LivingEntity) targetEntity : null;
                if (asLiving != null && syoPunchBaseDamage > 0) {
                    DamageUtil.dealHamonDamage(asLiving, syoPunchBaseDamage * 0.5f, entity, attacker, attack -> {
                        if (hamonParticles != null) {
                            attack.hamonParticle(hamonParticles);
                        }
                    });
                }
                if (scarletOverdriveFireTicks > 0) {
                    DamageUtil.dealDamageAndSetOnFire(targetEntity, 
                            e -> DamageUtil.hurtThroughInvulTicks(e, new EntityDamageSource("entityFlewInto", entity), 
                                    (float) getKnockbackImpactStrength() * 5), 
                            scarletOverdriveFireTicks / 20, false);
                }
                else {
                    DamageUtil.hurtThroughInvulTicks(targetEntity, new EntityDamageSource("entityFlewInto", entity), 
                            (float) getKnockbackImpactStrength() * 5);
                }
                if (asLiving != null) {
                    asLiving.knockback((float) getKnockbackImpactStrength(), -vec.x, -vec.z);
                }
            });
        }
        
        
        MutableBoolean doGlassBleeding = new MutableBoolean();
        float bleedingChance = asLiving != null ? BlockShardEntity.glassShardBleedingChance(asLiving) : 0;
        
        MutableFloat wallDamage = new MutableFloat(0);
        
        if (collideBlocks) {
            BlockCollisionResult collision = CollideBlocks.collideBoundingBox(movementVec, aabb, serverWorld, selectionContext);

            if (collision.blocks.size() > 0) {
                collision.blocks.stream()
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
                        
                        float damage = useImpactStrength * 4;
                        wallDamage.add(damage);
                        
                        blockState.entityInside(serverWorld, blockPos, entity);
    
                        // episode #158 of me being on the spectrum
                        if (!doGlassBleeding.booleanValue() && asLiving != null 
                                && BlockShardEntity.isGlassBlock(blockState)
                                && asLiving.getRandom().nextFloat() < bleedingChance) {
                            doGlassBleeding.setTrue();
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
                setKnockbackImpactStrength(0);
                
                Vector3d collisionDir = new Vector3d(collision.movementX - collision.x, collision.movementY - collision.y, collision.movementZ - collision.z);
                Direction faceHit = Direction.getNearest(collisionDir.x, collisionDir.y, collisionDir.z);
                if (faceHit != Direction.DOWN) {
                    if (breakBlocks) {
                        if (explosionRadius > 0) {
                            AxisAlignedBB entityBB = entity.getBoundingBox();
                            Vector3d hitPos = new Vector3d(
                                    MathHelper.lerp(faceHit.getStepX() * 0.5 + 0.5, entityBB.minX, entityBB.maxX), 
                                    MathHelper.lerp(faceHit.getStepY() * 0.5 + 0.5, entityBB.minY, entityBB.maxY), 
                                    MathHelper.lerp(faceHit.getStepZ() * 0.5 + 0.5, entityBB.minZ, entityBB.maxZ));
                            BlockPos hitBlockPos = new BlockPos(hitPos.add(Vector3d.atBottomCenterOf(faceHit.getNormal()).scale(0.5)));
                            
                            HeavyPunchExplosion explosion = new HeavyPunchExplosion(world, attacker, new ActionTarget(hitBlockPos, faceHit.getOpposite()), 
                                    movementVec, explosionDmgSource, null, 
                                    hitPos.x, hitPos.y, hitPos.z, 
                                    explosionRadius, false, Explosion.Mode.BREAK)
                                    .aoeDamage(explosionDamage)
                                    .entityNoDamage(entity);
                            if (CustomExplosion.explode(explosion)) {
                                if (doGlassBleeding.booleanValue()) {
                                    BlockShardEntity.glassShardBleeding(asLiving);
                                }
                            }
                        }
                    }
                    
                    if (wallDamage.floatValue() > 0) {
                        DamageUtil.hurtThroughInvulTicks(entity, DamageSource.FLY_INTO_WALL, wallDamage.floatValue());
                    }
                }
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
//            if (z != 0) {
//                aabb = aabb.move(0, 0, z);
//            }
        }

        if (x != 0) {
            x = collideEntitiesAxis(Direction.Axis.X, aabb, world, x, 
                    worldBorderCollision, potentialEntityCollisions, 
                    selectionContext, entityCollision);
//            if (!zFirst && x != 0) {
//                aabb = aabb.move(x, 0, 0);
//            }
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
