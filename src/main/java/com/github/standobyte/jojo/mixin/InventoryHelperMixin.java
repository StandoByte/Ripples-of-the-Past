package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.action.stand.GoldExperienceCreateLifeform;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(InventoryHelper.class)
public abstract class InventoryHelperMixin {
    
    @SuppressWarnings("unlikely-arg-type")
    @Inject(method = "dropContents", at = @At("HEAD"), cancellable = true)
    private static void jojoKeepItemsOnTEBreak(World world, BlockPos pos, IInventory teInventory, CallbackInfo ci) {
        if (GoldExperienceCreateLifeform.KEEP_ITEMS.contains(teInventory)) {
            ci.cancel();
        }
    }

}
