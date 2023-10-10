package com.github.standobyte.jojo.client.render.entity.model.stand.bb;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.stand.HumanoidStandModel;
import com.github.standobyte.jojo.client.render.entity.pose.XRotationModelRenderer;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import it.unimi.dsi.fastutil.objects.ObjectList;
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
        List<ModelRenderer> modelParts = new ArrayList<>();
        Map<ModelRenderer, ModelRenderer> remapChildren = new HashMap<>();
        
        for (Field exportedModelPartField : exportedModelParts) {
            String name = exportedModelPartField.getName();
            
            Iterator<Field> it = inModModelParts.iterator();
            while (it.hasNext()) {
                Field inModPartField = it.next();
                if (inModPartField.getName().equals(name)) {
                    boolean xRotJank = false;
                    if (!inModPartField.getType().isAssignableFrom(exportedModelPartField.getType())) {
                        if (inModPartField.getType() == XRotationModelRenderer.class && exportedModelPartField.getType() == ModelRenderer.class) {
                            xRotJank = true;
                        }
                        else {
                            RuntimeException e = new ClassCastException(exportedModelPartField.getType() + " can't be cast to " + inModPartField.getType());
                            e.printStackTrace();
                            throw e;
                        }
                    }
                    
                    inModPartField.setAccessible(true);
                    exportedModelPartField.setAccessible(true);
                    try {
                        ModelRenderer blockbenchPart = (ModelRenderer) exportedModelPartField.get(bbSourceModel);
                        if (xRotJank) {
                            ModelRenderer jank = jankToKeepAddonsWorkingForNow(blockbenchPart);
                            remapChildren.put(blockbenchPart, jank);
                            blockbenchPart = jank;
                        }
                        modelParts.add(blockbenchPart);
                        inModPartField.set(inModModel, blockbenchPart);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        JojoMod.getLogger().error("Failed to add humanoid Stand model parts for model {}", bbSourceModel.getClass().getName());
                        e.printStackTrace();
                    }
                    
                    it.remove();
                }
            }
        }
        
        for (ModelRenderer modelPart : modelParts) {
            ObjectList<ModelRenderer> children = ClientReflection.getChildren(modelPart);
            if (!children.isEmpty()) {
                remapChildren.forEach((oldPart, newPart) -> {
                    Collections.replaceAll(children, oldPart, newPart);
                });
            }
        }
        
        inModModel.texWidth = bbSourceModel.texWidth;
        inModModel.texHeight = bbSourceModel.texHeight;
    }
    
    private static XRotationModelRenderer jankToKeepAddonsWorkingForNow(ModelRenderer modelPart) {
        XRotationModelRenderer deepCopy = new XRotationModelRenderer(256, 256, 0, 0);
        
        deepCopy.x = modelPart.x;
        deepCopy.y = modelPart.y;
        deepCopy.z = modelPart.z;
        deepCopy.xRot = modelPart.xRot;
        deepCopy.yRot = modelPart.yRot;
        deepCopy.zRot = modelPart.zRot;
        deepCopy.mirror = modelPart.mirror;
        deepCopy.visible = modelPart.visible;
        
        ClientReflection.setCubes(deepCopy, ClientReflection.getCubes(modelPart));
        ClientReflection.setChildren(deepCopy, ClientReflection.getChildren(modelPart));
        
        return deepCopy;
    }
}
