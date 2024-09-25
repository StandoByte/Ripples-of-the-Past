package com.github.standobyte.jojo.util.mod;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.entity.damaging.projectile.ModdedProjectileEntity;
import com.github.standobyte.jojo.entity.mob.IMobStandUser;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModGamerules;
import com.github.standobyte.jojo.init.ModTags;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.item.ClothesSet;
import com.github.standobyte.jojo.modcompat.OptionalDependencyHelper;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PlayVoiceLinePacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.INPC;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.monster.AbstractIllagerEntity;
import net.minecraft.entity.monster.HuskEntity;
import net.minecraft.entity.monster.ZoglinEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.entity.passive.horse.ZombieHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;

public class JojoModUtil {
    
    public static RayTraceResult rayTrace(Entity entity, double reachDistance, @Nullable Predicate<Entity> entityFilter) {
        return rayTrace(entity, reachDistance, entityFilter, 0);
    }

    public static RayTraceResult rayTrace(Entity entity, double reachDistance, @Nullable Predicate<Entity> entityFilter, 
            double rayTraceInflate) {
        return rayTrace(entity, reachDistance, entityFilter, rayTraceInflate, 0);
    }

    public static RayTraceResult rayTrace(Entity entity, double reachDistance, 
            @Nullable Predicate<Entity> entityFilter, 
            double rayTraceInflate, double standPrecision) {
        return rayTraceMultipleEntities(entity, reachDistance, entityFilter, rayTraceInflate, standPrecision)[0];
    }
    
    public static RayTraceResult[] rayTraceMultipleEntities(Entity entity, double reachDistance, 
            @Nullable Predicate<Entity> entityFilter, 
            double rayTraceInflate, double standPrecision) {
        return rayTraceMultipleEntities(entity.getEyePosition(1.0F), entity.getViewVector(1.0F), reachDistance, 
                entity.level, entity, 
                entityFilter, RayTraceContext.BlockMode.OUTLINE, 
                rayTraceInflate, standPrecision);
    }
    
    public static RayTraceResult[] rayTraceMultipleEntities(Entity entity, double reachDistance, 
            @Nullable Predicate<Entity> entityFilter, RayTraceContext.BlockMode blockMode, 
            double rayTraceInflate, double standPrecision) {
        return rayTraceMultipleEntities(entity.getEyePosition(1.0F), entity.getViewVector(1.0F), reachDistance, 
                entity.level, entity, entityFilter, blockMode, rayTraceInflate, standPrecision);
    }
    
    public static RayTraceResult[] rayTraceMultipleEntities(Vector3d startPos, Vector3d rayVec, double distance, 
            World world, @Nullable Entity entity, 
            @Nullable Predicate<Entity> entityFilter, RayTraceContext.BlockMode blockMode, 
            double rayTraceInflate, double standPrecision) {
        Vector3d rtVec = rayVec.normalize().scale(distance);
        Vector3d endPos = startPos.add(rtVec);
        AxisAlignedBB aabb = entity.getBoundingBox().expandTowards(rtVec).inflate(1.0D);
        return rayTraceMultipleEntities(startPos, endPos, aabb, 
                distance, entity.level, entity, entityFilter, blockMode, 
                rayTraceInflate, standPrecision);
    }

    public static RayTraceResult rayTrace(Vector3d startPos, Vector3d rayVec, double distance, 
            World world, @Nullable Entity entity, 
            @Nullable Predicate<Entity> entityFilter, 
            double rayTraceInflate, double standPrecision) {
        return rayTraceMultipleEntities(startPos, rayVec, distance, 
                world, entity, 
                entityFilter, RayTraceContext.BlockMode.OUTLINE, 
                rayTraceInflate, standPrecision)[0];
    }

    public static RayTraceResult[] rayTraceMultipleEntities(Vector3d startPos, Vector3d endPos, AxisAlignedBB aabb, 
            double minDistance, World world, @Nullable Entity entity, 
            @Nullable Predicate<Entity> entityFilter, 
            double rayTraceInflate, double standPrecision) {
        return rayTraceMultipleEntities(startPos, endPos, aabb, 
                minDistance, world, entity, 
                entityFilter, RayTraceContext.BlockMode.OUTLINE, 
                rayTraceInflate, standPrecision);
    }

