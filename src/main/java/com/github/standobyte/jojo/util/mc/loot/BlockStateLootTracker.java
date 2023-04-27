package com.github.standobyte.jojo.util.mc.loot;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.action.stand.CrazyDiamondRestoreTerrain;
import com.google.gson.JsonObject;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

public class BlockStateLootTracker extends LootModifier {

    protected BlockStateLootTracker(ILootCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        if (!LootParameterSets.BLOCK.getRequired().stream().anyMatch(param -> !context.hasParam(param))) {
            World world = context.getLevel();
            if (world != null) {
                BlockState blockState = context.getParamOrNull(LootParameters.BLOCK_STATE);
                Optional<TileEntity> tileEntity = Optional.ofNullable(context.getParamOrNull(LootParameters.BLOCK_ENTITY));
                Vector3d posCenter = context.getParamOrNull(LootParameters.ORIGIN);
                BlockPos blockPos = new BlockPos(posCenter);
                CrazyDiamondRestoreTerrain.rememberBrokenBlock(
                        world, blockPos, blockState, tileEntity, world.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) ? generatedLoot : Collections.emptyList());
            }
        }
        return generatedLoot;
    }

    
    
    public static class Serializer extends GlobalLootModifierSerializer<BlockStateLootTracker> {

        @Override
        public BlockStateLootTracker read(ResourceLocation location, JsonObject object, ILootCondition[] conditions) {
            return new BlockStateLootTracker(conditions);
        }

        @Override
        public JsonObject write(BlockStateLootTracker instance) {
            JsonObject json = makeConditions(instance.conditions);
            return json;
        }
    }

}
