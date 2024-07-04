package com.github.standobyte.jojo.power.impl.stand.type;

import java.util.List;
import java.util.UUID;

import com.github.standobyte.jojo.entity.mob.CocoJumboTurtleEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.world.dimension.ModDimensions;
import com.github.standobyte.jojo.world.dimension.mr_president.MrPresidentInsideTeleporter;
import com.github.standobyte.jojo.world.dimension.mr_president.MrPresidentWorldData;

import net.minecraft.entity.Entity;
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
            if (user.isAlive()) {
                mrPresidentTracker.ifPresent(tracker -> tracker.rememberTurtlePosition(user));
                
                if (power.canUsePower()) {
                    boolean canTeleport = true;
                    if (user instanceof CocoJumboTurtleEntity) {
                        CocoJumboTurtleEntity tutel = (CocoJumboTurtleEntity) user;
                        canTeleport = tutel.hasKey() || !tutel.hasAssignedKey();
                    }
                    if (canTeleport) {
                        List<Entity> entities = user.level.getEntities(user, user.getBoundingBox()
                                .move(0, 0.5, 0).inflate(0.25), EntityPredicates.NO_SPECTATORS);
                        if (!entities.isEmpty()) {
                            MinecraftServer server = ((ServerWorld) user.level).getServer();
                            ServerWorld mrPresidentWorld = server.getLevel(ModDimensions.MR_PRESIDENT);
                            if (mrPresidentWorld != null) {
                                for (Entity entity : entities) {
                                    if (!entity.isOnGround() && entity.getDeltaMovement().y < 0 && entity.getY() > user.getY(1)
                                            && entity.tickCount >= 20 && !MCUtil.hasIndirectPassenger(entity, user)) {
                                        UUID turtleId = user.getUUID();
                                        ITeleporter teleporter = new MrPresidentInsideTeleporter(turtleId);
                                        /* can't call changeDimension right away, 
                                         * because changeDimension immediately removes the entity, 
                                         * which can't be done while the entities are ticking */
                                        server.tell(new TickDelayedTask(server.getTickCount(), () -> {
                                            entity.changeDimension(mrPresidentWorld, teleporter);
                                        }));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
