package com.github.standobyte.jojo.mixin.itemtracking.equip;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack.KnownItemState;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntityMixin {
    
    protected MobEntityMixin(EntityType<? extends MobEntity> type, World world) {
        super(type, world);
    }
    
    @Shadow public abstract ItemStack getItemBySlot(EquipmentSlotType pSlot);
    
    @Override
    public void onTake(Entity entity, int amount, CallbackInfo ci) {
        if (amount == 1 && entity instanceof ItemEntity) {
            ItemStack item = ((ItemEntity) entity).getItem();
            if (!item.isEmpty()) {
                TrackerItemStack.getItemTracker(item).ifPresent(tracker -> {
                    tracker.setAtEntity(this.getId(), level, KnownItemState.ENTITY_HAS_ITEM);
                    tracker.setItemStillThereCheck(null);
                });
            }
        }
    }
    
    @Inject(method = "setItemSlot", at = @At("TAIL"))
    public void jojoOnMobItemEquip(EquipmentSlotType pSlot, ItemStack pStack, CallbackInfo ci) {
        if (!level.isClientSide()) {
            TrackerItemStack.getItemTracker(pStack).ifPresent(tracker -> {
                tracker.setAtEntity(this.getId(), level, KnownItemState.ENTITY_HAS_ITEM);
                tracker.setItemStillThereCheck(trackerId -> TrackerItemStack.trackerIdCheck(trackerId).test(this.getItemBySlot(pSlot)));
            });
        }
    }
}
