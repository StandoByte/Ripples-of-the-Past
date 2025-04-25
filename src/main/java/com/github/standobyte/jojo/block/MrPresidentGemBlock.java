package com.github.standobyte.jojo.block;

import java.util.UUID;

import com.github.standobyte.jojo.capability.world.MrPresidentWorldDataProvider;
import com.github.standobyte.jojo.mrpresident.MrPresidentStandType;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData.ChunkSectionPos;
import com.github.standobyte.jojo.world.dimension.ModDimensions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class MrPresidentGemBlock extends Block {
    /*
     * 1 - up
     * 2 - right
     * 3 - down
     * 4 - left
     * 5 - left up
     * 6 - right up
     * 7 - right down
     * 8 - left down
     */
    public static final IntegerProperty BORDER_VARIANT = IntegerProperty.create("border_variant", 0, 8);
    
    public MrPresidentGemBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BORDER_VARIANT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BORDER_VARIANT);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!player.isShiftKeyDown() && world.dimension() == ModDimensions.MR_PRESIDENT) {
            if (!world.isClientSide()) {
                MrPresidentWorldData rooms = world.getCapability(MrPresidentWorldDataProvider.CAPABILITY).resolve().get();
                UUID turtleId = rooms.getTurtleId(new ChunkSectionPos(pos));
                if (turtleId != null) {
                    MrPresidentStandType.teleportFromRoom(player, turtleId, world.getServer());
                    return ActionResultType.CONSUME;
                }
            }
        }
        
        return ActionResultType.PASS;
    }
    
}
