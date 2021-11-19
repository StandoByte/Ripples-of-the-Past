package com.github.standobyte.jojo.entity.itemprojectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public abstract class ItemNbtProjectileEntity extends ItemProjectileEntity {
    protected ItemStack thrownStack = ItemStack.EMPTY;

    public ItemNbtProjectileEntity(EntityType<? extends ItemNbtProjectileEntity> type, World world, LivingEntity thrower, ItemStack thrownStack) {
        super(type, thrower, world);
        this.thrownStack = thrownStack.copy();
    }

    protected ItemNbtProjectileEntity(EntityType<? extends ItemNbtProjectileEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected ItemStack getPickupItem() {
        return thrownStack.copy();
    }

    @Override
    protected boolean isRemovedOnEntityHit() {
        return false;
    }

    @Override
    protected void onHit(RayTraceResult rayTraceResult) {
        Entity shooter = getOwner();
        if (shooter instanceof LivingEntity) {
            thrownStack.hurtAndBreak(1, (LivingEntity) shooter, entity -> remove());
        }
        super.onHit(rayTraceResult);
    }

    @Override
    public void tickDespawn() {
        if (this.pickup != AbstractArrowEntity.PickupStatus.ALLOWED) {
            super.tickDespawn();
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Item", 10)) {
            thrownStack = ItemStack.of(compound.getCompound("Item"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.put("Item", thrownStack.save(new CompoundNBT()));
    }
}
