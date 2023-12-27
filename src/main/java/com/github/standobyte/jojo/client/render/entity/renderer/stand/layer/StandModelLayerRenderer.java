package com.github.standobyte.jojo.client.render.entity.renderer.stand.layer;

import java.util.Optional;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

public abstract class StandModelLayerRenderer<T extends StandEntity, M extends StandEntityModel<T>> extends LayerRenderer<T, M> {
    protected final StandEntityRenderer<T, M> entityRenderer;
    private final M model;
    protected final ResourceLocation texture;

    public StandModelLayerRenderer(IEntityRenderer<T, M> entityRenderer, M model, ResourceLocation texture) {
        super(entityRenderer);
        this.entityRenderer = (StandEntityRenderer<T, M>) entityRenderer;
        this.model = model;
        this.texture = texture;
        if (model != getParentModel()) {
            model.afterInit();
        }
    }

    public M getLayerModel() {
        return model;
    }

    public boolean shouldRender(T entity, Optional<ResourceLocation> standSkin) {
        return true;
    }

    public int getPackedLight(int packedLight) {
        return packedLight;
    }
    
    public RenderType getRenderType(T entity) {
        return entityRenderer.getRenderType(entity, getLayerModel(), getLayerTexture(entity.getStandSkin()));
    }
    
    public ResourceLocation getBaseTexture() {
        return texture;
    }

    public ResourceLocation getLayerTexture(Optional<ResourceLocation> standSkin) {
        return StandSkinsManager.getInstance()
                .getRemappedResPath(manager -> manager.getStandSkin(standSkin), texture);
    }
    
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight,
            T entity, float walkAnimPos, float walkAnimSpeed, float partialTick,
            float ticks, float headYRotation, float headXRotation) {
        if (shouldRender(entity, entity.getStandSkin())) {
            RenderType renderType = getRenderType(entity);
            if (renderType != null) {
                entityRenderer.renderLayer(matrixStack, buffer.getBuffer(renderType), getPackedLight(packedLight), 
                        entity, walkAnimPos, walkAnimSpeed, partialTick, 
                        ticks, headYRotation, headXRotation, getLayerModel());
            }
        }
    }
}
