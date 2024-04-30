package com.github.standobyte.jojo.client.render.item.generic;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.bb.BlockbenchStandModelHelper;
import com.github.standobyte.jojo.client.resources.models.ResourceEntityModels;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class CustomModelItemISTER<M extends Model> extends ItemStackTileEntityRenderer {
    private final ResourceLocation modelResource;
    private final ResourceLocation texture;
    private final Supplier<? extends Item> item;
    private final Supplier<M> modelObjConstructor;
    
    protected M model;
    @Nullable protected LivingEntity entity;
    
    public CustomModelItemISTER(ResourceLocation modelResource, ResourceLocation texture, 
            Supplier<? extends Item> item, Supplier<M> modelObjConstructor) {
        this.modelResource = modelResource;
        this.texture = texture;
        this.item = item;
        this.modelObjConstructor = modelObjConstructor;
        ResourceEntityModels.addListener(this.modelResource, parsedModel -> {
            try {
                M newModel = this.modelObjConstructor.get();
                BlockbenchStandModelHelper.replaceModelParts(newModel, parsedModel.getNamedModelParts());
                this.model = newModel;
            } catch (IllegalArgumentException | IllegalAccessException e) {
                JojoMod.LOGGER.error("Failed to load model {}", this.modelResource);
                e.printStackTrace();
            }
        });
    }
    
    public void setEntity(@Nullable LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, 
            IRenderTypeBuffer renderTypeBuffer, int light, int overlay) {
        Item item = itemStack.getItem();
        if (item == this.item.get()) {
            matrixStack.pushPose();
            matrixStack.scale(-1.0F, -1.0F, 1.0F);
            matrixStack.translate(-0.5, -1.5, 0.5);
            IVertexBuilder vertexBuilder = ItemRenderer.getFoilBufferDirect(
                    renderTypeBuffer, model.renderType(texture), false, itemStack.hasFoil());
            beforeRender(itemStack, transformType, matrixStack, renderTypeBuffer, light, overlay);
            model.renderToBuffer(matrixStack, vertexBuilder, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
            matrixStack.popPose();
        }
    }
    
    protected void beforeRender(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, 
            IRenderTypeBuffer renderTypeBuffer, int light, int overlay) {}

}
