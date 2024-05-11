package com.github.standobyte.jojo.client.render.item.generic;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.bb.BlockbenchStandModelHelper;
import com.github.standobyte.jojo.client.resources.models.ResourceEntityModels;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;

public class CustomModelItemISTER<M extends Model> extends ItemStackTileEntityRenderer implements ISTERWithEntity {
    protected final ResourceLocation modelResource;
    protected final ResourceLocation texture;
    protected final Supplier<? extends Item> item;
    protected final Supplier<M> modelObjConstructor;
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
                JojoMod.getLogger().error("Failed to load model {}", this.modelResource);
                e.printStackTrace();
            }
        });
    }
    
    @Override
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
            doRender(itemStack, transformType, matrixStack, renderTypeBuffer, light, overlay);
            matrixStack.popPose();
        }
    }
    
    protected void doRender(ItemStack itemStack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, 
            IRenderTypeBuffer renderTypeBuffer, int light, int overlay) {
        IVertexBuilder vertexBuilder = ItemRenderer.getFoilBufferDirect(
                renderTypeBuffer, model.renderType(texture), false, itemStack.hasFoil());
        model.renderToBuffer(matrixStack, vertexBuilder, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }
    
    
    
    public static void renderItemNormally(MatrixStack matrixStack, ItemStack itemStack, TransformType transformType, 
            IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, IBakedModel itemModel) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        boolean cull;
        if (transformType != ItemCameraTransforms.TransformType.GUI && !transformType.firstPerson() && itemStack.getItem() instanceof BlockItem) {
            Block block = ((BlockItem)itemStack.getItem()).getBlock();
            cull = !(block instanceof BreakableBlock) && !(block instanceof StainedGlassPaneBlock);
        } else {
            cull = true;
        }
        if (itemModel.isLayered()) {
            ForgeHooksClient.drawItemLayered(itemRenderer, itemModel, itemStack, matrixStack, buffer, combinedLight, combinedOverlay, cull);
        }
        else {
            RenderType renderType = RenderTypeLookup.getRenderType(itemStack, cull);
            IVertexBuilder vertexBuilder;
            if (cull) {
                vertexBuilder = ItemRenderer.getFoilBufferDirect(buffer, renderType, true, itemStack.hasFoil());
            } else {
                vertexBuilder = ItemRenderer.getFoilBuffer(buffer, renderType, true, itemStack.hasFoil());
            }

            itemRenderer.renderModelLists(itemModel, itemStack, combinedLight, combinedOverlay, matrixStack, vertexBuilder);
        }
    }
}
