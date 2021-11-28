package com.github.standobyte.jojo.entity.itemprojectile;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.item.StandArrowItem;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

public class StandArrowEntity extends AbstractArrowEntity {
    private ItemStack arrowItem = new ItemStack(ModItems.STAND_ARROW.get());
    
    public StandArrowEntity(World world, LivingEntity thrower, ItemStack arrowItem) {
        super(ModEntityTypes.STAND_ARROW.get(), thrower, world);
        this.arrowItem = arrowItem;
    }
    
    public StandArrowEntity(EntityType<? extends AbstractArrowEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        StandArrowItem.onPiercedByArrow(target, arrowItem, level);
    }

    @Override
    protected ItemStack getPickupItem() {
        return arrowItem.copy();
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Arrow", 10)) {
            arrowItem = ItemStack.of(compound.getCompound("Trident"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT arrow) {
        super.addAdditionalSaveData(arrow);
        arrow.put("Arrow", arrowItem.save(new CompoundNBT()));
    }
}
