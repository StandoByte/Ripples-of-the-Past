package com.github.standobyte.jojo.entity.stand.stands;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MagiciansRedEntity extends StandEntity {
    
    public MagiciansRedEntity(StandEntityType<MagiciansRedEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    public boolean attackEntity(Supplier<Boolean> doAttack, StandEntityPunch punch, StandEntityTask task) {
        return DamageUtil.dealDamageAndSetOnFire(punch.target, 
                entity -> super.attackEntity(doAttack, punch, task), 10, true);
    }
    
    @Override
    public void playStandSummonSound() {
        if (!isArmsOnlyMode()) {
            super.playStandSummonSound();
        }
    }
    
    @Deprecated
    public static void removeFireUnderPlayer(LivingEntity user, IStandPower power) {
        World world = user.level;
        if (!world.isClientSide() && user.isAlive()
                && power.isActive() && power.getStandManifestation() instanceof MagiciansRedEntity) {
            AxisAlignedBB userHitbox = user.getBoundingBox();
            BlockPos pos1 = new BlockPos(userHitbox.minX + 0.001D, userHitbox.minY + 0.001D, userHitbox.minZ + 0.001D);
            BlockPos pos2 = new BlockPos(userHitbox.maxX - 0.001D, userHitbox.maxY - 0.001D, userHitbox.maxZ - 0.001D);
            BlockPos.Mutable blockPos = new BlockPos.Mutable();
            if (world.hasChunksAt(pos1, pos2)) {
                for(int x = pos1.getX(); x <= pos2.getX(); ++x) {
                    for(int y = pos1.getY(); y <= pos2.getY(); ++y) {
                        for(int z = pos1.getZ(); z <= pos2.getZ(); ++z) {
                            blockPos.set(x, y, z);
                            BlockState blockState = world.getBlockState(blockPos);
                            if (!blockState.isAir(world, blockPos) && blockState.getBlock() instanceof AbstractFireBlock) {
                                world.destroyBlock(blockPos, false);
                            }
                        }
                    }
                }
            }
        }
    }
}