    public static RayTraceResult[] rayTraceMultipleEntities(Vector3d startPos, Vector3d endPos, AxisAlignedBB aabb, 
            double minDistance, World world, @Nullable Entity entity, 
            @Nullable Predicate<Entity> entityFilter, RayTraceContext.BlockMode blockMode, 
            double rayTraceInflate, double standPrecision) {
        aabb.inflate(rayTraceInflate);
        double minDistanceSqr = minDistance * minDistance;
        Map<EntityRayTraceResult, Double> rayTracedWithDistance = new HashMap<>();
        List<Entity> entities = world.getEntities(entity, aabb, e -> !e.isSpectator() && e.isPickable() && (entityFilter == null || entityFilter.test(e)));
        for (Entity potentialTarget : entities) {
            AxisAlignedBB targetCollisionAABB = potentialTarget.getBoundingBox().inflate((double) potentialTarget.getPickRadius() + rayTraceInflate);
            targetCollisionAABB = standPrecisionTargetHitbox(targetCollisionAABB, standPrecision);
            Optional<Vector3d> clipOptional = targetCollisionAABB.clip(startPos, endPos);
            if (targetCollisionAABB.contains(startPos)) {
                rayTracedWithDistance.put(new EntityRayTraceResult(potentialTarget, clipOptional.orElse(startPos)), 0.0);
            } else if (clipOptional.isPresent()) {
                Vector3d clipVec = clipOptional.get();
                double clipDistanceSqr = startPos.distanceToSqr(clipVec);
                if (clipDistanceSqr < minDistanceSqr || minDistanceSqr == 0.0D) {
                    if (entity != null && potentialTarget.getRootVehicle() == entity.getRootVehicle() && !potentialTarget.canRiderInteract()) {
                        if (minDistanceSqr == 0.0D) {
                            rayTracedWithDistance.put(new EntityRayTraceResult(potentialTarget, clipVec), 0.0);
                        }
                    } else {
                        rayTracedWithDistance.put(new EntityRayTraceResult(potentialTarget, clipVec), clipDistanceSqr);
                    }
                }
            }
        }
        if (rayTracedWithDistance.isEmpty()) {
            return new RayTraceResult[] { 
                    world.clip(new RayTraceContext(startPos, endPos, blockMode, RayTraceContext.FluidMode.NONE, entity))
                    };
        }
        return rayTracedWithDistance.entrySet().stream()
        .sorted(Comparator.comparingDouble(Map.Entry::getValue))
        .map(Map.Entry::getKey)
        .toArray(EntityRayTraceResult[]::new);
        
//        return new EntityRayTraceResult(targetEntity, targetEntityPos);
    }
    
    private static AxisAlignedBB standPrecisionTargetHitbox(AxisAlignedBB aabb, double precision) {
        if (precision > 4) {
            double smallAabbAddFraction = Math.min(Math.pow(2, precision - 16), 1);
            aabb.inflate(
                    Math.max(1.0 - aabb.getXsize(), 0) * smallAabbAddFraction, 
                    Math.max(1.0 - aabb.getYsize(), 0) * smallAabbAddFraction, 
                    Math.max(1.0 - aabb.getZsize(), 0) * smallAabbAddFraction);
            
            double scale = precision / 5 + 0.2;
            double xSize = aabb.getXsize();
            double ySize = aabb.getYsize();
            double zSize = aabb.getZsize();
            aabb = MCUtil.scale(aabb, 
                    Math.min(scale, 1 + 4 / xSize), 
                    Math.min(scale, 1 + 4 / ySize), 
                    Math.min(scale, 1 + 4 / zSize));
        }
        return aabb;
    }

