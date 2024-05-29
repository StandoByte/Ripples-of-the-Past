package com.github.standobyte.jojo.action.stand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandStatFormulas;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Dimension;
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
            if (pos != null) {
                if (JojoModUtil.breakingBlocksEnabled(world)) {
                    BlockState blockState = world.getBlockState(pos);
                    List<ItemStack> drops = Block.getDrops(blockState, (ServerWorld) world, pos, 
                            blockState.hasTileEntity() ? world.getBlockEntity(pos) : null);
                    ItemStack item = drops.isEmpty() ? ItemStack.EMPTY : drops.get(0);
                    if (standEntity.breakBlockWithExternalDrops(pos, blockState, 
                            item.isEmpty() ? null : Util.make(new ArrayList<>(), list -> list.add(item)))) {
                        makeAnchor(standEntity, item, world, pos, blockState);
                    }
                    else {
                        standEntity.playSound(ModSounds.CRAZY_DIAMOND_PUNCH_HEAVY.get(), 1.0F, 1.0F);
                    }
                }
                else {
                    makeAnchor(standEntity, new ItemStack(ModItems.CRAZY_DIAMOND_NON_BLOCK_ANCHOR.get()), world, pos, null);
                }
            }
        }
    }
    
    private void makeAnchor(StandEntity standEntity, ItemStack drop, World world, BlockPos blockPos, BlockState blockState) {
        standEntity.playSound(ModSounds.CRAZY_DIAMOND_PUNCH_HEAVY.get(), 1.0F, 1.0F);
        if (!drop.isEmpty()) {
            boolean dropItem = true;
            if (blockState == null || drop.getItem() instanceof BlockItem) {
                fillAnchorNbt(drop, world, blockPos, blockState);
                LivingEntity user = standEntity.getUser();
                dropItem = !(user instanceof PlayerEntity && ((PlayerEntity) user).inventory.add(drop) && drop.isEmpty());
            }
            if (dropItem) {
                Block.popResource(world, blockPos, drop);
            }
        }
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.BLOCK;
    }
    
    private static void fillAnchorNbt(ItemStack stack, World world, BlockPos pos, @Nullable BlockState blockState) {
        CompoundNBT mainNBT = new CompoundNBT();
        
        CompoundNBT posNBT = new CompoundNBT();
        posNBT.putInt("x", pos.getX());
        posNBT.putInt("y", pos.getY());
        posNBT.putInt("z", pos.getZ());
        mainNBT.put("Pos", posNBT);
        
        if (blockState != null) {
            mainNBT.put("BlockState", NBTUtil.writeBlockState(blockState));
        }
        
        mainNBT.putString("Dimension", world.dimension().location().toString());

        stack.getOrCreateTag().put("CDCheckpoint", mainNBT);
    }

    public static Optional<BlockPos> getBlockPosMoveTo(World world, ItemStack stack) {
        if (!stack.isEmpty() && stack.hasTag()) {
            CompoundNBT nbt = stack.getTag();
            if (nbt.contains("CDCheckpoint", MCUtil.getNbtId(CompoundNBT.class))) {
                CompoundNBT checkpointNbt = nbt.getCompound("CDCheckpoint");
                
                if (checkpointNbt.contains("Dimension", MCUtil.getNbtId(StringNBT.class))) {
                    String dimensionKey = checkpointNbt.getString("Dimension");
                    if (!dimensionKey.equals(world.dimension().location().toString())) {
                        return Optional.empty();
                    }
                    
                    if (checkpointNbt.contains("Pos", MCUtil.getNbtId(CompoundNBT.class))) {
                        CompoundNBT posNBT = checkpointNbt.getCompound("Pos");
                        BlockPos pos = new BlockPos(posNBT.getInt("x"), posNBT.getInt("y"), posNBT.getInt("z"));
                        return Optional.of(pos);
                    }
                }
                
                else if (!world.isClientSide()) {
                    checkpointNbt.putString("Dimension", Dimension.OVERWORLD.location().toString());
                }
            }
        }
        return Optional.empty();
    }
    
    public static BlockState getBlockState(ItemStack stack, BlockItem item) {
        if (stack.hasTag()) {
            CompoundNBT nbt = stack.getTag();
            if (nbt.contains("CDCheckpoint", MCUtil.getNbtId(CompoundNBT.class))) {
                CompoundNBT checkpointNbt = nbt.getCompound("CDCheckpoint");
                if (checkpointNbt.contains("BlockState", MCUtil.getNbtId(CompoundNBT.class))) {
                    BlockState blockState = NBTUtil.readBlockState(checkpointNbt.getCompound("BlockState"));
                    if (blockState != Blocks.AIR.defaultBlockState()) {
                        return blockState;
                    }
                }
            }
        }
        return item.getBlock().defaultBlockState();
    }

    @Override
    public int getStandRecoveryTicks(IStandPower standPower, StandEntity standEntity) {
        return StandStatFormulas.getHeavyAttackRecovery(standEntity.getAttackSpeed(), 0);
    }
    
    @Override
    protected boolean isFreeRecovery(IStandPower standPower, StandEntity standEntity) {
        return true;
    }
}
