package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.action.non_stand.HamonWallClimbing2;

import net.minecraft.block.BarrierBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

@Mixin(BarrierBlock.class)
public abstract class BarrierBlockWallClimbMixin extends AbstractBlockMixin {

    @Override
    public void changeCollisionShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext, CallbackInfoReturnable<VoxelShape> ci) {
        if (HamonWallClimbing2.disableBlockCollisionShape(pContext)) {
            ci.setReturnValue(VoxelShapes.empty());
        }
    }
    
}
