package com.github.standobyte.jojo.action.stand;

import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class CrazyDiamondBlockCheckpointMake extends StandEntityAction {

    public CrazyDiamondBlockCheckpointMake(Builder builder) {
        super(builder);
    }

    @Override
    public boolean standCanPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        BlockPos pos = task.getTarget().getBlockPos();
        if (pos != null) {
            return standEntity.canBreakBlock(pos, world.getBlockState(pos));
        }
        return false;
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            BlockPos pos = task.getTarget().getBlockPos();
            // FIXME ! (fast travel) handle the "no block breaking" config
            if (pos != null) {
                BlockState blockState = world.getBlockState(pos);
                List<ItemStack> drops = Block.getDrops(blockState, (ServerWorld) world, pos, 
                        blockState.hasTileEntity() ? world.getBlockEntity(pos) : null);
                
                if (standEntity.breakBlock(pos, false)) {
                    drops.forEach(stack -> {
                        boolean dropItem = true;
                        if (stack.getItem() instanceof BlockItem) {
                            CompoundNBT mainNBT = new CompoundNBT();
                            
                            CompoundNBT posNBT = new CompoundNBT();
                            posNBT.putInt("x", pos.getX());
                            posNBT.putInt("y", pos.getY());
                            posNBT.putInt("z", pos.getZ());
                            mainNBT.put("Pos", posNBT);
                            
                            mainNBT.put("BlockState", NBTUtil.writeBlockState(blockState));

                            stack.getOrCreateTag().put("CDCheckpoint", mainNBT);
                            LivingEntity user = standEntity.getUser();
                            dropItem = !(user instanceof PlayerEntity && ((PlayerEntity) user).inventory.add(stack) && stack.isEmpty());
                        }
                        if (dropItem) {
                            Block.popResource(world, pos, stack);
                        }
                    });
                }
            }
        }
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.BLOCK;
    }

    // FIXME ! (fast travel) dimension key
    public static Optional<BlockPos> getBlockPosMoveTo(World world, ItemStack stack) {
        if (stack.hasTag()) {
            CompoundNBT nbt = stack.getTag();
            if (nbt.contains("CDCheckpoint", JojoModUtil.getNbtId(CompoundNBT.class))) {
                CompoundNBT checkpointNbt = nbt.getCompound("CDCheckpoint");
                if (checkpointNbt.contains("Pos", JojoModUtil.getNbtId(CompoundNBT.class))) {
                    CompoundNBT posNBT = checkpointNbt.getCompound("Pos");
                    BlockPos pos = new BlockPos(posNBT.getInt("x"), posNBT.getInt("y"), posNBT.getInt("z"));
                    return Optional.of(pos);
                }
            }
        }
        return Optional.empty();
    }
    
    public static BlockState getBlockState(ItemStack stack, BlockItem item) {
        if (stack.hasTag()) {
            CompoundNBT nbt = stack.getTag();
            if (nbt.contains("CDCheckpoint", JojoModUtil.getNbtId(CompoundNBT.class))) {
                CompoundNBT checkpointNbt = nbt.getCompound("CDCheckpoint");
                if (checkpointNbt.contains("BlockState", JojoModUtil.getNbtId(CompoundNBT.class))) {
                    BlockState blockState = NBTUtil.readBlockState(checkpointNbt.getCompound("BlockState"));
                    if (blockState != Blocks.AIR.defaultBlockState()) {
                        return blockState;
                    }
                }
            }
        }
        return item.getBlock().defaultBlockState();
    }
}
