package com.github.standobyte.jojo.entity.damaging.projectile;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.util.JojoModUtil;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;

@OnlyIn(value = Dist.CLIENT, _interface = IRendersAsItem.class)
public class MRFireballEntity extends ModdedProjectileEntity implements IRendersAsItem {
    
    public MRFireballEntity(LivingEntity shooter, World world) {
        super(ModEntityTypes.MR_FIREBALL.get(), shooter, world);
    }

    public MRFireballEntity(EntityType<? extends MRFireballEntity> type, World world) {
        super(type, world);
    }

    @Override
    public boolean standDamage() {
        return true;
    }
    
    @Override
    protected boolean hurtTarget(Entity target, LivingEntity owner) {
        if (super.hurtTarget(target, owner)) {
            hurtTarget(target, getDamageSource(owner).setIsFire(), getBaseDamage() * getDamageFactor());
            int seconds = 3;
            if (target instanceof StandEntity) {
                ((StandEntity) target).setFireFromStand(seconds);
            }
            else {
                target.setSecondsOnFire(seconds);
            }
            return true;
        }
        return false;
    }
    
    @Override
    protected void afterBlockHit(BlockRayTraceResult blockRayTraceResult, boolean blockDestroyed) {
        if (!level.isClientSide) {
            if (ForgeEventFactory.getMobGriefingEvent(level, getEntity())) {
                BlockPos blockPos = blockDestroyed ? blockRayTraceResult.getBlockPos() : 
                    blockRayTraceResult.getBlockPos().relative(blockRayTraceResult.getDirection());
                if (level.isEmptyBlock(blockPos)) {
                    level.setBlockAndUpdate(blockPos, AbstractFireBlock.getState(level, blockPos));
                }
            }
        }
    }

    @Override
    public void tick() {
        if (isInWaterOrRain()) {
            clearFire();
        }
        else {
            super.tick();
        }
    }
    
    @Override
    public void clearFire() {
        super.clearFire();
        if (!level.isClientSide()) {
            JojoModUtil.extinguishFieryStandEntity(this, (ServerWorld) level);
        }
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.FIRE_CHARGE);
    }

    @Override
    public boolean isOnFire() {
        return true;
    }
    
    @Override
    public float getBaseDamage() {
        return 2.0F;
    }
    
    @Override
    protected float getMaxHardnessBreakable() {
        return 5F;
    }
    
    @Override
    protected int ticksLifespan() {
        return 100;
    }
}
