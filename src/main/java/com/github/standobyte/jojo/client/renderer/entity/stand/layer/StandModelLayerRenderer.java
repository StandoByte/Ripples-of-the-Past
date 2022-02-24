package com.github.standobyte.jojo.client.renderer.entity.stand.layer;

import com.github.standobyte.jojo.client.model.entity.stand.StandEntityModel;
import com.github.standobyte.jojo.client.renderer.entity.stand.AbstractStandRenderer;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

public abstract class StandModelLayerRenderer<T extends StandEntity, M extends StandEntityModel<T>> extends LayerRenderer<T, M> {
    protected final AbstractStandRenderer<T, M> entityRenderer;
    protected final M model;

    public StandModelLayerRenderer(IEntityRenderer<T, M> entityRenderer, M model) {
        super(entityRenderer);
        this.entityRenderer = (AbstractStandRenderer<T, M>) entityRenderer;
        this.model = model;
        model.afterInit();
    }

    public boolean shouldRender(T entity) {
        return true;
    }

    public int getPackedLight(int packedLight) {
        return packedLight;
    }
    
    public IVertexBuilder getBuffer(IRenderTypeBuffer buffer, T entity) {
        return buffer.getBuffer(entityRenderer.getRenderType(entity, getLayerTexture()));
    }

    public M getLayerModel() {
        return model;
    }

    protected abstract ResourceLocation getLayerTexture();
    
    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight,
            T entity, float walkAnimPos, float walkAnimSpeed, float partialTick,
            float ticks, float headYRotation, float headXRotation) {
        if (shouldRender(entity)) {
            entityRenderer.renderLayer(matrixStack, getBuffer(buffer, entity), getPackedLight(packedLight), 
                    entity, walkAnimPos, walkAnimSpeed, partialTick, 
                    ticks, headYRotation, headXRotation, 
                    getLayerTexture(), getLayerModel());
        }
    }
}
