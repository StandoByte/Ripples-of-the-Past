package com.github.standobyte.jojo.util.mc.damage.explosion;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.non_stand.PillarmanSelfDetonation;
import com.github.standobyte.jojo.action.stand.StandEntityHeavyAttack;
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
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
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
    protected float radius;
    protected Explosion.Mode blockInteraction;
    protected boolean fire;
    protected Random random = new Random();
    protected ExplosionContext damageCalculator;
    
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
    
    protected CustomExplosion(World pLevel, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius) {
        this(pLevel, null, null, null, pToBlowX, pToBlowY, pToBlowZ, pRadius, false, Explosion.Mode.NONE);
    }
    
    public void toBuf(PacketBuffer buf) {}
    
    public void fromBuf(PacketBuffer buf) {}
    
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
        
        AxisAlignedBB area = entityDamageArea();
        List<Entity> entities = getAffectedEntities(area);
        filterEntities(entities);
        ForgeEventFactory.onExplosionDetonate(level, this, entities, radius * 2);
        hurtEntities(entities);
    }
    
    protected AxisAlignedBB entityDamageArea() {
        double diameter = radius * 2;
        Vector3d pos = getPosition();
        return new AxisAlignedBB(
                MathHelper.floor(pos.x - diameter - 1), 
                MathHelper.floor(pos.y - diameter - 1), 
                MathHelper.floor(pos.z - diameter - 1), 
                MathHelper.floor(pos.x + diameter + 1), 
                MathHelper.floor(pos.y + diameter + 1), 
                MathHelper.floor(pos.z + diameter + 1));
    }
    
    protected List<Entity> getAffectedEntities(AxisAlignedBB area) {
        return level.getEntities(getExploder(), area);
    }
    
    protected Set<BlockPos> calculateBlocksToBlow() {
        Set<BlockPos> blocksToBlow = Sets.newHashSet();
        
        for (int xStep = 0; xStep < 16; ++xStep) {
            for (int yStep = 0; yStep < 16; ++yStep) {
                for (int zStep = 0; zStep < 16; ++zStep) {
                    if (xStep == 0 || xStep == 15 || yStep == 0 || yStep == 15 || zStep == 0 || zStep == 15) {
                        double xd = (xStep / 15.0F * 2.0F - 1.0F);
                        double yd = (yStep / 15.0F * 2.0F - 1.0F);
                        double zd = (zStep / 15.0F * 2.0F - 1.0F);
                        double len = Math.sqrt(xd * xd + yd * yd + zd * zd);
                        xd = xd / len;
                        yd = yd / len;
                        zd = zd / len;
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
                            
                            x += xd * 0.3;
                            y += yd * 0.3;
                            z += zd * 0.3;
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
                        if (damage > 0) {
                            hurtEntity(entity, damage, impact, diff.normalize());
                        }
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
                    TileEntity tileEntity = blockState.hasTileEntity() ? level.getBlockEntity(blockPos) : null;
                    LootContext.Builder lootCtxBuilder = (
                            new LootContext.Builder((ServerWorld)level))
                            .withRandom(level.random)
                            .withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(blockPos))
                            .withParameter(LootParameters.TOOL, ItemStack.EMPTY)
                            .withOptionalParameter(LootParameters.BLOCK_ENTITY, tileEntity)
                            .withOptionalParameter(LootParameters.THIS_ENTITY, getExploder());
                    if (blockInteraction == Explosion.Mode.DESTROY) {
                        lootCtxBuilder.withParameter(LootParameters.EXPLOSION_RADIUS, radius);
                    }

                    blockState.getDrops(lootCtxBuilder).forEach(itemStack -> {
                        addBlockDrops(dropPositions, itemStack, blockPos);
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
    
    
    public static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> pDropPositionArray, ItemStack pStack, BlockPos pPos) {
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
        
        pDropPositionArray.add(Pair.of(pStack, pPos.immutable()));
    }
    
    
    
    public static boolean explode(CustomExplosion explosion) {
        World world = explosion.level;
        if (ForgeEventFactory.onExplosionStart(world, explosion)) {
            return false;
        }
        explosion.explode();
        explosion.finalizeExplosion(true);
        
        if (!world.isClientSide()) {
            if (explosion.blockInteraction == Explosion.Mode.NONE) {
                explosion.clearToBlow();
            }
            
            ResourceLocation explosionType = explosion.getExplosionType();
            if (explosionType != null) {
                Vector3d pos = explosion.getPosition();
                for (ServerPlayerEntity player : ((ServerWorld) world).players()) {
                    if (player.distanceToSqr(pos.x, pos.y, pos.z) < 4096) {
                        PacketManager.sendToClient(new CustomExplosionPacket(explosion, pos.x, pos.y, pos.z, 
                                explosion.radius, explosion.getToBlow(), explosion.getHitPlayers().get(player), explosionType), player);
                    }
                }
            }
        }
        
        return true;
    }
    
    public abstract ResourceLocation getExplosionType();
    
    public static class Register {
        public static final ResourceLocation CROSSFIRE_HURRICANE = new ResourceLocation(JojoMod.MOD_ID, "cfh");
        public static final ResourceLocation PILLAR_MAN_DETONATION = new ResourceLocation(JojoMod.MOD_ID, "acdc");
        public static final ResourceLocation HAMON = new ResourceLocation(JojoMod.MOD_ID, "hamon");
        public static final ResourceLocation STAND_HEAVY_PUNCH = new ResourceLocation(JojoMod.MOD_ID, "heavy_punch");
        
        public static final HashMap<ResourceLocation, CustomExplosionSupplier> REGISTER = Util.make(new HashMap<>(), map -> {
            map.put(CROSSFIRE_HURRICANE, MRCrossfireHurricaneEntity.CrossfireHurricaneExplosion::new);
            map.put(PILLAR_MAN_DETONATION, PillarmanSelfDetonation.PillarmanExplosion::new);
            map.put(HAMON, HamonBlastExplosion::new);
            map.put(STAND_HEAVY_PUNCH, StandEntityHeavyAttack.HeavyPunchBlockInstance.HeavyPunchExplosion::new);
        });
    }
    
    @FunctionalInterface
    public static interface CustomExplosionSupplier {
        CustomExplosion createExplosion(World pLevel, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius);
    }
}
