package com.github.standobyte.jojo.util.mc.damage.explosion;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.damaging.projectile.MRCrossfireHurricaneEntity;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.CustomExplosionPacket;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.EntityExplosionContext;
import net.minecraft.world.Explosion;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;

public abstract class CustomExplosion extends Explosion {
    protected final World level;
    protected final float radius;
    private final Explosion.Mode blockInteraction;
    protected final boolean fire;
    protected final Random random = new Random();
    private final ExplosionContext damageCalculator;
    
    protected CustomExplosion(World pLevel, @Nullable Entity pSource, 
            @Nullable DamageSource pDamageSource, @Nullable ExplosionContext pDamageCalculator, 
            double pToBlowX, double pToBlowY, double pToBlowZ, 
            float pRadius, boolean pFire, Explosion.Mode pBlockInteraction) {
        super(pLevel, pSource, pDamageSource, pDamageCalculator, pToBlowX, pToBlowY, pToBlowZ, pRadius, pFire, pBlockInteraction);
        this.level = pLevel;
        this.radius = pRadius;
        this.blockInteraction = pBlockInteraction;
        this.fire = pFire;
        this.damageCalculator = pDamageCalculator == null ? makeDamageCalculator(pSource) : pDamageCalculator;
    }
    
    protected ExplosionContext makeDamageCalculator(@Nullable Entity pEntity) {
        return (ExplosionContext)(pEntity == null ? new ExplosionContext() : new EntityExplosionContext(pEntity));
    }
    
    /**
     * Does the first part of the explosion (destroy blocks)
     * Is only called on server
     */
    @Override
    public void explode() {
        getToBlow().addAll(calculateBlocksToBlow());
        
        double diameter = radius * 2.0F;
        Vector3d pos = getPosition();
        AxisAlignedBB aabb = new AxisAlignedBB(
                MathHelper.floor(pos.x - diameter - 1.0D), 
                MathHelper.floor(pos.y - diameter - 1.0D), 
                MathHelper.floor(pos.z - diameter - 1.0D), 
                MathHelper.floor(pos.x + diameter + 1.0D), 
                MathHelper.floor(pos.y + diameter + 1.0D), 
                MathHelper.floor(pos.z + diameter + 1.0D));
        List<Entity> entities = getAffectedEntities(aabb);
        filterEntities(entities);
        ForgeEventFactory.onExplosionDetonate(level, this, entities, diameter);
        hurtEntities(entities);
    }
    
    protected List<Entity> getAffectedEntities(AxisAlignedBB area) {
        return level.getEntities(getExploder(), area);
    }
    
