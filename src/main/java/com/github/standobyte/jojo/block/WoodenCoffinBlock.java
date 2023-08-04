package com.github.standobyte.jojo.block;

import static net.minecraft.block.BedBlock.OCCUPIED;
import static net.minecraft.block.BedBlock.PART;

import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.GameplayEventHandler;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntity.SleepResult;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BedPart;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class WoodenCoffinBlock extends HorizontalBlock {
    public static final BooleanProperty CLOSED = BooleanProperty.create("coffin_lid_closed");
    private final DyeColor color;

    public WoodenCoffinBlock(DyeColor color, AbstractBlock.Properties properties) {
        super(properties);
        this.color = color;
        this.registerDefaultState(stateDefinition.any().setValue(PART, BedPart.FOOT).setValue(OCCUPIED, false).setValue(CLOSED, false));
    }

    @Override
    public boolean isBed(BlockState state, IBlockReader world, BlockPos pos, @Nullable Entity player) {
        return true;
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos blockPos, 
            PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
        if (world.isClientSide) {
            if (!BedBlock.canSetSpawn(world)) {
                Random random = world.random;
                int particles = ClientUtil.decreasedParticlesSetting() ? 256 : 2560;
                for (int i = 0; i < particles; i++) {
                    ClientUtil.getClientWorld().addParticle(ModParticles.BLOOD.get(), 
                            blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D, 
                            (random.nextDouble() - 0.5) * 1.5, 
                            (random.nextDouble() - 0.5) * 1.5, 
                            (random.nextDouble() - 0.5) * 1.5);
                }
            }
            return ActionResultType.CONSUME;
        } else {
            if (blockState.getValue(PART) != BedPart.HEAD) {
                blockPos = blockPos.relative(blockState.getValue(FACING));
                blockState = world.getBlockState(blockPos);
                if (!blockState.is(this)) {
                    return ActionResultType.CONSUME;
                }
            }
            if (!BedBlock.canSetSpawn(world)) {
                world.removeBlock(blockPos, false);
                BlockPos neighborPos = blockPos.relative(blockState.getValue(FACING).getOpposite());
                if (world.getBlockState(neighborPos).is(this)) {
                    world.removeBlock(neighborPos, false);
                }

                world.getEntitiesOfClass(LivingEntity.class, new AxisAlignedBB(blockPos).inflate(6), 
                        EntityPredicates.ENTITY_STILL_ALIVE.and(EntityPredicates.NO_SPECTATORS))
                .forEach(entity -> {
                    entity.addEffect(new EffectInstance(Effects.BLINDNESS, 100));
                    entity.clearFire();
                });
                world.explode(null, DamageSource.badRespawnPointExplosion(), null, 
                        blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D, 5.0F, false, Explosion.Mode.DESTROY);
                GameplayEventHandler.splashBlood(world, Vector3d.atCenterOf(blockPos), 16, 10, Optional.empty());
                
                return ActionResultType.SUCCESS;
            } else {
                boolean occupied = blockState.getValue(OCCUPIED);
                if (player.isShiftKeyDown() || occupied) {
                    world.setBlock(blockPos, blockState.setValue(CLOSED, !blockState.getValue(CLOSED)), 3);
                    if (occupied) {
                        player.displayClientMessage(new TranslationTextComponent("block.minecraft.bed.occupied"), true);
                    }
                    return ActionResultType.SUCCESS;
                } else {
                    BlockPos coffinPos = blockPos;
                    player.startSleepInBed(blockPos)
                    .ifLeft(failed -> {
                        if (failed != null) {
                            if (!world.isClientSide() && failed == PlayerEntity.SleepResult.NOT_SAFE) {
                                forseSleep((ServerPlayerEntity) player, coffinPos);
                            }
                            else {
                                player.displayClientMessage(failed.getMessage(), true);
                            }
                        }
                    });
                    return ActionResultType.SUCCESS;
                }
            }
        }
    }
    
    private void forseSleep(ServerPlayerEntity player, BlockPos blockPos) {
        player.startSleeping(blockPos);
        CommonReflection.setSleepCounter(player, 0);
        player.awardStat(Stats.SLEEP_IN_BED);
        CriteriaTriggers.SLEPT_IN_BED.trigger(player);
        ((ServerWorld) player.level).updateSleepingPlayerList();
    }
    
    @Override
    public void setBedOccupied(BlockState state, World world, BlockPos pos, LivingEntity sleeper, boolean occupied) {
        super.setBedOccupied(state, world, pos, sleeper, occupied);
        world.setBlock(pos, state.setValue(OCCUPIED, occupied).setValue(CLOSED, occupied), 3);
    }
    
    @SubscribeEvent
    public static void setRespawnLocation(PlayerSetSpawnEvent event) {
        if (isBlockCoffin(event.getEntityLiving().level, Optional.ofNullable(event.getNewSpawn()))
                && !isEntityVampire(event.getPlayer())) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void preventBedCoffinAlternating(PlayerSleepInBedEvent event) {
        PlayerEntity player = event.getPlayer();
        boolean isCoffin = isBlockCoffin(player.level, event.getOptionalPos());
        boolean canSleep = player.getCapability(PlayerUtilCapProvider.CAPABILITY)
                .map(cap -> cap.canGoToSleep(isCoffin)).orElse(true);
        if (!canSleep) {
            event.setResult(SleepResult.OTHER_PROBLEM);
            player.displayClientMessage(new TranslationTextComponent("block.jojo.wooden_coffin.full_time_skip_fix"), true);
        }
    }
    
    @SubscribeEvent
    public static void canSleepAtTime(SleepingTimeCheckEvent event) {
        if (isBlockCoffin(event.getEntityLiving().level, event.getSleepingLocation())) {
            event.setResult(Result.ALLOW);
        }
    }
    
    @SubscribeEvent
    public static void setCoffinTime(SleepFinishedTimeEvent event) {
        if (event.getWorld() instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) event.getWorld();
            int playersCount = world.players().size();
            if (world.players().stream()
                    .filter(player -> player.isSleeping() && isBlockCoffin(player.level, Optional.of(player.blockPosition())))
                    .count() >= (playersCount + 1) / 2) {
                long time = world.getDayTime();
                long timeAdded = (24000L - time % 24000L + 12600L) % 24000L;
                event.setTimeAddition(time + timeAdded);
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void skippedToNight(SleepFinishedTimeEvent event) {
        int time = (int) (event.getNewTime() % 24000L);
        if (event.getWorld() instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) event.getWorld();
            world.players().stream()
            .filter(player -> player.isSleeping())
            .forEach(player -> {
                boolean isCoffin = isBlockCoffin(player.level, Optional.of(player.blockPosition()));
                
                if (isCoffin) {
                    if (player.hasEffect(ModStatusEffects.VAMPIRE_SUN_BURN.get())) {
                        player.removeEffect(ModStatusEffects.VAMPIRE_SUN_BURN.get());
                        player.removeEffect(Effects.WEAKNESS);
                    }
                    if (time >= 12600 && time < 23500) {
                        ModCriteriaTriggers.SLEPT_IN_COFFIN.get().trigger(player);
                    }
                }

                long oldTime = event.getWorld().dayTime();
                int timeAdded = (int) Math.max(event.getNewTime() - oldTime, 0);
                player.getCapability(PlayerUtilCapProvider.CAPABILITY)
                        .ifPresent(cap -> cap.onSleep(isCoffin, timeAdded));
            });
        }
    }
    
    @SubscribeEvent
    public static void respawnInsideCoffin(PlayerRespawnEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player.level.isDay() && isEntityVampire(player) && player instanceof ServerPlayerEntity) {
            BlockPos pos = ((ServerPlayerEntity) player).getRespawnPosition();
            if (pos != null) {
                BlockState blockState = player.level.getBlockState(pos);
                if (blockState.getBlock() instanceof WoodenCoffinBlock) {
                    // FIXME (vampire\coffin) spawn inside the coffin?
                }
            }
        }
    }
    
    private static boolean isBlockCoffin(World world, Optional<BlockPos> blockPos) {
        return blockPos.map(pos -> world.getBlockState(pos).getBlock() instanceof WoodenCoffinBlock).orElse(false);
    }
    
    private static boolean isEntityVampire(LivingEntity entity) {
        return INonStandPower.getNonStandPowerOptional(entity)
                .map(power -> power.getType() == ModPowers.VAMPIRISM.get()).orElse(false);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
        if (direction == getNeighbourDirection(state.getValue(PART), state.getValue(FACING))) {
            return neighborState.is(this) && neighborState.getValue(PART) != state.getValue(PART) ? 
                    state.setValue(OCCUPIED, neighborState.getValue(OCCUPIED)).setValue(CLOSED, neighborState.getValue(CLOSED))
                    : Blocks.AIR.defaultBlockState();
        } else {
            return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
        }
    }

    private static Direction getNeighbourDirection(BedPart p_208070_0_, Direction p_208070_1_) {
        return p_208070_0_ == BedPart.FOOT ? p_208070_1_ : p_208070_1_.getOpposite();
    }

    @Override
    public void playerWillDestroy(World p_176208_1_, BlockPos p_176208_2_, BlockState p_176208_3_, PlayerEntity p_176208_4_) {
        if (!p_176208_1_.isClientSide && p_176208_4_.isCreative()) {
            BedPart bedpart = p_176208_3_.getValue(PART);
            if (bedpart == BedPart.FOOT) {
                BlockPos blockpos = p_176208_2_.relative(getNeighbourDirection(bedpart, p_176208_3_.getValue(FACING)));
                BlockState blockstate = p_176208_1_.getBlockState(blockpos);
                if (blockstate.getBlock() == this && blockstate.getValue(PART) == BedPart.HEAD) {
                    p_176208_1_.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 35);
                    p_176208_1_.levelEvent(p_176208_4_, 2001, blockpos, Block.getId(blockstate));
                }
            }
        }

        super.playerWillDestroy(p_176208_1_, p_176208_2_, p_176208_3_, p_176208_4_);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext p_196258_1_) {
        Direction direction = p_196258_1_.getHorizontalDirection();
        BlockPos blockpos = p_196258_1_.getClickedPos();
        BlockPos blockpos1 = blockpos.relative(direction);
        return p_196258_1_.getLevel().getBlockState(blockpos1).canBeReplaced(p_196258_1_) ? this.defaultBlockState().setValue(FACING, direction) : null;
    }

    private static final VoxelShape WALL_1 = Block.box(0.0D, 0.0D, 0.0D, 2.0D, 12.0D, 16.0D);
    private static final VoxelShape WALL_2 = Block.box(14.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    private static final VoxelShape WALL_3 = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 2.0D);
    private static final VoxelShape WALL_4 = Block.box(0.0D, 0.0D, 14.0D, 16.0D, 12.0D, 16.0D);
    private static final VoxelShape BOTTOM = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    
    protected static final VoxelShape SHAPE_CLOSED = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);
    protected static final VoxelShape SHAPE_OPEN_W = VoxelShapes.or(BOTTOM, WALL_2, WALL_3, WALL_4);
    protected static final VoxelShape SHAPE_OPEN_E = VoxelShapes.or(BOTTOM, WALL_1, WALL_3, WALL_4);
    protected static final VoxelShape SHAPE_OPEN_N = VoxelShapes.or(BOTTOM, WALL_1, WALL_2, WALL_4);
    protected static final VoxelShape SHAPE_OPEN_S = VoxelShapes.or(BOTTOM, WALL_1, WALL_2, WALL_3);
    @Deprecated
    @Override
    public VoxelShape getShape(BlockState blockState, IBlockReader world, BlockPos pos, ISelectionContext p_220053_4_) {
        if (blockState.getValue(CLOSED)) {
            return SHAPE_CLOSED;
        }
        Direction direction = blockState.getValue(FACING);
        BedPart part = blockState.getValue(PART);
        if (part == BedPart.HEAD) direction = direction.getOpposite();
        switch (direction) {
        case NORTH:
            return SHAPE_OPEN_N;
        case EAST:
            return SHAPE_OPEN_E;
        case SOUTH:
            return SHAPE_OPEN_S;
        case WEST:
            return SHAPE_OPEN_W;
        default:
            break;
        }
        return super.getShape(blockState, world, pos, p_220053_4_);
    }

    public static Direction getConnectedDirection(BlockState blockState) {
        Direction direction = blockState.getValue(FACING);
        return blockState.getValue(PART) == BedPart.HEAD ? direction.getOpposite() : direction;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState blockState) {
        return PushReaction.DESTROY;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> p_206840_1_) {
        p_206840_1_.add(FACING, PART, OCCUPIED, CLOSED);
    }

    @Override
    public void setPlacedBy(World world, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity entity, ItemStack item) {
        super.setPlacedBy(world, blockPos, blockState, entity, item);
        if (!world.isClientSide) {
            BlockPos blockpos = blockPos.relative(blockState.getValue(FACING));
            world.setBlock(blockpos, blockState.setValue(PART, BedPart.HEAD), 3);
            world.blockUpdated(blockPos, Blocks.AIR);
            blockState.updateNeighbourShapes(world, blockPos, 3);
        }

    }

    public DyeColor getColor() {
        return this.color;
    }

    @Override
    public long getSeed(BlockState p_209900_1_, BlockPos p_209900_2_) {
        BlockPos blockpos = p_209900_2_.relative(p_209900_1_.getValue(FACING), p_209900_1_.getValue(PART) == BedPart.HEAD ? 0 : 1);
        return MathHelper.getSeed(blockpos.getX(), p_209900_2_.getY(), blockpos.getZ());
    }

    @Override
    public boolean isPathfindable(BlockState p_196266_1_, IBlockReader p_196266_2_, BlockPos p_196266_3_, PathType p_196266_4_) {
        return false;
    }
    
    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return 5;
    }
    
    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return 5;
    }

}
