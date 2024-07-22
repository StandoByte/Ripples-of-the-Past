package com.github.standobyte.jojo.client.render.entity.model.stand.bb;

import java.util.Map;

import com.github.standobyte.jojo.client.render.entity.bb.EntityModelUnbaked;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

/**
 * @deprecated Import the {@link com.github.standobyte.jojo.client.render.entity.bb.BlockbenchStandModelHelper} class instead.
 */
@Deprecated
public class BlockbenchStandModelHelper {

    @Deprecated
    public static void fillFromBlockbenchExport(EntityModel<?> bbSourceModel, EntityModel<?> inModModel) {
        com.github.standobyte.jojo.client.render.entity.bb.BlockbenchStandModelHelper.fillFromBlockbenchExport(bbSourceModel, inModModel);
    }

    @Deprecated
    public static void replaceModelParts(EntityModel<?> inModModel, Map<String, ModelRenderer> source) throws IllegalArgumentException, IllegalAccessException {
        com.github.standobyte.jojo.client.render.entity.bb.BlockbenchStandModelHelper.replaceModelParts(inModModel, source);
    }

    @Deprecated
    public static void replaceCubes(EntityModel<?> inModModel, Map<String, ModelRenderer> source) throws IllegalArgumentException, IllegalAccessException {
        com.github.standobyte.jojo.client.render.entity.bb.BlockbenchStandModelHelper.replaceCubes(inModModel, source);
    }

    @Deprecated
    public static <M extends EntityModel<?>> void fillFromUnbaked(EntityModelUnbaked model, M inModModel) {
        com.github.standobyte.jojo.client.render.entity.bb.BlockbenchStandModelHelper.fillFromUnbaked(model, inModModel);
    }
}
