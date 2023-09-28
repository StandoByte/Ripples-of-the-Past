package com.github.standobyte.jojo.client.render.entity.model.stand.bb;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.stand.HumanoidStandModel;
import com.github.standobyte.jojo.entity.stand.StandEntity;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class BlockbenchStandModelHelper {

    /*
     * Allows adding models exported from Blockbench with minimal edits to the model file
     * 
     * Add the .java model file exported from Blockbench to your project, 
     * then create another class for your actual model (which will contain stuff like animations).
     * Call this method from your model's constructor to copy the ModelRenderer values from the Blockbench method.
     * 
     * Example: CrazyDiamondModel2 and CrazyDiamondModelConvertExample.
     * CrazyDiamondModel2 can be used interchangeably with CrazyDiamondModel in CrazyDiamondRenderer.
     *
     */
    public static <T extends StandEntity, M extends HumanoidStandModel<T>> void partsFromBlockbenchExport(
            EntityModel<?> bbSourceModel, M inModModel) {
        Field[] exportedModelParts = bbSourceModel.getClass().getDeclaredFields();
        List<Field> inModModelParts = FieldUtils.getAllFieldsList(inModModel.getClass()).stream()
                .filter(field -> ModelRenderer.class.isAssignableFrom(field.getType()))
                .collect(Collectors.toList());
        
        for (Field exportedModelPartField : exportedModelParts) {
            String name = exportedModelPartField.getName();
            
            Iterator<Field> it = inModModelParts.iterator();
            while (it.hasNext()) {
                Field inModPartField = it.next();
                if (
                        inModPartField.getName().equals(name)
                        && inModPartField.getType().isAssignableFrom(exportedModelPartField.getType())
                        ) {
                    inModPartField.setAccessible(true);
                    exportedModelPartField.setAccessible(true);
                    try {
                        inModPartField.set(inModModel, exportedModelPartField.get(bbSourceModel));
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        JojoMod.getLogger().error("Failed to add humanoid Stand model parts for model {}", bbSourceModel.getClass().getName());
                        e.printStackTrace();
                    }
                }
            }
        }
        
        inModModel.texWidth = bbSourceModel.texWidth;
        inModModel.texHeight = bbSourceModel.texHeight;
    }
}
