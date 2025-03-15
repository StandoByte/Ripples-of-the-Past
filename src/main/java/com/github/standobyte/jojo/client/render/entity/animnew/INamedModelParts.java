package com.github.standobyte.jojo.client.render.entity.animnew;

import net.minecraft.client.renderer.model.ModelRenderer;

public interface INamedModelParts {
    void putNamedModelPart(String name, ModelRenderer modelPart);
    ModelRenderer getModelPart(String name);
}
