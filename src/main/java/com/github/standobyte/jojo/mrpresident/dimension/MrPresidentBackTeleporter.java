package com.github.standobyte.jojo.mrpresident.dimension;

import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData.MrPresidentTurtlePos;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;

public class MrPresidentBackTeleporter implements ITeleporter {
    public final ServerWorld world;
    public final Vector3d pos;
    @Nullable public final Entity turtle;
    
    public MrPresidentBackTeleporter(ServerWorld world, Vector3d pos, Entity turtle) {
        this.world = world;
        this.pos = pos;
        this.turtle = turtle;
    }
    
    @Nullable
    public static MrPresidentBackTeleporter teleportBackToTurtle(MinecraftServer server, UUID turtleId) {
        for (ServerWorld world : server.getAllLevels()) {
            Entity turtle = world.getEntity(turtleId);
            if (turtle != null) {
                Vector3d pos = posToTeleportTo(turtle);
                return new MrPresidentBackTeleporter(world, pos, turtle);
            }
        }

        MrPresidentWorldData rooms = MrPresidentWorldData.get(server).orElse(null);
        if (rooms != null) {
            MrPresidentTurtlePos turtleTrackedPos = rooms.getTurtlePosition(turtleId);
            if (turtleTrackedPos != null && turtleTrackedPos.turtleDimension != null && turtleTrackedPos.turtlePos != null) {
                ServerWorld world = server.getLevel(turtleTrackedPos.turtleDimension);
                if (world != null) {
                    return new MrPresidentBackTeleporter(world, turtleTrackedPos.turtlePos, null);
                }
            }
        }
        
        return null;
    }
    
    public static Vector3d posToTeleportTo(Entity turtle) {
        return new Vector3d(turtle.getX(), turtle.getY(1), turtle.getZ());
    }

    @Override
    public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destinationWorld,
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        entity = repositionEntity.apply(false);
        entity.teleportTo(pos.x, pos.y, pos.z);
        return entity;
    }
    
    @Override
    public boolean playTeleportSound(ServerPlayerEntity player, ServerWorld sourceWorld, ServerWorld destWorld) {
        return false;
    }

}
