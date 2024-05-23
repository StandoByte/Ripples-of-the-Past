package com.github.standobyte.jojo.client.ui.marker;

import java.util.List;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.effect.GECreatedLifeformEffect;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.entity.ObjectEntity;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.IPower.PowerClassification;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class GoldExperienceLifeformRevertMarker extends MarkerRenderer {
    
    public GoldExperienceLifeformRevertMarker(Minecraft mc) {
        super(null, mc);
    }
    
    @Override
    protected boolean shouldRender() {
        ActionsOverlayGui hud = ActionsOverlayGui.getInstance();
        return hud.getCurrentMode() == PowerClassification.STAND && hud.showExtraActionHud(ModStandsInit.GOLD_EXPERIENCE_REVERT_LIFEFORM.get());
    }
    
    @Override
    protected void renderIcon(MatrixStack matrixStack, MarkerInstance marker, float partialTick) {
        marker.standEffect.ifPresent(effect -> {
            if (effect instanceof GECreatedLifeformEffect) {
                GECreatedLifeformEffect lifeformData = (GECreatedLifeformEffect) effect;
                ItemStack item = lifeformData.getItemView();
                if (item != null && !item.isEmpty()) {
                    renderItem(matrixStack, item, partialTick);
                }
                else {
                    Entity sourceEntity = lifeformData.getSource().getSourceEntity();
                    if (sourceEntity instanceof ObjectEntity) {
                        ObjectEntity.Type objectType = ((ObjectEntity) sourceEntity).getObjectType();
                        switch (objectType) {
                        case TOOTH:
                            mc.getTextureManager().bind(ICON_TOOTH);
                            AbstractGui.blit(matrixStack, 0, 0, 0, 0, 16, 16, 16, 16);
                            break;
                        }
                    }
                }
            }
        });
    }
    
    @SuppressWarnings("deprecation")
    protected void renderItem(MatrixStack matrixStack, ItemStack item, float partialTick) {
        ItemRenderer itemRenderer = mc.getItemRenderer();
        
        TextureManager textureManager = mc.textureManager;
        IBakedModel itemModel = itemRenderer.getModel(item, null, null);
        
        textureManager.bind(AtlasTexture.LOCATION_BLOCKS);
        textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        IRenderTypeBuffer.Impl buffer = mc.renderBuffers().bufferSource();
        
        matrixStack.pushPose();
        matrixStack.translate(8, 8, 0);
        matrixStack.scale(16, 16, 0.0625f);
        matrixStack.scale(1, -1, -1);
        
        matrixStack.last().normal().setIdentity(); 
        matrixStack.last().normal().mul(Vector3f.XP.rotationDegrees(mc.gameRenderer.getMainCamera().getXRot() - 90));
        matrixStack.last().normal().mul(Vector3f.YP.rotationDegrees(45));

        // FIXME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! the item model isn't rendered behind blocks/entities
        itemRenderer.render(item, ItemCameraTransforms.TransformType.GUI, false, 
                matrixStack, buffer, ClientUtil.MAX_MODEL_LIGHT, OverlayTexture.NO_OVERLAY, itemModel);
        
        matrixStack.popPose();
        buffer.endBatch();
        
        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
    }
    


    public void fuckthis(ItemStack pItemStack, ItemCameraTransforms.TransformType pTransformType, boolean pLeftHand, 
            MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, int pCombinedOverlay, IBakedModel pModel) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        if (!pItemStack.isEmpty()) {
            pMatrixStack.pushPose();
            boolean flag = pTransformType == ItemCameraTransforms.TransformType.GUI || pTransformType == ItemCameraTransforms.TransformType.GROUND || pTransformType == ItemCameraTransforms.TransformType.FIXED;

            pModel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(pMatrixStack, pModel, pTransformType, pLeftHand);
            pMatrixStack.translate(-0.5D, -0.5D, -0.5D);
            if (!pModel.isCustomRenderer() && (pItemStack.getItem() != Items.TRIDENT || flag)) {
                boolean flag1;
                if (pTransformType != ItemCameraTransforms.TransformType.GUI && !pTransformType.firstPerson() && pItemStack.getItem() instanceof BlockItem) {
                    Block block = ((BlockItem)pItemStack.getItem()).getBlock();
                    flag1 = !(block instanceof BreakableBlock) && !(block instanceof StainedGlassPaneBlock);
                } else {
                    flag1 = true;
                }
                if (pModel.isLayered()) { net.minecraftforge.client.ForgeHooksClient.drawItemLayered(itemRenderer, pModel, pItemStack, pMatrixStack, pBuffer, pCombinedLight, pCombinedOverlay, flag1); }
                else {
                    RenderType rendertype = RenderTypeLookup.getRenderType(pItemStack, flag1);
                    IVertexBuilder ivertexbuilder;
                    if (pItemStack.getItem() == Items.COMPASS && pItemStack.hasFoil()) {
                        pMatrixStack.pushPose();
                        MatrixStack.Entry matrixstack$entry = pMatrixStack.last();
                        if (pTransformType == ItemCameraTransforms.TransformType.GUI) {
                            matrixstack$entry.pose().multiply(0.5F);
                        } else if (pTransformType.firstPerson()) {
                            matrixstack$entry.pose().multiply(0.75F);
                        }

                        if (flag1) {
                            ivertexbuilder = ItemRenderer.getCompassFoilBufferDirect(pBuffer, rendertype, matrixstack$entry);
                        } else {
                            ivertexbuilder = ItemRenderer.getCompassFoilBuffer(pBuffer, rendertype, matrixstack$entry);
                        }

                        pMatrixStack.popPose();
                    } else if (flag1) {
                        ivertexbuilder = ItemRenderer.getFoilBufferDirect(pBuffer, rendertype, true, pItemStack.hasFoil());
                    } else {
                        ivertexbuilder = ItemRenderer.getFoilBuffer(pBuffer, rendertype, true, pItemStack.hasFoil());
                    }

                    itemRenderer.renderModelLists(pModel, pItemStack, pCombinedLight, pCombinedOverlay, pMatrixStack, ivertexbuilder);
                }
            } else {
                pItemStack.getItem().getItemStackTileEntityRenderer().renderByItem(pItemStack, pTransformType, pMatrixStack, pBuffer, pCombinedLight, pCombinedOverlay);
            }

            pMatrixStack.popPose();
        }
    }
    
    private static final ResourceLocation ICON_TOOTH = new ResourceLocation(JojoMod.MOD_ID, "textures/icons/tooth.png");
    
    @Override
    protected void updatePositions(List<MarkerInstance> list, float partialTick) {
        GoldExperienceLifeformMarker.updateGELifeformMarkers(list, partialTick, mc, true);
    }
}
