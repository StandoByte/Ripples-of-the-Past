package com.github.standobyte.jojo.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class MeteoricOreBlock extends OreBlock {

    public MeteoricOreBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    protected int xpOnDrop(Random rand) {
        return MathHelper.nextInt(rand, 6, 10);
    }

    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, (new AxisAlignedBB(x, y, z, x, y, z)).inflate(2.0D, 2.0D, 2.0D))) {
            if (entity.getMobType() != CreatureAttribute.UNDEAD && entity.getHealth() < entity.getMaxHealth() && !isImmuneToMeteoriteStrain(entity)) {
                entity.hurt(DamageUtil.STAND_VIRUS_METEORITE, 4.0F);
            }
        }
        world.getBlockTicks().scheduleTick(pos, this, 10);
    }

    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        double d0 = (double)((float)pos.getX() + random.nextFloat() * 4F - 2F);
        double d1 = (double)((float)pos.getY() + random.nextFloat() * 4F - 2F);
        double d2 = (double)((float)pos.getZ() + random.nextFloat() * 4F - 2F);
        world.addParticle(ModParticles.METEORITE_VIRUS.get(), d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }

    @Deprecated
    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
        world.getBlockTicks().scheduleTick(currentPos, this, 10);
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    @Override
    public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        world.getBlockTicks().scheduleTick(pos, this, 10);
    }

    @Override
    public void playerDestroy(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity tileEntity, ItemStack stack) {
        super.playerDestroy(world, player, pos, state, tileEntity, stack);
        if (player.getHealth() < player.getMaxHealth() && !isImmuneToMeteoriteStrain(player)) {
            player.hurt(DamageUtil.STAND_VIRUS_METEORITE, 10.0F);
        }
    }
    
    public static boolean isImmuneToMeteoriteStrain(LivingEntity entity) {
        return GeneralUtil.orElseFalse(IStandPower.getStandPowerOptional(entity), power -> power.hadAnyStand() || power.hasPower());
    }
}
