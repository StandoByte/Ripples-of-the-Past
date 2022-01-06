package com.github.standobyte.jojo.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PlayVoiceLinePacket;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.stand.StandUtil;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.INPC;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.AbstractIllagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.network.play.server.SSpawnMovingSoundEffectPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;

public class JojoModUtil {
    
    public static void debug(World world, PlayerEntity player) { // FIXME
    }
    
    public static RayTraceResult rayTrace(Entity entity, double reachDistance, @Nullable Predicate<Entity> entityFilter) {
        return rayTrace(entity, reachDistance, entityFilter, 0);
    }
    
    public static RayTraceResult rayTrace(Entity entity, double reachDistance, @Nullable Predicate<Entity> entityFilter, double additionalAccuracy) {
        Vector3d startPos = entity.getEyePosition(1.0F);
        Vector3d rtVec = entity.getViewVector(1.0F).scale(reachDistance);
        Vector3d endPos = startPos.add(rtVec);
        AxisAlignedBB aabb = entity.getBoundingBox().expandTowards(rtVec).inflate(1.0D + additionalAccuracy);
        return rayTrace(startPos, endPos, aabb, reachDistance, entity.level, entity, entityFilter, additionalAccuracy);
    }
    
    public static RayTraceResult rayTrace(Vector3d startPos, Vector3d endPos, AxisAlignedBB aabb, 
            double minDistance, World world, @Nullable Entity entity, @Nullable Predicate<Entity> entityFilter, double additionalAccuracy) {
        Entity targetEntity = null;
        Vector3d targetEntityPos = null;
        minDistance *= minDistance;
        for (Entity potentialTarget : world.getEntities(entity, aabb, e -> !e.isSpectator() && e.isPickable() && (entityFilter == null || entityFilter.test(e)))) {
            AxisAlignedBB targetCollisionAABB = potentialTarget.getBoundingBox().inflate((double) potentialTarget.getPickRadius() + additionalAccuracy);
            Optional<Vector3d> clipOptional = targetCollisionAABB.clip(startPos, endPos);
            if (targetCollisionAABB.contains(startPos)) {
                if (minDistance >= 0.0D) {
                    targetEntity = potentialTarget;
                    targetEntityPos = clipOptional.orElse(startPos);
                    minDistance = 0.0D;
                }
            } else if (clipOptional.isPresent()) {
                Vector3d clipVec = clipOptional.get();
                double clipDistanceSqr = startPos.distanceToSqr(clipVec);
                if (clipDistanceSqr < minDistance || minDistance == 0.0D) {
                    if (entity != null && potentialTarget.getRootVehicle() == entity.getRootVehicle() && !potentialTarget.canRiderInteract()) {
                        if (minDistance == 0.0D) {
                            targetEntity = potentialTarget;
                            targetEntityPos = clipVec;
                        }
                    } else {
                        targetEntity = potentialTarget;
                        targetEntityPos = clipVec;
                        minDistance = clipDistanceSqr;
                    }
                }
            }
        }
        if (targetEntity == null) {
            return world.clip(new RayTraceContext(startPos, endPos, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, entity));
        }
        return new EntityDistanceRayTraceResult(targetEntity, targetEntityPos, Math.sqrt(minDistance));
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
    
    public static Iterable<Entity> getAllEntities(World world) {
        return world.isClientSide() ? ((ClientWorld) world).entitiesForRendering() : ((ServerWorld) world).getAllEntities();
    }
    
    public static void rotateTowards(Entity entity, Vector3d targetPos) {
        Vector3d targetVec = targetPos.subtract(entity.getEyePosition(1.0F));
        float yRot = MathUtil.yRotDegFromVec(targetVec);
        float xRot = MathUtil.xRotDegFromVec(targetVec);
        entity.yRot = yRot % 360.0F;
        entity.xRot = xRot % 360.0F;
        entity.setYHeadRot(yRot);
    }
    
    
    
    public static boolean canEntityDestroy(World world, BlockPos pos, LivingEntity entity) {
        BlockState state = world.getBlockState(pos);
        return JojoModConfig.COMMON.abilitiesBreakBlocks.get() && state.canEntityDestroy(world, pos, entity) && ForgeEventFactory.onEntityDestroyBlock(entity, pos, state);
    }
    
    
    
    public static ResourceLocation makeTextureLocation(String folderName, String namespace, String path) {
        return new ResourceLocation(namespace, "textures/"+ folderName + "/" + path + ".png");
    }
    

    
    public static <T extends Entity> List<T> entitiesAround(Class<? extends T> clazz, Entity centerEntity, double radius, boolean includeSelf, @Nullable Predicate<? super T> filter) {
        AxisAlignedBB aabb = new AxisAlignedBB(centerEntity.position().subtract(radius, radius, radius), centerEntity.position().add(radius, radius, radius));
        return centerEntity.level.getEntitiesOfClass(clazz, aabb, entity -> (includeSelf || entity != centerEntity) && (filter == null || filter.test(entity)));
    }

    public static boolean isUndead(LivingEntity entity) {
        if (entity.getMobType() == CreatureAttribute.UNDEAD) {
            return true;
        }
        if (entity instanceof PlayerEntity) {
            return isPlayerUndead((PlayerEntity) entity);
        }
        return false;
    }

    public static boolean isPlayerUndead(PlayerEntity player) {
        return INonStandPower.getNonStandPowerOptional(player).map(power -> {
            NonStandPowerType<?> powerType = power.getType();
            return powerType == ModNonStandPowers.VAMPIRISM.get();
        }).orElse(false); 
    }

    public static boolean canBleed(LivingEntity entity) {
        return entity instanceof PlayerEntity || entity instanceof AgeableEntity || entity instanceof INPC || entity instanceof AbstractIllagerEntity;
    }
    
    public static void extinguishFieryStandEntity(Entity entity, ServerWorld world) {
        JojoModUtil.playSound(world, null, entity.getX(), entity.getY(), entity.getZ(), 
                SoundEvents.FIRE_EXTINGUISH, entity.getSoundSource(), 1.0F, 1.0F, StandUtil::isPlayerStandUser);
        world.sendParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY(), entity.getZ(), 
                8, entity.getBbWidth() / 2F, entity.getBbHeight() / 2F, entity.getBbWidth() / 2F, 0);
        entity.remove();
    }
    
