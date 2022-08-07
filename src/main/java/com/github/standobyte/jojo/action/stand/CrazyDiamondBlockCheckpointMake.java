package com.github.standobyte.jojo.action.stand;

import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
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
            // FIXME !! (fast travel) handle the "no block breaking" config
            if (pos != null) {
                BlockState blockState = world.getBlockState(pos);
                List<ItemStack> drops = Block.getDrops(blockState, (ServerWorld) world, pos, 
                        blockState.hasTileEntity() ? world.getBlockEntity(pos) : null);
                if (standEntity.breakBlock(pos, false)) {
                    drops.forEach(stack -> {
                        boolean dropItem = true;
                        if (stack.getItem() instanceof BlockItem) {
                            CompoundNBT posNBT = new CompoundNBT();
                            posNBT.putInt("x", pos.getX());
                            posNBT.putInt("y", pos.getY());
                            posNBT.putInt("z", pos.getZ());
                            stack.getOrCreateTag().put("CDCheckpoint", posNBT);
                            ITextComponent name = stack.getHoverName();
                            ITextComponent coord = new StringTextComponent(String.format("(%s, %s, %s)", 
                                    pos.getX(), pos.getY(), pos.getZ())).withStyle(TextFormatting.RED);
                            if (name instanceof TextComponent) {
                                ((TextComponent) name).append(coord);
                            }
                            else {
                                stack.setHoverName(coord);
                            }
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

    // FIXME ! (fast travel) marker
    // FIXME ! (fast travel) dimension key
    public static Optional<BlockPos> getBlockPosMoveTo(World world, ItemStack stack) {
        CompoundNBT nbt = stack.getOrCreateTag();
        if (nbt.contains("CDCheckpoint", JojoModUtil.getNbtId(CompoundNBT.class))) {
            CompoundNBT posNBT = nbt.getCompound("CDCheckpoint");
            BlockPos pos = new BlockPos(posNBT.getInt("x"), posNBT.getInt("y"), posNBT.getInt("z"));
            return Optional.of(pos);
        }
        return Optional.empty();
    }
}
