package com.github.standobyte.jojo.mixin;

import java.util.Collections;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.action.stand.GoldExperienceCreateLifeform;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

@Mixin(AbstractFurnaceTileEntity.class)
public abstract class AbstractFurnaceTEMixin extends TileEntity {
    
    public AbstractFurnaceTEMixin(TileEntityType<?> type) {
        super(type);
    }
    
    @Inject(method = "getRecipesToAwardAndPopExperience", at = @At("HEAD"), cancellable = true)
    public void jojoKeepXpOnTEBreak(World world, Vector3d pos, CallbackInfoReturnable<List<IRecipe<?>>> ci) {
        if (GoldExperienceCreateLifeform.KEEP_ITEMS.contains(this)) {
            ci.setReturnValue(Collections.emptyList());
        }
    }
}