    public static void sayVoiceLine(LivingEntity entity, SoundEvent voiceLine) {
        sayVoiceLine(entity, voiceLine, 1.0F, 1.0F);
    }
    
    public static void sayVoiceLine(LivingEntity entity, SoundEvent voiceLine, float volume, float pitch) {
        if (!entity.level.isClientSide() && canPlayVoiceLine(entity, voiceLine)) {
            if (entity instanceof PlayerEntity) {
                playVoiceLineSound(entity, voiceLine, entity.getSoundSource(), volume, pitch);
            }
            else {
                entity.level.playSound(null, entity, voiceLine, entity.getSoundSource(), volume, pitch);
            }
        }
    }
    
    private static boolean canPlayVoiceLine(LivingEntity entity, SoundEvent voiceLine) {
        if (entity.hasEffect(Effects.INVISIBILITY)) {
            return false;
        }
        if (entity instanceof PlayerEntity) {
            return ((PlayerEntity) entity).getCapability(PlayerUtilCapProvider.CAPABILITY)
                    .map(cap -> cap.checkNotRepeatingVoiceLine(voiceLine)).orElse(true);
        }
        return true;
    }

    private static void playVoiceLineSound(Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        PlaySoundAtEntityEvent event = ForgeEventFactory.onPlaySoundAtEntity(null, sound, category, volume, pitch);
        if (event.isCanceled() || event.getSound() == null) return;
        sound = event.getSound();
        category = event.getCategory();
        volume = event.getVolume();
        PacketManager.sendToNearby(new PlayVoiceLinePacket(sound, category, entity.getId(), volume, pitch), null, 
                entity.getX(), entity.getY(), entity.getZ(), volume > 1.0F ? (double) (16.0F * volume) : 16.0D, entity.level.dimension());
    }

    public static void playSound(World world, @Nullable PlayerEntity clientHandled, BlockPos blockPos, 
            SoundEvent sound, SoundCategory category, float volume, float pitch, Predicate<PlayerEntity> condition) {
        playSound(world, clientHandled, (double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D, 
                sound, category, volume, pitch, condition);
    }

    public static void playSound(World world, @Nullable PlayerEntity clientHandled, double x, double y, double z, 
            SoundEvent sound, SoundCategory category, float volume, float pitch, Predicate<PlayerEntity> condition) {
        if (!world.isClientSide()) {
            PlaySoundAtEntityEvent event = ForgeEventFactory.onPlaySoundAtEntity(null, sound, category, volume, pitch);
            if (event.isCanceled() || event.getSound() == null) return;
            sound = event.getSound();
            category = event.getCategory();
            volume = event.getVolume();
            pitch = event.getPitch();
            broadcastWithCondition(((ServerWorld) world).getServer().getPlayerList().getPlayers(), clientHandled, 
                    x, y, z, volume > 1.0F ? (double)(16.0F * volume) : 16.0D, world.dimension(), 
                    new SPlaySoundEffectPacket(sound, category, x, y, z, volume, pitch), condition);
        }
        else if (clientHandled != null && condition.test(clientHandled)) {
            world.playSound(clientHandled, x, y, z, sound, category, volume, pitch);
        }
    }

    public static void playSound(World world, @Nullable PlayerEntity clientHandled, Entity entity, 
            SoundEvent sound, SoundCategory category, float volume, float pitch, Predicate<PlayerEntity> condition) {
        if (!world.isClientSide()) {
            PlaySoundAtEntityEvent event = ForgeEventFactory.onPlaySoundAtEntity(entity, sound, category, volume, pitch);
            if (event.isCanceled() || event.getSound() == null) return;
            sound = event.getSound();
            category = event.getCategory();
            volume = event.getVolume();
            pitch = event.getPitch();
            broadcastWithCondition(((ServerWorld) world).getServer().getPlayerList().getPlayers(), clientHandled, 
                    entity.getX(), entity.getY(), entity.getZ(), volume > 1.0F ? (double)(16.0F * volume) : 16.0D, world.dimension(), 
                    new SSpawnMovingSoundEffectPacket(sound, category, entity, volume, pitch), condition);
        }
        else if (clientHandled != null && condition.test(clientHandled)) {
            world.playSound(clientHandled, entity, sound, category, volume, pitch);
        }
    }

    private static void broadcastWithCondition(List<ServerPlayerEntity> players, @Nullable PlayerEntity clientHandled, 
            double x, double y, double z, double radius, RegistryKey<World> dimension, IPacket<IClientPlayNetHandler> packet, 
            Predicate<PlayerEntity> condition) {
        for (int i = 0; i < players.size(); ++i) {
            ServerPlayerEntity player = players.get(i);
            if (player != clientHandled && player.level.dimension() == dimension && condition.test(player)) {
                double d0 = x - player.getX();
                double d1 = y - player.getY();
                double d2 = z - player.getZ();
                if (d0 * d0 + d1 * d1 + d2 * d2 < radius * radius) {
                    player.connection.send(packet);
                }
            }
        }
    }
}
