package com.github.standobyte.jojo.world.dimension.mr_president;

import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;

public class MrPresidentBackTeleporter implements ITeleporter {
    public final ServerWorld world;
    public final Entity turtle;
    public final Vector3d pos;
    
    public MrPresidentBackTeleporter(ServerWorld world, Entity turtle, Vector3d pos) {
        this.world = world;
        this.turtle = turtle;
        this.pos = pos;
    }
    
    @Nullable
    public static MrPresidentBackTeleporter teleportToTurtle(MinecraftServer server, UUID turtleId) {
        for (ServerWorld world : server.getAllLevels()) {
            Entity turtle = world.getEntity(turtleId);
            if (turtle != null) {
                Vector3d pos = new Vector3d(turtle.getX(), turtle.getY(1), turtle.getZ());
                return new MrPresidentBackTeleporter(world, turtle, pos);
            }
        }
        
        return null;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destinationWorld,
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        entity = repositionEntity.apply(false);
        entity.teleportTo(pos.x, pos.y, pos.z);
        return entity;
    }

}
