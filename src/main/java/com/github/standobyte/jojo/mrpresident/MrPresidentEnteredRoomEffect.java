package com.github.standobyte.jojo.mrpresident;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.stand.effect.StandEffectInstance;
import com.github.standobyte.jojo.action.stand.effect.StandEffectType;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentInsideTeleporter;
import com.github.standobyte.jojo.world.dimension.ModDimensions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
                teleportEntitiesBack(entity -> entity instanceof LivingEntity && !(entity instanceof ArmorStandEntity));
            }
            prevTickRoomWasLocked = roomIsLocked;
        }
    }

    @Override
    protected void stop() {
        breakAndTeleportBlocks();
        teleportEntitiesBack(null);
    }
    
    public void teleportEntitiesBack(@Nullable Predicate<Entity> filter) {
        if (!world.isClientSide()) {
            ServerWorld serverWorld = (ServerWorld) world;
            MinecraftServer server = serverWorld.getServer();
            ServerWorld mrPresidentWorld = server.getLevel(ModDimensions.MR_PRESIDENT);
            if (mrPresidentWorld != null) {
                Set<Entity> entities = new HashSet<>();
                
                BlockPos roomCorner1 = MrPresidentInsideTeleporter.getCorner1RoomPos(mrPresidentWorld, roomId);
                if (roomCorner1 != null) {
                    AxisAlignedBB aabb = new AxisAlignedBB(roomCorner1, new BlockPos(
                            roomCorner1.getX() + MrPresidentInsideTeleporter.ROOM_SIZE.getX(),
                            roomCorner1.getY() + MrPresidentInsideTeleporter.ROOM_SIZE.getY(),
                            roomCorner1.getZ() + MrPresidentInsideTeleporter.ROOM_SIZE.getZ()));
                    entities.addAll(mrPresidentWorld.getEntities((Entity) null, aabb, filter));
                }
                
                for (UUID entityId : enteredEntities) {
                    Entity entity = mrPresidentWorld.getEntity(entityId);
                    if (entity != null && (filter == null || filter.test(entity))) {
                        entities.add(entity);
                    }
                }
                
                for (Entity entity : entities) {
                    MrPresidentStandType.teleportFromRoom(entity, roomId, server);
                }
            }
        }
    }
    
    public void breakAndTeleportBlocks() {
        // TODO
    }

    @Override
    protected boolean needsTarget() {
        return false;
    }

}
