package com.github.standobyte.jojo.mixin.itemtracking.equip;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandEntityMixin extends LivingEntity {
    
    protected ArmorStandEntityMixin(EntityType<? extends MobEntity> type, World world) {
        super(type, world);
    }
    
    @Inject(method = "setItemSlot", at = @At("TAIL"))
    public void jojoOnArmorStandItemEquip(EquipmentSlotType pSlot, ItemStack pStack, CallbackInfo ci) {
        if (!level.isClientSide()) {
            TrackerItemStack.getItemTracker(pStack).ifPresent(tracker -> {
                tracker.setAtEntity(this.getId(), level);
                tracker.setItemStillThereCheck(trackerId -> TrackerItemStack.trackerIdCheck(trackerId).test(this.getItemBySlot(pSlot)));
            });
        }
    }
}
