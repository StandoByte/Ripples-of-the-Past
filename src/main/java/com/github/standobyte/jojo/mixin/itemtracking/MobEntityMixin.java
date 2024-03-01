package com.github.standobyte.jojo.mixin.itemtracking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends Entity {
    
    protected MobEntityMixin(EntityType<? extends MobEntity> type, World world) {
        super(type, world);
    }
    
    @Inject(method = "setItemSlot", at = @At("TAIL"))
    public void jojoOnMobItemEquip(EquipmentSlotType pSlot, ItemStack pStack, CallbackInfo ci) {
        TrackerItemStack.updateItemAtEntity(level, pStack, this.getId());
    }
}
