package com.github.standobyte.jojo.client.render.entity.model.animnew;

import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelPartDefaultState {
    public final ModelRenderer modelPart;
    public final IModelRendererScale modelPartScale;
    public final float x;
    public final float y;
    public final float z;
    public final float xRot;
    public final float yRot;
    public final float zRot;
    
    public static ModelPartDefaultState fromModelPart(ModelRenderer modelPart) {
        return modelPart == null ? null : new ModelPartDefaultState(modelPart, 
                modelPart.x, modelPart.y, modelPart.z, 
                modelPart.xRot, modelPart.yRot, modelPart.zRot);
    }
    
    public ModelPartDefaultState(ModelRenderer modelPart, 
            float x, float y, float z, 
            float xRot, float yRot, float zRot) {
        this.modelPart = modelPart;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xRot = xRot;
        this.yRot = yRot;
        this.zRot = zRot;
        this.modelPartScale = modelPart instanceof IModelRendererScale ? (IModelRendererScale) modelPart : null;
    }
    
    public void reset() {
        modelPart.x = this.x;
        modelPart.y = this.y;
        modelPart.z = this.z;
        modelPart.xRot = this.xRot;
        modelPart.yRot = this.yRot;
        modelPart.zRot = this.zRot;
        if (modelPartScale != null) {
            modelPartScale.resetScale();
        }
    }
    
}
