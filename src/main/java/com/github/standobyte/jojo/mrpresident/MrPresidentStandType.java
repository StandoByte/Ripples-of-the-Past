package com.github.standobyte.jojo.mrpresident;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentBackTeleporter;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentInsideTeleporter;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.type.NoSummonStandType;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.world.dimension.ModDimensions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.common.util.LazyOptional;

public class MrPresidentStandType<T extends StandStats> extends NoSummonStandType<T> {
    
    public MrPresidentStandType(Builder<T> builder) {
        super(builder);
    }
    
    @Override
    public void tickUser(LivingEntity user, IStandPower power) {
        if (!user.level.isClientSide()) {
            LazyOptional<MrPresidentWorldData> mrPresidentTracker = MrPresidentWorldData.get(((ServerWorld) user.level).getServer());
            mrPresidentTracker.ifPresent(tracker -> tracker.rememberTurtlePosition(user));
            if (power.canUsePower()) {
                boolean canTeleport = true;
                if (user instanceof CocoJumboTurtleEntity) {
                    CocoJumboTurtleEntity tutel = (CocoJumboTurtleEntity) user;
                    canTeleport = tutel.hasKey() || !tutel.hasAssignedKey();
                }
                if (canTeleport) {
                    List<Entity> entities = findTargets(user, entity -> 
                            !entity.isOnGround() && entity.getDeltaMovement().y < 0 && entity.getY() > user.getY(1)
                            && (entity.tickCount >= 20 || entity.getType() == EntityType.ITEM) && !MCUtil.hasIndirectPassenger(entity, user));
                    teleportEntities(user, power, entities);
                }
            }
        }
    }
    
    public static List<Entity> findTargets(Entity turtle, @Nullable Predicate<Entity> filter) {
        Predicate<Entity> predicate = EntityPredicates.NO_SPECTATORS
                .and(entity -> entity.getBbWidth() < 4 && entity.getBbHeight() < 4);
        if (filter != null) {
            predicate = predicate.and(filter);
        }
        return turtle.level.getEntities(turtle, turtle.getBoundingBox()
                .move(0, 0.5, 0).inflate(0.25), predicate);
    }
    
    public static void teleportEntities(Entity turtle, IStandPower power, Collection<Entity> entities) {
        if (entities.isEmpty()) return;
            
        MinecraftServer server = ((ServerWorld) turtle.level).getServer();
        ServerWorld mrPresidentWorld = server.getLevel(ModDimensions.MR_PRESIDENT);
        if (mrPresidentWorld != null) {
            for (Entity entity : entities) {
                UUID turtleId = turtle.getUUID();
                teleportToRoom(entity, turtleId, server, power);
            }
        }
    }
    
    private static void teleportToRoom(Entity entity, UUID roomId, MinecraftServer server, IStandPower turtleStand) {
        ServerWorld mrPresidentWorld = server.getLevel(ModDimensions.MR_PRESIDENT);
        ITeleporter teleporter = new MrPresidentInsideTeleporter(roomId);
        /* can't call changeDimension right away, 
         * because changeDimension immediately removes the entity, 
         * which can't be done while the entities are ticking */
        server.tell(new TickDelayedTask(server.getTickCount(), () -> {
            entity.changeDimension(mrPresidentWorld, teleporter);
            if (turtleStand != null) {
                MrPresidentEnteredRoomEffect room = turtleStand.getContinuousEffects()
                        .getOrCreateEffect(ModStandEffects.MR_PRESIDENT_ENTITIES_ENTERED.get());
                room.roomId = roomId;
                room.onEntityEntered(entity);
            }
        }));
    }
    
    public static void teleportFromRoom(Entity entity, UUID roomId, MinecraftServer server) {
        MrPresidentBackTeleporter teleporter = MrPresidentBackTeleporter.teleportBackToTurtle(server, roomId);
        if (teleporter != null) {
            entity.changeDimension(teleporter.world, teleporter);
            if (teleporter.turtle instanceof LivingEntity) {
                LivingEntity turtle = (LivingEntity) teleporter.turtle;
                IStandPower.getStandPowerOptional(turtle).ifPresent(stand -> {
                    stand.getContinuousEffects()
                    .getEffectsOfType(ModStandEffects.MR_PRESIDENT_ENTITIES_ENTERED.get())
                    .forEach(room -> {
                        room.onEntityQuit(entity);
                    });
                });
            }
        }
    }

}
