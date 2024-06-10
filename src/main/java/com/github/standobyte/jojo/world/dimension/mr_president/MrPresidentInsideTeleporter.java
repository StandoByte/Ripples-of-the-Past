package com.github.standobyte.jojo.world.dimension.mr_president;

import java.util.UUID;
import java.util.function.Function;

import com.github.standobyte.jojo.capability.world.MrPresidentWorldDataProvider;
import com.github.standobyte.jojo.init.ModStructures;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;

public class MrPresidentInsideTeleporter implements ITeleporter {
    private final UUID turtleUUID;

    public MrPresidentInsideTeleporter(UUID turtleUUID) {
        this.turtleUUID = turtleUUID;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destinationWorld,
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        MrPresidentWorldData rooms = destinationWorld.getCapability(MrPresidentWorldDataProvider.CAPABILITY).orElse(null);
        if (rooms != null) {
            entity = repositionEntity.apply(false);
            MrPresidentWorldData.ChunkSectionPos roomChunkSectionPos = rooms.getAllocatedRoom(turtleUUID);
            boolean generateRoom = false;
            if (roomChunkSectionPos == null) {
                roomChunkSectionPos = rooms.posForNewRoom(turtleUUID);
                generateRoom = true;
            }
            BlockPos cornerPos = new BlockPos(
                    roomChunkSectionPos.x << 4,
                    roomChunkSectionPos.y << 4,
                    roomChunkSectionPos.z << 4);
            
            if (generateRoom) {
                ModStructures.CONFIGURED_MR_PRESIDENT_ROOM.get().place(destinationWorld, 
                        destinationWorld.getChunkSource().getGenerator(), destinationWorld.getRandom(), cornerPos);
            }
            Vector3d pos = new Vector3d(cornerPos.getX() + 8, cornerPos.getY() + 8, cornerPos.getZ() + 8);
            entity.teleportTo(pos.x, pos.y, pos.z);
        }
        
        return entity;
    }

}
