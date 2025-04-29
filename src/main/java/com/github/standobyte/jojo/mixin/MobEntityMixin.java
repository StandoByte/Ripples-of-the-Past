package com.github.standobyte.jojo.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {

    protected MobEntityMixin(EntityType<? extends LivingEntity> p_i48577_1_, World p_i48577_2_) {
        super(p_i48577_1_, p_i48577_2_);
    }
    
    @Shadow protected abstract void pickUpItem(ItemEntity pItemEntity);
    @Shadow public abstract boolean wantsToPickUp(ItemStack pStack);

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/IProfiler;pop()V"))
    public void looting2(CallbackInfo ci) {
        if (!this.level.isClientSide && this.isAlive() && !this.dead && ForgeEventFactory.getMobGriefingEvent(this.level, this)) {
            List<ItemEntity> markedItems = this.level.getEntitiesOfClass(
                    ItemEntity.class, this.getBoundingBox().inflate(1.0D, 0.0D, 1.0D), itemEntity -> {
                        if (itemEntity.removed) return false;
                        ItemStack item = itemEntity.getItem();
                        if (item.isEmpty()) return false;
                        return TrackerItemStack.getItemTracker(item).filter(TrackerItemStack::isTracked).isPresent();
                    });
            if (!markedItems.isEmpty()) {
                for (ItemEntity itementity : markedItems) {
                    if (this.wantsToPickUp(itementity.getItem())) {
                        this.pickUpItem(itementity);
                    }
                }
            }
        }
    }

}
