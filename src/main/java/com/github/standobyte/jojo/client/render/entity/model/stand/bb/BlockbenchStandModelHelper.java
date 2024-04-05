package com.github.standobyte.jojo.client.render.entity.model.stand.bb;

import java.util.Map;

import com.github.standobyte.jojo.client.render.entity.bb.EntityModelUnbaked;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class BlockbenchStandModelHelper {
    
    public static void fillFromBlockbenchExport(EntityModel<?> bbSourceModel, EntityModel<?> inModModel) {
        com.github.standobyte.jojo.client.render.entity.bb.BlockbenchStandModelHelper.fillFromBlockbenchExport(bbSourceModel, inModModel);
    }
    
    public static void replaceModelParts(EntityModel<?> inModModel, Map<String, ModelRenderer> source) throws IllegalArgumentException, IllegalAccessException {
        com.github.standobyte.jojo.client.render.entity.bb.BlockbenchStandModelHelper.replaceModelParts(inModModel, source);
    }
    
    public static void replaceCubes(EntityModel<?> inModModel, Map<String, ModelRenderer> source) throws IllegalArgumentException, IllegalAccessException {
        com.github.standobyte.jojo.client.render.entity.bb.BlockbenchStandModelHelper.replaceCubes(inModModel, source);
    }
    
    public static <M extends EntityModel<?>> void fillFromUnbaked(EntityModelUnbaked model, M inModModel) {
        com.github.standobyte.jojo.client.render.entity.bb.BlockbenchStandModelHelper.fillFromUnbaked(model, inModModel);
    }
}
