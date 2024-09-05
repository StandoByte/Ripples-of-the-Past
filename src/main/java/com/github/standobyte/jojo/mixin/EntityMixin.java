package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.github.standobyte.jojo.capability.world.TimeStopHandler;

import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

@Mixin(Entity.class)
public class EntityMixin {
    
    @Shadow
    public World level;
    
    @Redirect(method = "updateFluidHeightAndDoFluidPushing", at = @At(
            value = "INVOKE", 
            target = "Lnet/minecraft/fluid/FluidState;getFlow("
                    + "Lnet/minecraft/world/IBlockReader;"
                    + "Lnet/minecraft/util/math/BlockPos;)"
                    + "Lnet/minecraft/util/math/vector/Vector3d;"))
    public Vector3d jojoTsCancelFluidPush(FluidState fluidState, IBlockReader world, BlockPos blockPos) {
        if (TimeStopHandler.isTimeStopped(level, blockPos)) {
            return Vector3d.ZERO;
        }
        return fluidState.getFlow(world, blockPos);
    }
}
