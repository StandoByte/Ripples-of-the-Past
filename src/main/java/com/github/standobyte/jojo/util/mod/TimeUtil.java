package com.github.standobyte.jojo.util.mod;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.capability.world.TimeStopHandler;
import com.github.standobyte.jojo.capability.world.TimeStopInstance;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
@Deprecated
public class TimeUtil {

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static void stopTime(World world, TimeStopInstance instance) {
        TimeStopHandler.stopTime(world, instance);
    }

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static void resumeTime(World world, int instanceId) {
        TimeStopHandler.resumeTime(world, instanceId);
    }

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static void resumeTime(World world, TimeStopInstance instance) {
        TimeStopHandler.resumeTime(world, instance);
    }
    
    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static TimeStopInstance getTimeStopInstance(World world, int instanceId) {
        return TimeStopHandler.getTimeStopInstance(world, instanceId);
    }

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static boolean canPlayerSeeInStoppedTime(PlayerEntity player) {
        return TimeStopHandler.canPlayerSeeInStoppedTime(player);
    }

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static boolean canPlayerSeeInStoppedTime(boolean canMove, boolean hasTimeStopAbility) {
        return TimeStopHandler.canPlayerSeeInStoppedTime(canMove, hasTimeStopAbility);
    }

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static boolean canPlayerMoveInStoppedTime(PlayerEntity player, boolean checkEffect) {
        return TimeStopHandler.canPlayerMoveInStoppedTime(player, checkEffect);
    }

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static boolean hasTimeStopAbility(LivingEntity entity) {
        return TimeStopHandler.hasTimeStopAbility(entity);
    }

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static boolean isTimeStopped(World world, BlockPos blockPos) {
        return TimeStopHandler.isTimeStopped(world, blockPos);
    }

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static boolean isTimeStopped(World world, ChunkPos chunkPos) {
        return TimeStopHandler.isTimeStopped(world, chunkPos);
    }

    @Deprecated
    /** @deprecated method moved to TimeStopHandler */
    public static int getTimeStopTicksLeft(World world, ChunkPos chunkPos) {
        return TimeStopHandler.getTimeStopTicksLeft(world, chunkPos);
    }
}