    public static RayTraceResult getHitResult(Entity projectile, @Nullable Predicate<Entity> targetPredicate, RayTraceContext.BlockMode blockMode) {
        World world = projectile.level;
        Vector3d pos = projectile.position();
        Vector3d nextPos = pos.add(projectile.getDeltaMovement());
        RayTraceResult rayTraceResult = world.clip(new RayTraceContext(pos, nextPos, blockMode, RayTraceContext.FluidMode.NONE, projectile));
        if (rayTraceResult.getType() != RayTraceResult.Type.MISS) {
            nextPos = rayTraceResult.getLocation();
        }
        RayTraceResult entityRayTraceResult = ProjectileHelper.getEntityHitResult(world, projectile, pos, nextPos, 
                projectile.getBoundingBox().expandTowards(projectile.getDeltaMovement()).inflate(1.0D), targetPredicate);
        if (entityRayTraceResult != null) {
            rayTraceResult = entityRayTraceResult;
        }
        return rayTraceResult;
    }
    
    // Item.getPlayerPOVHitResult
    protected static BlockRayTraceResult getPlayerPOVHitResult(World world, LivingEntity entity, RayTraceContext.FluidMode fluidMode) {
        float xRot = entity.xRot;
        float yRot = entity.yRot;
        Vector3d eyePos = entity.getEyePosition(1.0F);
        float f2 = MathHelper.cos(-yRot * ((float)Math.PI / 180F) - (float)Math.PI);
        float f3 = MathHelper.sin(-yRot * ((float)Math.PI / 180F) - (float)Math.PI);
        float f4 = -MathHelper.cos(-xRot * ((float)Math.PI / 180F));
        float f5 = MathHelper.sin(-xRot * ((float)Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double distance = Optional.ofNullable(entity.getAttribute(ForgeMod.REACH_DISTANCE.get())).map(ModifiableAttributeInstance::getValue).orElse(5D);
        Vector3d vector3d1 = eyePos.add((double)f6 * distance, (double)f5 * distance, (double)f7 * distance);
        return world.clip(new RayTraceContext(eyePos, vector3d1, RayTraceContext.BlockMode.OUTLINE, fluidMode, entity));
    }

    public static boolean isAnotherEntityTargeted(RayTraceResult rayTraceResult, Entity targettingEntity) {
        return (rayTraceResult.getType() == RayTraceResult.Type.ENTITY
                && !((EntityRayTraceResult) rayTraceResult).getEntity().is(targettingEntity));
    }

    public static double getDistance(Entity entity, AxisAlignedBB targetAabb) {
        Vector3d startPos = entity.getEyePosition(1.0F);
        if (targetAabb.contains(startPos)) {
            return 0;
        }
        Vector3d endPos = new Vector3d(
                MathHelper.lerp(0.5D, targetAabb.minX, targetAabb.maxX), 
                MathHelper.lerp(entity.getBbHeight() == 0 ? 0 : 
                    entity.getEyeHeight() / entity.getBbHeight(), targetAabb.minY, targetAabb.maxY), 
                MathHelper.lerp(0.5D, targetAabb.minZ, targetAabb.maxZ));
        Optional<Vector3d> clipOptional = targetAabb.clip(startPos, endPos);
        return clipOptional.map(clipVec -> startPos.distanceTo(clipVec) - entity.getBbWidth() / 2).orElse(-1D);
    }



    public static boolean canEntityDestroy(ServerWorld world, BlockPos blockPos, BlockState blockState, LivingEntity entity) {
        if (breakingBlocksEnabled(world)
                && blockState.canEntityDestroy(world, blockPos, entity)
                && ForgeEventFactory.onEntityDestroyBlock(entity, blockPos, blockState)) {
            PlayerEntity player = null;
            if (entity instanceof PlayerEntity) {
                player = (PlayerEntity) entity;
            }
            else if (entity instanceof StandEntity) {
                LivingEntity standUser = ((StandEntity) entity).getUser();
                if (standUser instanceof PlayerEntity) {
                    player = (PlayerEntity) standUser;
                }
            }
            return player == null || world.mayInteract(player, blockPos);
        }
        return false;
    }
    
    public static boolean breakingBlocksEnabled(World world) {
        return world.getGameRules().getBoolean(ModGamerules.BREAK_BLOCKS);
    }



    public static ResourceLocation makeTextureLocation(String folderName, String namespace, String path) {
        return makeTextureLocation(folderName, namespace, path, true);
    }

    public static ResourceLocation makeTextureLocation(String folderName, String namespace, String path, boolean addPngExtension) {
        return new ResourceLocation(namespace, "textures/"+ folderName + "/" + path + (addPngExtension ? ".png" : ""));
    }
    
    
    
    public static boolean playerHasClientInput(PlayerEntity player) {
        return player.level.isClientSide() ? InputHandler.getInstance().hasInput
                : player.getCapability(PlayerUtilCapProvider.CAPABILITY).map(PlayerUtilCap::hasClientInput).orElse(false);
    }
    


    @Deprecated
    public static boolean isUndead(LivingEntity entity) {
        return isUndeadOrVampiric(entity);
    }

    @Deprecated
    public static boolean isPlayerUndead(PlayerEntity player) {
        return isPlayerJojoVampiric(player);
    }

    public static boolean isUndeadOrVampiric(LivingEntity entity) {
        if (entity.getMobType() == CreatureAttribute.UNDEAD) {
            return true;
        }
        if (entity instanceof PlayerEntity) {
            return isPlayerJojoVampiric((PlayerEntity) entity);
        }
        return false;
    }
    
    /** 
     * You don't have to call this, it's just a condition to change the PlayerEntity's getMobType() to CreatureAttribute.UNDEAD via a mixin
     */
    public static boolean playerUndeadAttribute(LivingEntity player) {
        return INonStandPower.getNonStandPowerOptional(player).map(power -> {
            NonStandPowerType<?> powerType = power.getType();
            return powerType == ModPowers.VAMPIRISM.get() || powerType == ModPowers.ZOMBIE.get();
        }).orElse(false);
    }
    
    /**
     * Is treated differently from the conventional vanilla "undead"
     */
    public static boolean isPlayerJojoVampiric(PlayerEntity player) {
        return INonStandPower.getNonStandPowerOptional(player).map(power -> {
            NonStandPowerType<?> powerType = power.getType();
            return powerType == ModPowers.VAMPIRISM.get() || powerType == ModPowers.PILLAR_MAN.get();
        }).orElse(false); 
    }
    
    public static boolean isAffectedByHamon(LivingEntity entity) {
        if (ModTags.NO_HAMON_DAMAGE.contains(entity.getType())) {
            return false;
        }
        if (ModTags.HAMON_DAMAGE.contains(entity.getType())) {
            return true;
        }
        return JojoModUtil.isUndeadOrVampiric(entity) || OptionalDependencyHelper.vampirism().isEntityVampire(entity);
    }

    public static boolean canBleed(LivingEntity entity) {
        if (entity.getMobType() == CreatureAttribute.UNDEAD) {
            return entity instanceof PlayerEntity
                    || entity instanceof ZombieEntity && !(entity instanceof HuskEntity)
                    || entity instanceof ZoglinEntity
                    || entity instanceof ZombieHorseEntity;
        }
        return entity instanceof PlayerEntity
                || entity instanceof AgeableEntity
                || entity instanceof INPC
                || entity instanceof AbstractIllagerEntity
                || entity instanceof WaterMobEntity
                || entity instanceof IMobStandUser;
    }

    public static void extinguishFieryStandEntity(Entity entity, ServerWorld world) {
        MCUtil.playSound(world, null, entity.getX(), entity.getY(), entity.getZ(), 
                SoundEvents.FIRE_EXTINGUISH, entity.getSoundSource(), 1.0F, 1.0F, StandUtil::playerCanHearStands);
        world.sendParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY(), entity.getZ(), 
                8, entity.getBbWidth() / 2F, entity.getBbHeight() / 2F, entity.getBbWidth() / 2F, 0);
        entity.remove();
    }
    
    public static void deflectProjectile(Entity projectile, @Nullable Vector3d deflectVec) {
        projectile.setDeltaMovement(deflectVec != null ? 
                deflectVec.scale(Math.sqrt(projectile.getDeltaMovement().lengthSqr() / deflectVec.lengthSqr()))
                : projectile.getDeltaMovement().reverse());
        projectile.move(MoverType.SELF, projectile.getDeltaMovement());
        if (projectile instanceof ModdedProjectileEntity) {
            ((ModdedProjectileEntity) projectile).setIsDeflected();
        }
    }
    
    public static boolean isTargetBlocking(LivingEntity target) {
        ItemStack usedItem = target.getUseItem();
        if (!usedItem.isEmpty() && usedItem.isShield(target)) {
            return true;
        }
        if (target instanceof StandEntity) {
            StandEntity stand = (StandEntity) target;
            if (stand.isBlocking()) {
                return true;
            }
        }
        return false;
    }
    
    
    
    public static void sayVoiceLine(LivingEntity entity, SoundEvent voiceLine) {
        sayVoiceLine(entity, voiceLine, null);
    }

    public static void sayVoiceLine(LivingEntity entity, SoundEvent voiceLine, 
            @Nullable ClothesSet character) {
        sayVoiceLine(entity, voiceLine, character, 1.0F, 1.0F, false);
    }

    public static void sayVoiceLine(LivingEntity entity, SoundEvent voiceLine, 
            @Nullable ClothesSet character, boolean interrupt) {
        sayVoiceLine(entity, voiceLine, character, 1.0F, 1.0F, interrupt);
    }

    public static void sayVoiceLine(LivingEntity entity, SoundEvent sound, 
            @Nullable ClothesSet character, float volume, float pitch, boolean interrupt) {
        sayVoiceLine(entity, sound, character, volume, pitch, 200, interrupt);
    }

    public static void sayVoiceLine(LivingEntity entity, SoundEvent sound, 
            @Nullable ClothesSet character, float volume, float pitch, int voiceLineDelay, boolean interrupt) {
        if (entity.level.isClientSide() || entity.hasEffect(Effects.INVISIBILITY) ||
                character != null && character != ClothesSet.getClothesSet(entity)) {
            return;
        }
        SoundCategory category = SoundCategory.VOICE;
        if (entity instanceof PlayerEntity) {
            PlayVoiceLinePacket packet;
            if (!canPlayVoiceLine((PlayerEntity) entity, sound, voiceLineDelay)) {
                packet = PlayVoiceLinePacket.notTriggered(entity.getId());
            }
            else {
                PlaySoundAtEntityEvent event = ForgeEventFactory.onPlaySoundAtEntity(null, sound, category, volume, pitch);
                if (event.isCanceled() || event.getSound() == null) {
                    packet = PlayVoiceLinePacket.notTriggered(entity.getId());
                }
                else {
                    sound = event.getSound();
                    category = event.getCategory();
                    volume = event.getVolume();
                    packet = new PlayVoiceLinePacket(sound, category, entity.getId(), volume, pitch, interrupt);
                }
            }
            PacketManager.sendToNearby(packet, null, entity.getX(), entity.getY(), entity.getZ(), 
                    volume > 1.0F ? (double) (16.0F * volume) : 16.0D, entity.level.dimension());
        }
        else {
            entity.level.playSound(null, entity, sound, category, volume, pitch);
        }
    }

    private static boolean canPlayVoiceLine(PlayerEntity entity, SoundEvent voiceLine, int voiceLineDelay) {
        return entity.getCapability(PlayerUtilCapProvider.CAPABILITY)
                .map(cap -> cap.checkNotRepeatingVoiceLine(voiceLine, voiceLineDelay)).orElse(true);
    }
    
    
    @Deprecated
    public static boolean useShiftVar(LivingEntity user) {
        return user.isShiftKeyDown();
    }
    
    
    public static <T extends INBTSerializable<N>, N extends INBT> IStorage<T> makeSerializableStorage() {
        return new IStorage<T>() {
            @Override public INBT writeNBT(Capability<T> capability, T instance, Direction side) { return instance.serializeNBT(); }
            @Override public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt) { instance.deserializeNBT((N) nbt); }
        };
    }
    
    public static <T> IStorage<T> noStorage() {
        return new IStorage<T>() {
            @Override public INBT writeNBT(Capability<T> capability, T instance, Direction side) { return null; }
            @Override public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt) {}
        };
    }
    
    
    
    public static enum Direction2D {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }
}
