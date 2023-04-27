package com.github.standobyte.jojo.client.render.entity.renderer.damaging.stretching;

import com.github.standobyte.jojo.client.render.entity.model.ownerbound.ZoomPunchModel;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.ZoomPunchEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;

public class ZoomPunchRenderer extends StretchingEntityRenderer<ZoomPunchEntity, ZoomPunchModel> {

    public ZoomPunchRenderer(EntityRendererManager renderManager) {
        super(renderManager, new ZoomPunchModel(0.0F), DefaultPlayerSkin.getDefaultSkin());
    }
    
    protected float getModelLength() {
        return 12F;
    }
    
    protected float getModelRotationPointOffset() {
        return 2F;
    } 

    @Override
    public ResourceLocation getTextureLocation(ZoomPunchEntity entity) {
        Entity owner = entity.getOwner();
        return owner != null ? entityRenderDispatcher.getRenderer(owner).getTextureLocation(owner) : 
            Minecraft.getInstance().player.getSkinTextureLocation();
    }

    @Override
    public void render(ZoomPunchEntity entity, float yRotation, float partialTick, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        LivingEntity owner = entity.getOwner();
        if (owner != null) {
            boolean isPlayer = owner.getType() == EntityType.PLAYER;
            boolean slimPlayerModel = isPlayer && "slim".equals(((AbstractClientPlayerEntity) owner).getModelName());
            getEntityModel().setVisibility(entity.getSide() == HandSide.LEFT, isPlayer, slimPlayerModel);
        }
        super.render(entity, yRotation, partialTick, matrixStack, buffer, packedLight);
    }
}
