package com.github.standobyte.jojo.mixin.itemtracking.inventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.ContainerMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

@Mixin(ContainerMinecartEntity.class)
public abstract class ContainerMinecartEntityMixin extends AbstractMinecartEntity {
    
    protected ContainerMinecartEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }
    
    @Shadow
    private NonNullList<ItemStack> itemStacks;
    
    @Inject(method = "setItem", at = @At("TAIL"))
    public void jojoOnItemSetToSlot(int slot, ItemStack item, CallbackInfo ci) {
        TrackerItemStack.trackedInEntityInv(item, itemStacks.stream(), 
                level, getId());
    }
}