    protected Set<BlockPos> calculateBlocksToBlow() {
        Set<BlockPos> blocksToBlow = Sets.newHashSet();
        
        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d0 = (j / 15.0F * 2.0F - 1.0F);
                        double d1 = (k / 15.0F * 2.0F - 1.0F);
                        double d2 = (l / 15.0F * 2.0F - 1.0F);
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 = d0 / d3;
                        d1 = d1 / d3;
                        d2 = d2 / d3;
                        float power = radius * (0.7F + level.random.nextFloat() * 0.6F);
                        Vector3d pos = getPosition();
                        double x = pos.x;
                        double y = pos.y;
                        double z = pos.z;
                        
                        for (; power > 0.0F; power -= 0.225F) {
                            BlockPos blockPos = new BlockPos(x, y, z);
                            BlockState blockState = level.getBlockState(blockPos);
                            FluidState fluidState = level.getFluidState(blockPos);
                            Optional<Float> resistance = damageCalculator.getBlockExplosionResistance(this, level, blockPos, blockState, fluidState);
                            if (resistance.isPresent()) {
                                power -= (resistance.get() + 0.3F) * 0.3F;
                            }
                            
                            if (power > 0.0F && damageCalculator.shouldBlockExplode(this, level, blockPos, blockState, power)) {
                                blocksToBlow.add(blockPos);
                            }
                            
                            x += d0 * 0.3;
                            y += d1 * 0.3;
                            z += d2 * 0.3;
                        }
                    }
                }
            }
        }
        
        return blocksToBlow;
    }
    
    /**
     * Does the second part of the explosion (sound, particles, drop spawn)
     * Is called on both sides
     */
    @Override
    public void finalizeExplosion(boolean pSpawnParticles) {
        if (level.isClientSide) {
            playSound();
        }
        
        if (pSpawnParticles) {
            spawnParticles();
        }
        
        if (blockInteraction != Explosion.Mode.NONE) {
            explodeBlocks();
        }

        if (fire) {
            spawnFire();
        }
    }
    
    
    protected void filterEntities(List<Entity> entities) {}
    
    protected void hurtEntities(Collection<Entity> entities) {
        double diameter = radius * 2.0F;
        Vector3d pos = getPosition();
        
        for (Entity entity : entities) {
            if (!entity.ignoreExplosion()) {
                double distRatio = entity.position().distanceTo(pos) / diameter;
                if (distRatio <= 1.0D) {
                    Vector3d entityPos = entity instanceof TNTEntity ? entity.position() : entity.getEyePosition(1.0F);
                    Vector3d diff = entityPos.subtract(pos);
                    
                    double length = diff.length();
                    if (length > 1.0E-4D) {
                        double seenPercent = getSeenPercent(pos, entity);
                        double impact = (1.0D - distRatio) * seenPercent;
                        float damage = calcDamage(impact, diameter);
                        double knockback = impact;
                        if (entity instanceof LivingEntity) {
                            knockback = ProtectionEnchantment.getExplosionKnockbackAfterDampener((LivingEntity) entity, knockback);
                        }
                        hurtEntity(entity, damage, impact, diff.normalize());
                    }
                }
            }
        }
    }
    
    protected float calcDamage(double impact, double diameter) {
        return (float) ((impact * impact + impact) / 2.0D * 7.0D * diameter + 1.0D);
    }
    
    protected void hurtEntity(Entity entity, float damage, double knockback, Vector3d vecToEntityNorm) {
        entity.hurt(getDamageSource(), damage);
        
        entity.setDeltaMovement(entity.getDeltaMovement().add(vecToEntityNorm.scale(knockback)));
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            if (!player.isSpectator() && (!player.isCreative() || !player.abilities.flying)) {
                getHitPlayers().put(player, vecToEntityNorm.scale(knockback));
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    protected void explodeBlocks() {
        ObjectArrayList<Pair<ItemStack, BlockPos>> dropPositions = new ObjectArrayList<>();
        Collections.shuffle(getToBlow(), level.random);

        for (BlockPos blockPos : getToBlow()) {
            BlockState blockState = level.getBlockState(blockPos);
            if (!blockState.isAir(level, blockPos)) {
                level.getProfiler().push("explosion_blocks");
                if (blockState.canDropFromExplosion(level, blockPos, this) && level instanceof ServerWorld) {
                    TileEntity tileentity = blockState.hasTileEntity() ? level.getBlockEntity(blockPos) : null;
                    LootContext.Builder lootcontext$builder = (
                            new LootContext.Builder((ServerWorld)level))
                            .withRandom(level.random)
                            .withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(blockPos))
                            .withParameter(LootParameters.TOOL, ItemStack.EMPTY)
                            .withOptionalParameter(LootParameters.BLOCK_ENTITY, tileentity)
                            .withOptionalParameter(LootParameters.THIS_ENTITY, getExploder());
                    if (blockInteraction == Explosion.Mode.DESTROY) {
                        lootcontext$builder.withParameter(LootParameters.EXPLOSION_RADIUS, radius);
                    }

                    blockState.getDrops(lootcontext$builder).forEach(itemStack -> {
                        addBlockDrops(dropPositions, itemStack, blockPos.immutable());
                    });
                }
                
                blockState.onBlockExploded(level, blockPos, this);
                level.getProfiler().pop();
            }
        }
        
        for (Pair<ItemStack, BlockPos> pair : dropPositions) {
            Block.popResource(level, pair.getSecond(), pair.getFirst());
        }
    }
    
    protected void playSound() {
        Vector3d pos = getPosition();
        level.playLocalSound(pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, SoundCategory.BLOCKS, 
                4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F, false);
    }
    
    protected void spawnParticles() {
        Vector3d pos = getPosition();
        if (radius >= 2.0F && blockInteraction != Explosion.Mode.NONE) {
            level.addParticle(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 1.0D, 0.0D, 0.0D);
        } else {
            level.addParticle(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 1.0D, 0.0D, 0.0D);
        }
    }
    
    @SuppressWarnings("deprecation")
    protected void spawnFire() {
        for (BlockPos blockPos : getToBlow()) {
            if (random.nextInt(3) == 0 && level.getBlockState(blockPos).isAir(level, blockPos)
                    && level.getBlockState(blockPos.below()).isSolidRender(level, blockPos.below())) {
                level.setBlockAndUpdate(blockPos, AbstractFireBlock.getState(level, blockPos));
            }
        }
    }
    
    
    private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> pDropPositionArray, ItemStack pStack, BlockPos pPos) {
        for (int i = 0; i < pDropPositionArray.size(); ++i) {
            Pair<ItemStack, BlockPos> pair = pDropPositionArray.get(i);
            ItemStack itemstack = pair.getFirst();
            if (ItemEntity.areMergable(itemstack, pStack)) {
                ItemStack itemstack1 = ItemEntity.merge(itemstack, pStack, 16);
                pDropPositionArray.set(i, Pair.of(itemstack1, pair.getSecond()));
                if (pStack.isEmpty()) {
                    return;
                }
            }
        }
        
        pDropPositionArray.add(Pair.of(pStack, pPos));
    }
    
    
    
    public static Explosion explode(World pLevel, @Nullable Entity pEntity, 
            double pX, double pY, double pZ, float pExplosionRadius, Explosion.Mode pMode, CustomExplosionType explosionType) {
        return explode(pLevel, pEntity, (DamageSource)null, (ExplosionContext)null, pX, pY, pZ, pExplosionRadius, false, pMode, explosionType);
    }

    public static Explosion explode(World pLevel, @Nullable Entity pEntity, 
            double pX, double pY, double pZ, float pExplosionRadius, boolean pCausesFire, Explosion.Mode pMode, CustomExplosionType explosionType) {
        return explode(pLevel, pEntity, (DamageSource)null, (ExplosionContext)null, pX, pY, pZ, pExplosionRadius, pCausesFire, pMode, explosionType);
    }

    public static CustomExplosion explode(World pLevel, @Nullable Entity pExploder, 
            @Nullable DamageSource pDamageSource, @Nullable ExplosionContext pContext, 
            double pX, double pY, double pZ, float pSize, boolean pCausesFire, Explosion.Mode pMode, CustomExplosionType explosionType) {
        CustomExplosion explosion = explosionType.createExplosion(pLevel, pExploder, 
                pDamageSource, pContext, 
                pX, pY, pZ, 
                pSize, pCausesFire, pMode);
        return explodePreCreated(explosion, pLevel, explosionType);
    }
    
    public static CustomExplosion explodePreCreated(CustomExplosion explosion, World pLevel, CustomExplosionType explosionType) {
        if (ForgeEventFactory.onExplosionStart(pLevel, explosion)) return explosion;
        explosion.explode();
        explosion.finalizeExplosion(true);
        
        if (!pLevel.isClientSide()) {
            if (explosion.blockInteraction == Explosion.Mode.NONE) {
                explosion.clearToBlow();
            }
            
            Vector3d pos = explosion.getPosition();
            for (ServerPlayerEntity player : ((ServerWorld) pLevel).players()) {
                if (player.distanceToSqr(pos.x, pos.y, pos.z) < 4096.0D) {
                    PacketManager.sendToClient(new CustomExplosionPacket(pos.x, pos.y, pos.z, 
                            explosion.radius, explosion.getToBlow(), explosion.getHitPlayers().get(player), explosionType), player);
                }
            }
        }
        
        return explosion;
    }
    
    
    public static enum CustomExplosionType {
        CROSSFIRE_HURRICANE {
            @Override public CustomExplosionSupplier explosionSupplier() {
                return MRCrossfireHurricaneEntity.CrossfireHurricaneExplosion::new;
            }
        },
        HAMON {
            @Deprecated @Override public CustomExplosionSupplier explosionSupplier() { return null; }
            
            @Override
            public CustomExplosion createExplosion(World pLevel, @Nullable Entity pSource, 
                    @Nullable DamageSource pDamageSource, @Nullable ExplosionContext pDamageCalculator, 
                    double pToBlowX, double pToBlowY, double pToBlowZ, 
                    float pRadius, boolean pFire, Explosion.Mode pBlockInteraction) {
                return new HamonBlastExplosion(pLevel, pSource, pDamageCalculator, pToBlowX, pToBlowY, pToBlowZ, pRadius);
            }
        };
        
        @Nonnull public CustomExplosion createExplosion(World pLevel, @Nullable Entity pSource, 
                @Nullable DamageSource pDamageSource, @Nullable ExplosionContext pDamageCalculator, 
                double pToBlowX, double pToBlowY, double pToBlowZ, 
                float pRadius, boolean pFire, Explosion.Mode pBlockInteraction) {
            return explosionSupplier().createExplosion(pLevel, pSource, 
                    pDamageSource, pDamageCalculator, 
                    pToBlowX, pToBlowY, pToBlowZ, 
                    pRadius, pFire, pBlockInteraction);
        }
        
        protected abstract CustomExplosionSupplier explosionSupplier();
        
        @Nonnull public Explosion createExplosionOnClient(World pLevel, @Nullable Entity pSource, 
                double pToBlowX, double pToBlowY, double pToBlowZ, 
                float pRadius, List<BlockPos> pPositions) {
            CustomExplosion explosion = createExplosion(pLevel, pSource, 
                    null, null, 
                    pToBlowX, pToBlowY, pToBlowZ, 
                    pRadius, false, Explosion.Mode.DESTROY);
            explosion.getToBlow().addAll(pPositions);
            return explosion;
        }
        
        
        @FunctionalInterface
        private static interface CustomExplosionSupplier {
            CustomExplosion createExplosion(World pLevel, Entity pSource, DamageSource pDamageSource,
                    ExplosionContext pDamageCalculator, double pToBlowX, double pToBlowY, double pToBlowZ,
                    float pRadius, boolean pFire, Mode pBlockInteraction);
        }
    }
}
