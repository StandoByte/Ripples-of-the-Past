package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.capability.world.TimeStopHandler;

import net.minecraft.block.CommandBlockBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;

@Mixin(ServerChunkProvider.class)
public class ServerChunkProviderMixin {
    @Shadow @Final private ServerWorld level;

    @Inject(method = "isTickingChunk", at = @At("TAIL"), cancellable = true)
    public void jojoTsCancelBlockTick(BlockPos blockPos, CallbackInfoReturnable<Boolean> ci) {
        if (ci.getReturnValue() && TimeStopHandler.isTimeStopped(level, blockPos)
                && (/*cancelCommandBlocks || */ !(level.getBlockState(blockPos).getBlock() instanceof CommandBlockBlock))) {
            ci.setReturnValue(false);
        }
    }

}
