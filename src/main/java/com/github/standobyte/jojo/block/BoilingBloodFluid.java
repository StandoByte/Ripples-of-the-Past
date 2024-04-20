package com.github.standobyte.jojo.block;

import java.util.Random;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModFluids;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModParticles;

import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.item.Item;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;

public abstract class BoilingBloodFluid extends LavaFluid { // TODO separate sounds to make subtitles mention "boiling blood" instead of "lava"

    @Override
    public Fluid getFlowing() {
        return ModFluids.FLOWING_BOILING_BLOOD.get();
    }

    @Override
    public Fluid getSource() {
        return ModFluids.BOILING_BLOOD.get();
    }

    @Override
    public Item getBucket() {
        return ModItems.BOILING_BLOOD_BUCKET.get();
    }
    
    @Override
    protected FluidAttributes createAttributes() {
       return FluidAttributes.builder(
               new ResourceLocation(JojoMod.MOD_ID, "block/boiling_blood_still"),
               new ResourceLocation(JojoMod.MOD_ID, "block/boiling_blood_flow"))
               .translationKey("block.jojo.boiling_blood")
               .luminosity(15).density(3000).viscosity(6000).temperature(1300)
               .sound(SoundEvents.BUCKET_FILL_LAVA, SoundEvents.BUCKET_EMPTY_LAVA)
               .build(this);
    }

