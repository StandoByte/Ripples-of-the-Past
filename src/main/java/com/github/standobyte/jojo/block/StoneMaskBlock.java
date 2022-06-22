package com.github.standobyte.jojo.block;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.init.ModTileEntities;
import com.github.standobyte.jojo.tileentity.StoneMaskTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class StoneMaskBlock extends HorizontalFaceBlock { // TODO allow harvesting it with silk touch tool (how do loot tables interact with tile entities tho?)
    public static final DirectionProperty HORIZONTAL_FACING = HorizontalBlock.FACING;
    public static final BooleanProperty BLOOD_ACTIVATION = BooleanProperty.create("blood_activation");
    protected static final VoxelShape WALL_NORTH_SHAPE = Block.box(4.0D, 4.0D, 15.0D, 12.0D, 12.0D, 16.0D);
    protected static final VoxelShape WALL_SOUTH_SHAPE = Block.box(4.0D, 4.0D, 0.0D, 12.0D, 12.0D, 1.0D);
    protected static final VoxelShape WALL_WEST_SHAPE = Block.box(15.0D, 4.0D, 4.0D, 16.0D, 12.0D, 12.0D);
    protected static final VoxelShape WALL_EAST_SHAPE = Block.box(0.0D, 4.0D, 4.0D, 1.0D, 12.0D, 12.0D);
    protected static final VoxelShape FLOOR_SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 1.0D, 12.0D);
    protected static final VoxelShape FLOOR_ACTIVATED_SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 6.0D, 12.0D);
    protected static final VoxelShape CEILING_SHAPE = Block.box(4.0D, 15.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public StoneMaskBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BLOOD_ACTIVATION, false));
    }
    
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch(state.getValue(FACE)) {
        case FLOOR:
            return state.getValue(BLOOD_ACTIVATION) ? FLOOR_ACTIVATED_SHAPE : FLOOR_SHAPE;
        case WALL:
            switch(state.getValue(HORIZONTAL_FACING)) {
            case EAST:
                return WALL_EAST_SHAPE;
            case WEST:
                return WALL_WEST_SHAPE;
            case SOUTH:
                return WALL_SOUTH_SHAPE;
            case NORTH:
            default:
                return WALL_NORTH_SHAPE;
            }
        case CEILING:
        default:
            return CEILING_SHAPE;
        }
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isClientSide()) {
            player.addItem(getItemFromBlock(world, pos, state));
            world.removeBlock(pos, false);
        }
        return ActionResultType.SUCCESS;
    }
    
    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
        BlockState blockState = super.updateShape(state, facing, facingState, world, currentPos, facingPos);
        if (blockState == Blocks.AIR.defaultBlockState() && !world.isClientSide() && world instanceof World) {
            Block.popResource((World) world, currentPos, StoneMaskBlock.getItemFromBlock(world, currentPos, blockState));
        }
        return blockState;
    }
    
    public static ItemStack getItemFromBlock(IBlockReader world, BlockPos pos, BlockState state) {
        ItemStack stack = ItemStack.EMPTY;
        TileEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof StoneMaskTileEntity) {
            stack = ((StoneMaskTileEntity) tileEntity).getStack();
        }
        return stack;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACE, HORIZONTAL_FACING, BLOOD_ACTIVATION);
    }
    
    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        TileEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof StoneMaskTileEntity) {
            ((StoneMaskTileEntity) tileEntity).setStack(stack.copy());
        }
    }
    
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
    
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModTileEntities.STONE_MASK.get().create();
    }

    @Override
    public void playerDestroy(World world, PlayerEntity player, BlockPos blockPos, BlockState blockState, @Nullable TileEntity tileEntity, ItemStack itemUsed) {
        super.playerDestroy(world, player, blockPos, blockState, tileEntity, itemUsed);
        if (!world.isClientSide && tileEntity instanceof StoneMaskTileEntity) {
            StoneMaskTileEntity stoneMask = (StoneMaskTileEntity) tileEntity;

            ModCriteriaTriggers.STONE_MASK_DESTROYED.get().trigger((ServerPlayerEntity) player, 
                    blockState.getBlock(), itemUsed, stoneMask.getStack());
        }
    }
}
