package com.github.standobyte.jojo.mixin;

import java.util.Collections;
import java.util.Optional;
import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;

import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(FireBlock.class)
public class FireBlockMixin {

    @Inject(method = "tryCatchFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    public void jojoOnFireRemovedBlock(World pLevel, BlockPos pPos, int pChance, Random pRandom, int pAge, Direction face, CallbackInfo ci) {
        cdRememberBurntBlock(pLevel, pPos);
    }
    
    @Inject(method = "tryCatchFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
    public void jojoOnFireReplacedBlock(World pLevel, BlockPos pPos, int pChance, Random pRandom, int pAge, Direction face, CallbackInfo ci) {
        cdRememberBurntBlock(pLevel, pPos);
    }
    
    private static void cdRememberBurntBlock(World world, BlockPos blockPos) {
        BlockState blockState = world.getBlockState(blockPos);
        CrazyDiamondRestoreTerrain.rememberBrokenBlock(world, blockPos, blockState, 
                Optional.ofNullable(world.getBlockEntity(blockPos)), Collections.emptyList());
    }
    
    // FIXME crossfire hurricane deleting blocks
    
}
