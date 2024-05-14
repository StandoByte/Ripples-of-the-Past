package com.github.standobyte.jojo.client.render.item.generic;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class ISTERItemCaptureEntity extends ItemOverrideList {
    
    public ISTERItemCaptureEntity() {
        super();
    }
    
    @Override
    public IBakedModel resolve(IBakedModel model, ItemStack item, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
        ItemStackTileEntityRenderer ister = item.getItem().getItemStackTileEntityRenderer();
        if (ister instanceof ISTERWithEntity) {
            ((ISTERWithEntity) ister).setEntity(entity);
        }
        return model;
    }
}
