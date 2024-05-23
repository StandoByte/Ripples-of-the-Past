package com.github.standobyte.jojo.client.ui.marker;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.action.stand.GoldExperienceMarkItem;
import com.github.standobyte.jojo.action.stand.effect.GEItemMarkEffect;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;

public class GoldExperienceMarkedItemMarker extends MarkerRenderer {
    
    public GoldExperienceMarkedItemMarker(Minecraft mc) {
        super(null, mc);
    }
    
    @Override
    protected boolean shouldRender() {
        return true;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    protected void renderIcon(MatrixStack matrixStack, MarkerInstance marker, float partialTick) {
        ItemStack item = ((ItemMarkerInstance) marker).item;
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
            matrixStack.scale(16, 16, 0.0625f);
            matrixStack.scale(1, -1, -1);

            itemRenderer.render(item, ItemCameraTransforms.TransformType.GUI, false, 
                    matrixStack, buffer, ClientUtil.MAX_MODEL_LIGHT, OverlayTexture.NO_OVERLAY, itemModel);
            matrixStack.popPose();
            buffer.endBatch();
            RenderSystem.enableDepthTest();

            RenderSystem.disableAlphaTest();
            RenderSystem.disableRescaleNormal();
        }
    }
    
    @Override
    protected void updatePositions(List<MarkerInstance> list, float partialTick) {
        IStandPower.getStandPowerOptional(mc.player).ifPresent(stand -> {
            List<Pair<GEItemMarkEffect, Vector3d>> targets = GoldExperienceMarkItem.getTargets(stand, mc.player);
            Optional<GEItemMarkEffect> outlined = GoldExperienceMarkItem.getTargetedEffect(targets, mc.player);
            
            for (Pair<GEItemMarkEffect, Vector3d> pair : targets) {
                GEItemMarkEffect effect = pair.getLeft();
                Vector3d pos = pair.getRight();
                TrackerItemStack item = effect.getItemTracker(false);
                list.add(new ItemMarkerInstance(pos, 
                        outlined.map(outlinedEffect -> pair.getLeft() == outlinedEffect).orElse(false),
                        item.getItem()));
            }
        });
    }
    
    
    private static class ItemMarkerInstance extends MarkerInstance {
        final ItemStack item;

        public ItemMarkerInstance(Vector3d pos, boolean outlined, ItemStack itemStack) {
            super(pos, outlined);
            this.item = itemStack;
        }
    }

}
