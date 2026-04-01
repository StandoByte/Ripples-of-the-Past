package com.github.standobyte.jojo.mixin.timestop;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.capability.world.TimeStopHandler;

import net.minecraft.profiler.IProfiler;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.ISpawnWorldInfo;

@Mixin(ServerWorld.class)
public abstract class WorldStopDaylightAndWeatherServer extends World {

    protected WorldStopDaylightAndWeatherServer(ISpawnWorldInfo pLevelData, RegistryKey<World> pDimension,
            DimensionType pDimensionType, Supplier<IProfiler> pProfiler, boolean pIsClientSide, boolean pIsDebug,
            long pBiomeZoomSeed) {
        super(pLevelData, pDimension, pDimensionType, pProfiler, pIsClientSide, pIsDebug, pBiomeZoomSeed);
    }

    @Inject(method = "tickTime", at = @At("HEAD"), cancellable = true)
    public void cancelDaylightCycle(CallbackInfo ci) {
        if (TimeStopHandler.shouldStopDaylightAndWeatherCycles(this)) {
            // thought it's preferrable to only cancel setDayTime call
            ci.cancel();
        }
    }
    
    /* TODO stop weather cycle in time stop 
     * (no longer using the gamerules because they're prone to breaking, 
     * but there is clean mixin for this on this version)
     */
}
