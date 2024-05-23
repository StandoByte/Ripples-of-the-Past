package com.github.standobyte.jojo.client.render.entity.bb;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.model.animnew.INamedModelParts;
import com.github.standobyte.jojo.client.render.entity.pose.XRotationModelRenderer;
import com.github.standobyte.jojo.util.mc.reflection.ClientReflection;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.renderer.model.Model;
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
    public static void fillFromBlockbenchExport(Model bbSourceModel, Model inModModel) {
        Field[] bbModelPartFields = bbSourceModel.getClass().getDeclaredFields();
        Map<String, ModelRenderer> bbModelParts = new HashMap<>();

        try {
            for (Field bbModelPartField : bbModelPartFields) {
                if (bbModelPartField.getType() == ModelRenderer.class) {
                    bbModelPartField.setAccessible(true);
                    bbModelParts.put(bbModelPartField.getName(), (ModelRenderer) bbModelPartField.get(bbSourceModel));
                }
            }
            
            replaceModelParts(inModModel, bbModelParts);
        } catch (Exception e) {
            JojoMod.getLogger().error("Failed to add model parts to {} via Blockbench helper", inModModel.getClass().getName());
            e.printStackTrace();
        }
        
        inModModel.texWidth = bbSourceModel.texWidth;
        inModModel.texHeight = bbSourceModel.texHeight;
    }
    
    public static void replaceModelParts(Model inModModel, Map<String, ModelRenderer> source) throws IllegalArgumentException, IllegalAccessException {
        Set<Field> declaredModelParts = FieldUtils.getAllFieldsList(inModModel.getClass()).stream()
                .filter(field -> ModelRenderer.class.isAssignableFrom(field.getType()))
                .collect(Collectors.toCollection(HashSet::new));
        List<ModelRenderer> editedParts = new ArrayList<>();
        Map<ModelRenderer, ModelRenderer> remapParents = new HashMap<>();
        INamedModelParts putNamed = inModModel instanceof INamedModelParts ? (INamedModelParts) inModModel : null;
        
        for (Map.Entry<String, ModelRenderer> entry : source.entrySet()) {
            String name = entry.getKey();
            ModelRenderer blockbenchPart = entry.getValue();
            
            Iterator<Field> it = declaredModelParts.iterator();
            boolean foundModelPart = false;
            while (it.hasNext() && !foundModelPart) {
                Field inModPartField = it.next();
                
                if (inModPartField.getName().equals(name)) {
                    boolean xRotJank = false;
                    if (!inModPartField.getType().isAssignableFrom(blockbenchPart.getClass())) {
                        if (inModPartField.getType() == XRotationModelRenderer.class && blockbenchPart.getClass() == ModelRenderer.class) {
                            xRotJank = true;
                        }
                        else {
                            RuntimeException e = new ClassCastException(blockbenchPart.getClass() + " can't be cast to " + inModPartField.getType());
                            e.printStackTrace();
                            throw e;
                        }
                    }
                    
                    if (xRotJank) {
                        ModelRenderer jank = jankToKeepAddonsWorkingForNow(blockbenchPart);
                        remapParents.put(blockbenchPart, jank);
                        blockbenchPart = jank;
                    }
                    inModPartField.setAccessible(true);
                    editedParts.add(blockbenchPart);
                    inModPartField.set(inModModel, blockbenchPart);
                    if (putNamed != null) {
                        putNamed.putMamedModelPart(name, blockbenchPart);
                    }
                    
                    it.remove();
                    foundModelPart = true;
                }
            }
        }
        
        for (Field field : declaredModelParts) {
            field.setAccessible(true);
            ModelRenderer declaredPartNotInGecko = (ModelRenderer) field.get(inModModel);
            if (declaredPartNotInGecko != null) {
                ClientReflection.getCubes(declaredPartNotInGecko).clear();
                ClientReflection.getChildren(declaredPartNotInGecko).clear();
            }
        }
        
        for (ModelRenderer modelPart : editedParts) {
            ObjectList<ModelRenderer> children = ClientReflection.getChildren(modelPart);
            if (!children.isEmpty()) {
                remapParents.forEach((oldChild, newChild) -> {
                    Collections.replaceAll(children, oldChild, newChild);
                });
            }
        }
    }
    
    public static void replaceCubes(Model inModModel, Map<String, ModelRenderer> source) throws IllegalArgumentException, IllegalAccessException {
        List<Field> inModModelParts = FieldUtils.getAllFieldsList(inModModel.getClass()).stream()
                .filter(field -> ModelRenderer.class.isAssignableFrom(field.getType()))
                .collect(Collectors.toList());
        
        for (Map.Entry<String, ModelRenderer> entry : source.entrySet()) {
            String name = entry.getKey();
            ModelRenderer blockbenchPart = entry.getValue();
            
            Iterator<Field> it = inModModelParts.iterator();
            while (it.hasNext()) {
                Field inModPartField = it.next();
                if (inModPartField.getName().equals(name)) {
                    
                    inModPartField.setAccessible(true);
                    ModelRenderer inModModelPart = (ModelRenderer) inModPartField.get(inModModel);
                    ObjectList<ModelRenderer.ModelBox> cubesReplacing = ClientReflection.getCubes(blockbenchPart);
                    ClientReflection.setCubes(inModModelPart, cubesReplacing);
                    
                    it.remove();
                }
            }
        }
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
    
    
    
    public static <M extends Model> void fillFromUnbaked(EntityModelUnbaked model, M inModModel) {
        try {
            replaceModelParts(inModModel, model.getNamedModelParts());
        } catch (Exception e) {
            JojoMod.getLogger().error("Failed to import Geckolib format model as {}", inModModel.getClass().getName());
            e.printStackTrace();
        }
    }
}
