package com.github.standobyte.jojo.mrpresident;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.effect.StandEffectInstance;
import com.github.standobyte.jojo.action.stand.effect.StandEffectType;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentInsideTeleporter;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.world.dimension.ModDimensions;
import com.mojang.datafixers.util.Either;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;

public class MrPresidentEnteredRoomEffect extends StandEffectInstance {
    public UUID roomId;
    private Set<UUID> enteredEntities = new HashSet<>();
    private boolean prevTickRoomWasLocked;

    public MrPresidentEnteredRoomEffect(StandEffectType<?> effectType) {
        super(effectType);
    }
    
    public void onEntityEntered(Entity entity) {
        enteredEntities.add(entity.getUUID());
    }
    
    public void onEntityQuit(Entity entity) {
        enteredEntities.remove(entity.getUUID());
    }

    
    @Override
    protected void start() {}

    @Override
    protected void tick() {
        if (!world.isClientSide()) {
            boolean roomIsLocked = MrPresidentStandType.roomIsLocked(user);
            if (roomIsLocked && !prevTickRoomWasLocked) {
                if (!world.isClientSide()) {
                    ServerWorld serverWorld = (ServerWorld) world;
                    MinecraftServer server = serverWorld.getServer();
                    ServerWorld mrPresidentWorld = server.getLevel(ModDimensions.MR_PRESIDENT);
                    BlockPos roomLowerCorner = MrPresidentInsideTeleporter.getLowerCornerRoomPos(mrPresidentWorld, roomId);
                    if (roomLowerCorner != null) {
                        getChunkFuture(mrPresidentWorld.getChunkSource(), 
                                roomLowerCorner.getX(), roomLowerCorner.getZ(), ChunkStatus.FULL, true)
                        .thenRun(() -> {
                            teleportEntitiesBack(mrPresidentWorld, roomLowerCorner, entity -> entity instanceof LivingEntity && !(entity instanceof ArmorStandEntity));
                        });
                    }
                }
            }
            prevTickRoomWasLocked = roomIsLocked;
        }
    }

    @Override
    protected void stop() {
        if (!world.isClientSide()) {
            ServerWorld serverWorld = (ServerWorld) world;
            MinecraftServer server = serverWorld.getServer();
            ServerWorld mrPresidentWorld = server.getLevel(ModDimensions.MR_PRESIDENT);
            BlockPos roomLowerCorner = MrPresidentInsideTeleporter.getLowerCornerRoomPos(mrPresidentWorld, roomId);
            if (roomLowerCorner != null) {
                getChunkFuture(mrPresidentWorld.getChunkSource(), 
                        roomLowerCorner.getX(), roomLowerCorner.getZ(), ChunkStatus.FULL, true)
                .thenRun(() -> {
                    breakAndTeleportBlocks(mrPresidentWorld, roomLowerCorner);
                    teleportEntitiesBack(mrPresidentWorld, roomLowerCorner, null);
                });
            }
        }
    }
    
    public void teleportEntitiesBack(ServerWorld mrPresidentWorld, BlockPos roomLowerCorner, @Nullable Predicate<Entity> filter) {
        if (mrPresidentWorld == null) return;
        
        Set<Entity> entities = new HashSet<>();
        
        if (roomLowerCorner != null) {
            AxisAlignedBB aabb = new AxisAlignedBB(roomLowerCorner, new BlockPos(
                    roomLowerCorner.getX() + MrPresidentInsideTeleporter.ROOM_SIZE.getX(),
                    roomLowerCorner.getY() + MrPresidentInsideTeleporter.ROOM_SIZE.getY(),
                    roomLowerCorner.getZ() + MrPresidentInsideTeleporter.ROOM_SIZE.getZ()));
            entities.addAll(mrPresidentWorld.getEntities((Entity) null, aabb, filter));
        }
        
        for (UUID entityId : enteredEntities) {
            Entity entity = mrPresidentWorld.getEntity(entityId);
            if (entity != null && (filter == null || filter.test(entity))) {
                entities.add(entity);
            }
        }
        
        MinecraftServer server = mrPresidentWorld.getServer();
        for (Entity entity : entities) {
            MrPresidentStandType.teleportFromRoom(entity, roomId, server);
        }
    }
    
    public void breakAndTeleportBlocks(ServerWorld mrPresidentWorld, BlockPos roomLowerCorner) {
        if (mrPresidentWorld == null) return;
        
        int x0 = roomLowerCorner.getX();
        int y0 = roomLowerCorner.getY();
        int z0 = roomLowerCorner.getZ();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int x = 0; x < MrPresidentInsideTeleporter.ROOM_SIZE.getX(); x++) {
            for (int y = 0; y < MrPresidentInsideTeleporter.ROOM_SIZE.getY(); y++) {
                for (int z = 0; z < MrPresidentInsideTeleporter.ROOM_SIZE.getZ(); z++) {
                    pos.set(x0 + x, y0 + y, z0 + z);
                    BlockState blockState = mrPresidentWorld.getBlockState(pos);
                    if (!blockState.isAir() && blockState.getDestroySpeed(world, pos) >= 0) {
                        MCUtil.destroyBlock(mrPresidentWorld, pos, true, null);
                    }
                }
            }
        }
    }

    @Override
    protected boolean needsTarget() {
        return false;
    }
    
    
    public static CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>> getChunkFuture(ServerChunkProvider chunkProvider, 
            int chunkX, int chunkY, ChunkStatus requiredStatus, boolean load) {
        boolean flag = Thread.currentThread() == chunkProvider.mainThread;
        CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>> future;
        if (flag) {
            future = chunkProvider.getChunkFutureMainThread(chunkX, chunkY, requiredStatus, load);
            chunkProvider.mainThreadProcessor.managedBlock(future::isDone);
        } else {
            future = CompletableFuture.supplyAsync(() -> {
                return chunkProvider.getChunkFutureMainThread(chunkX, chunkY, requiredStatus, load);
            }, chunkProvider.mainThreadProcessor).thenCompose(Function.identity());
        }

        return future;
    }

}
