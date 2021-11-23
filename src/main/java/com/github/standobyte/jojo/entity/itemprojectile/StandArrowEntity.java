package com.github.standobyte.jojo.entity.itemprojectile;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.item.StandArrowItem;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;

public class StandArrowEntity extends ItemNbtProjectileEntity {
    
    public StandArrowEntity(World world, LivingEntity thrower, ItemStack thrownStack) {
        super(ModEntityTypes.STAND_ARROW.get(), world, thrower, thrownStack);
    }
    
    public StandArrowEntity(EntityType<? extends ItemNbtProjectileEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected boolean hurtTarget(Entity target, Entity thrower) {
        if (super.hurtTarget(target, thrower)) {
            StandArrowItem.onPiercedByArrow(target, getPickupItem(), level);
            return true;
        }
        return false;
    }

    @Override
    protected boolean throwerCanCatch() {
        return false;
    }
    
    @Override
    protected SoundEvent getActualHitGroundSound(BlockState blockState, BlockPos blockPos) {
        return getDefaultHitGroundSoundEvent();
    }

    @Override
    protected void onHit(RayTraceResult rayTraceResult) {
        super.onHit(rayTraceResult);
        if (rayTraceResult.getType() == Type.BLOCK) {
            shakeTime = 7;
        }
    }
}
