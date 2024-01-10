package com.github.standobyte.jojo.client.render.entity.bb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.model.ModelRenderer;

public class EntityModelUnbaked {
    private final Map<String, ModelRenderer> modelParts = new HashMap<>();
    public final int texWidth;
    public final int texHeight;
    
    public EntityModelUnbaked(int texWidth, int texHeight) {
        this.texWidth = texWidth;
        this.texHeight = texHeight;
    }
    
    private final Map<ModelRenderer, String> orphanage = new HashMap<>();
    public void addModelPart(String name, ModelRenderer modelPart, @Nullable String parentName) {
        modelParts.put(name, modelPart);
        if (parentName != null) {
            if (parentName.equals(name)) throw new IllegalArgumentException();
            
            ModelRenderer parent = modelParts.get(parentName);
            if (parent != null) {
                parent.addChild(modelPart);
            }
            else {
                orphanage.put(modelPart, parentName);
            }
        }
        
        if (!orphanage.isEmpty()) {
            Iterator<Map.Entry<ModelRenderer, String>> orphanIter = orphanage.entrySet().iterator();
            while (orphanIter.hasNext()) {
                Map.Entry<ModelRenderer, String> orphan = orphanIter.next();
                if (orphan.getValue().equals(name)) {
                    modelPart.addChild(orphan.getKey());
                    orphanIter.remove();
                }
            }
        }
    }
    
    public Map<String, ModelRenderer> getNamedModelParts() {
        return modelParts;
    }

}
