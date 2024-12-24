package com.github.standobyte.jojo.client.render.entity.model.animnew;

import net.minecraft.client.renderer.model.ModelRenderer;

public interface INamedModelParts {
    ModelRenderer putNamedModelPart(String name, ModelRenderer modelPart);
    ModelRenderer getModelPart(String name);
}