    @Override
    public void animateTick(World pLevel, BlockPos pPos, FluidState pState, Random pRandom) {
        BlockPos blockpos = pPos.above();
        if (pLevel.getBlockState(blockpos).isAir() && !pLevel.getBlockState(blockpos).isSolidRender(pLevel, blockpos)) {
            if (pRandom.nextInt(100) == 0) {
                double d0 = (double)pPos.getX() + pRandom.nextDouble();
                double d1 = (double)pPos.getY() + 1.0D;
                double d2 = (double)pPos.getZ() + pRandom.nextDouble();
                pLevel.addParticle(ModParticles.BOILING_BLOOD_POP.get(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
                pLevel.playLocalSound(d0, d1, d2, SoundEvents.LAVA_POP, SoundCategory.BLOCKS, 0.2F + pRandom.nextFloat() * 0.2F, 0.9F + pRandom.nextFloat() * 0.15F, false);
            }

            if (pRandom.nextInt(200) == 0) {
                pLevel.playLocalSound((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), SoundEvents.LAVA_AMBIENT, SoundCategory.BLOCKS, 0.2F + pRandom.nextFloat() * 0.2F, 0.9F + pRandom.nextFloat() * 0.15F, false);
            }
        }

    }
//
//    @Override
//    public void randomTick(World pLevel, BlockPos pPos, FluidState pState, Random pRandom) {
//        if (pLevel.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
//            int i = pRandom.nextInt(3);
//            if (i > 0) {
//                BlockPos blockpos = pPos;
//
//                for(int j = 0; j < i; ++j) {
//                    blockpos = blockpos.offset(pRandom.nextInt(3) - 1, 1, pRandom.nextInt(3) - 1);
//                    if (!pLevel.isLoaded(blockpos)) {
//                        return;
//                    }
//
//                    BlockState blockstate = pLevel.getBlockState(blockpos);
//                    if (blockstate.isAir()) {
//                        if (this.hasFlammableNeighbours(pLevel, blockpos)) {
//                            pLevel.setBlockAndUpdate(blockpos, ForgeEventFactory.fireFluidPlaceBlockEvent(pLevel, blockpos, pPos, Blocks.FIRE.defaultBlockState()));
//                            return;
//                        }
//                    } else if (blockstate.getMaterial().blocksMotion()) {
//                        return;
//                    }
//                }
//            } else {
//                for(int k = 0; k < 3; ++k) {
//                    BlockPos blockpos1 = pPos.offset(pRandom.nextInt(3) - 1, 0, pRandom.nextInt(3) - 1);
//                    if (!pLevel.isLoaded(blockpos1)) {
//                        return;
//                    }
//
//                    if (pLevel.isEmptyBlock(blockpos1.above()) && this.isFlammable(pLevel, blockpos1, Direction.UP)) {
//                        pLevel.setBlockAndUpdate(blockpos1.above(), ForgeEventFactory.fireFluidPlaceBlockEvent(pLevel, blockpos1.above(), pPos, Blocks.FIRE.defaultBlockState()));
//                    }
//                }
//            }
//
//        }
//    }
//
//    private boolean hasFlammableNeighbours(IWorldReader pLevel, BlockPos pPos) {
//        for(Direction direction : Direction.values()) {
//            if (this.isFlammable(pLevel, pPos.relative(direction), direction.getOpposite())) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    private boolean isFlammable(IWorldReader world, BlockPos pos, Direction face) {
//        return pos.getY() >= 0 && pos.getY() < 256 && !world.hasChunkAt(pos) ? false : world.getBlockState(pos).isFlammable(world, pos, face);
//    }

    @Override
    public IParticleData getDripParticle() {
        return ParticleTypes.DRIPPING_LAVA; // TODO particle
    }
//
//    @Override
//    protected void beforeDestroyingBlock(IWorld pLevel, BlockPos pPos, BlockState pState) {
//        this.fizz(pLevel, pPos);
//    }
//
//    @Override
//    public int getSlopeFindDistance(IWorldReader pLevel) {
//        return pLevel.dimensionType().ultraWarm() ? 4 : 2;
//    }

    @Override
    public BlockState createLegacyBlock(FluidState pState) {
        return ModBlocks.BOILING_BLOOD.get().defaultBlockState().setValue(FlowingFluidBlock.LEVEL, Integer.valueOf(getLegacyLevel(pState)));
    }

    @Override
    public boolean isSame(Fluid pFluid) {
        return pFluid == ModFluids.BOILING_BLOOD.get() || pFluid == ModFluids.FLOWING_BOILING_BLOOD.get();
    }
//
//    @Override
//    public int getDropOff(IWorldReader pLevel) {
//        return pLevel.dimensionType().ultraWarm() ? 1 : 2;
//    }
//
//    @Override
//    public boolean canBeReplacedWith(FluidState pFluidState, IBlockReader pBlockReader, BlockPos pPos, Fluid pFluid, Direction pDirection) {
//        return pFluidState.getHeight(pBlockReader, pPos) >= 0.44444445F && pFluid.is(FluidTags.WATER);
//    }
//
//    @Override
//    public int getTickDelay(IWorldReader world) {
//       return world.dimensionType().ultraWarm() ? 10 : 30;
//    }
//
//    @Override
//    public int getSpreadDelay(World world, BlockPos pos, FluidState fluidState, FluidState newLiquid) {
//        int i = this.getTickDelay(world);
//        if (!fluidState.isEmpty() && !newLiquid.isEmpty() && !fluidState.getValue(FALLING) && !newLiquid.getValue(FALLING) && newLiquid.getHeight(world, pos) > fluidState.getHeight(world, pos) && world.getRandom().nextInt(4) != 0) {
//            i *= 4;
//        }
//
//        return i;
//    }

    private void fizz(IWorld pLevel, BlockPos pPos) {
        pLevel.levelEvent(1501, pPos, 0);
    }
//    
//    @Override
//    protected boolean canConvertToSource() {
//       return false;
//    }
//
//    @Override
//    protected void spreadTo(IWorld pLevel, BlockPos pPos, BlockState pBlockState, Direction pDirection, FluidState pFluidState) {
//        if (pDirection == Direction.DOWN) {
//            FluidState fluidstate = pLevel.getFluidState(pPos);
//            if (this.is(FluidTags.LAVA) && fluidstate.is(FluidTags.WATER)) {
//                if (pBlockState.getBlock() instanceof FlowingFluidBlock) {
//                    pLevel.setBlock(pPos, ForgeEventFactory.fireFluidPlaceBlockEvent(pLevel, pPos, pPos, Blocks.STONE.defaultBlockState()), 3);
//                }
//
//                this.fizz(pLevel, pPos);
//                return;
//            }
//        }
//
//        super.spreadTo(pLevel, pPos, pBlockState, pDirection, pFluidState);
//    }
//
//    @Override
//    protected boolean isRandomlyTicking() {
//       return true;
//    }
//
//    @Override
//    protected float getExplosionResistance() {
//       return 100.0F;
//    }

    public static class Flowing extends BoilingBloodFluid {
        
        @Override
        protected void createFluidStateDefinition(StateContainer.Builder<Fluid, FluidState> pBuilder) {
            super.createFluidStateDefinition(pBuilder);
            pBuilder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState pState) {
            return pState.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState pState) {
            return false;
        }
    }

    public static class Source extends BoilingBloodFluid {
        
        @Override
        public int getAmount(FluidState pState) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState pState) {
            return true;
        }
    }

}
