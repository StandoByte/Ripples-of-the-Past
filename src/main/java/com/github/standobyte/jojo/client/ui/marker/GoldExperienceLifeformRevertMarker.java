package com.github.standobyte.jojo.client.ui.marker;

import java.util.List;

import com.github.standobyte.jojo.action.stand.effect.GECreatedLifeformEffect;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.stand.ModStandEffects;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Matrix4f;

public class GoldExperienceLifeformRevertMarker extends MarkerRenderer {
    
    public GoldExperienceLifeformRevertMarker(Minecraft mc) {
        super(null, mc);
    }
    
    @Override
    protected boolean shouldRender() {
//        ActionsOverlayGui hud = ActionsOverlayGui.getInstance();
//        return hud.showExtraActionHud(ModStandsInit.CRAZY_DIAMOND_BLOCK_BULLET.get())
//               && !mc.player.isShiftKeyDown();
        return true;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    protected void renderIcon(MatrixStack matrixStack, MarkerInstance marker) {
        marker.standEffect.ifPresent(effect -> {
            if (effect instanceof GECreatedLifeformEffect) {
                ItemStack item = ((GECreatedLifeformEffect) effect).getItemView();
                if (item != null && !item.isEmpty()) {
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
                    matrixStack.scale(16, 16, 1);
                    matrixStack.scale(1, -1, -1);
                    
                    // FIXME the item is distorted the further it is from the cursor
                    itemRenderer.render(item, ItemCameraTransforms.TransformType.GUI, false, 
                            matrixStack, buffer, ClientUtil.MAX_MODEL_LIGHT, OverlayTexture.NO_OVERLAY, itemModel);
                    matrixStack.popPose();
                    buffer.endBatch();
                    RenderSystem.enableDepthTest();
                    
                    RenderSystem.disableAlphaTest();
                    RenderSystem.disableRescaleNormal();
                }
            }
        });
    }
    
    @Override
    protected void updatePositions(List<MarkerInstance> list, float partialTick) {
        fillWithStandEffectTargets(list, partialTick, ModStandEffects.GE_CREATED_LIFEFORM.get(), 32, mc, true);
    }
}
