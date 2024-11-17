package com.github.standobyte.jojo.mixin;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

@Mixin(LootTable.class)
public class LootTableMixin {

    @Inject(method = "getRandomItems(Lnet/minecraft/loot/LootContext;)Ljava/util/List;", at = @At("RETURN"))
    public void jojoRememberBlockLoot(LootContext context, CallbackInfoReturnable<List<ItemStack>> ci) {
        if (!LootParameterSets.BLOCK.getRequired().stream().anyMatch(param -> !context.hasParam(param))) {
            World world = context.getLevel();
            if (world != null) {
                List<ItemStack> generatedLoot = ci.getReturnValue();
                BlockState blockState = context.getParamOrNull(LootParameters.BLOCK_STATE);
                Optional<TileEntity> tileEntity = Optional.ofNullable(context.getParamOrNull(LootParameters.BLOCK_ENTITY));
                Vector3d posCenter = context.getParamOrNull(LootParameters.ORIGIN);
                BlockPos blockPos = new BlockPos(posCenter);
                CrazyDiamondRestoreTerrain.rememberBrokenBlock(
                        world, blockPos, blockState, tileEntity, world.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) ? generatedLoot : Collections.emptyList());
            }
        }
    }
}
