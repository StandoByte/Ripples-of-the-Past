package com.github.standobyte.jojo.block;

import java.util.UUID;

import com.github.standobyte.jojo.capability.world.MrPresidentWorldDataProvider;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentBackTeleporter;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData.ChunkSectionPos;
import com.github.standobyte.jojo.world.dimension.ModDimensions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class MrPresidentGemBlock extends Block {

    public MrPresidentGemBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!player.isShiftKeyDown() && world.dimension() == ModDimensions.MR_PRESIDENT) {
            if (!world.isClientSide()) {
                MrPresidentWorldData rooms = world.getCapability(MrPresidentWorldDataProvider.CAPABILITY).resolve().get();
                UUID turtleId = rooms.getTurtleId(new ChunkSectionPos(pos));
                if (turtleId != null) {
                    MrPresidentBackTeleporter teleporter = MrPresidentBackTeleporter.teleportToTurtle(world.getServer(), turtleId);
                    if (teleporter != null) {
                        player.changeDimension(teleporter.world, teleporter);
                        return ActionResultType.CONSUME;
                    }
                }
            }
        }
        
        return ActionResultType.PASS;
    }
    
}
