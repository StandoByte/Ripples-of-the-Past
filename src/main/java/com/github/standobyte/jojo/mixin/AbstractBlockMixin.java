package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {

    @Inject(method = "Lnet/minecraft/block/AbstractBlock;getCollisionShape("
            + "Lnet/minecraft/block/BlockState;"
            + "Lnet/minecraft/world/IBlockReader;"
            + "Lnet/minecraft/util/math/BlockPos;"
            + "Lnet/minecraft/util/math/shapes/ISelectionContext;"
            + ")Lnet/minecraft/util/math/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    public void changeCollisionShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext, CallbackInfoReturnable<VoxelShape> ci) {}
}
