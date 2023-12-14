package com.github.standobyte.jojo.client.render.entity.renderer.stand.layer;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.github.standobyte.jojo.client.resources.CustomResources;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

public abstract class StandModelLayerRenderer<T extends StandEntity, M extends StandEntityModel<T>> extends LayerRenderer<T, M> {
    protected final StandEntityRenderer<T, M> entityRenderer;
    private final boolean useParentModel;
    private final M model;

    public StandModelLayerRenderer(IEntityRenderer<T, M> entityRenderer, M model) {
        this(entityRenderer, false, model);
    }

    public StandModelLayerRenderer(IEntityRenderer<T, M> entityRenderer) {
        this(entityRenderer, true, null);
    }

    private StandModelLayerRenderer(IEntityRenderer<T, M> entityRenderer, boolean useParentModel, M model) {
        super(entityRenderer);
        this.entityRenderer = (StandEntityRenderer<T, M>) entityRenderer;
        this.model = model;
        this.useParentModel = useParentModel;
        if (!useParentModel && model != null) {
            model.afterInit();
        }
    }

    public M getLayerModel() {
        if (useParentModel) {
            return getParentModel();
        }
        if (model != null) {
            return CustomResources.getStandModelOverrides().overrideModel(model);
        }
        return null;
    }

    public boolean shouldRender(T entity) {
        return true;
    }

    public int getPackedLight(int packedLight) {
        return packedLight;
    }
    
    public RenderType getRenderType(T entity) {
        return entityRenderer.getRenderType(entity, getLayerModel(), getLayerTexture());
    }

    protected abstract ResourceLocation getLayerTexture();
    
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight,
            T entity, float walkAnimPos, float walkAnimSpeed, float partialTick,
            float ticks, float headYRotation, float headXRotation) {
        if (shouldRender(entity)) {
            RenderType renderType = getRenderType(entity);
            if (renderType != null) {
                entityRenderer.renderLayer(matrixStack, buffer.getBuffer(renderType), getPackedLight(packedLight), 
                        entity, walkAnimPos, walkAnimSpeed, partialTick, 
                        ticks, headYRotation, headXRotation, getLayerModel());
            }
        }
    }
}
