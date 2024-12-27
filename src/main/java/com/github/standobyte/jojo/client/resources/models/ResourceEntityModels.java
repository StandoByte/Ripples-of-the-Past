package com.github.standobyte.jojo.client.resources.models;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.bb.BlockbenchStandModelHelper;
import com.github.standobyte.jojo.client.render.entity.bb.EntityModelUnbaked;
import com.github.standobyte.jojo.client.resources.models.StandModelOverrides.CustomModelPrepared;

import net.minecraft.client.renderer.model.Model;
import net.minecraft.util.ResourceLocation;

public class ResourceEntityModels {
    static final Map<ResourceLocation, Consumer<EntityModelUnbaked>> resourceListeners = new HashMap<>();
    
    
    static void loadEntityModel(ResourceLocation listenerId, CustomModelPrepared readJson) {
        if (resourceListeners.containsKey(listenerId)) {
            EntityModelUnbaked modelOverride = readJson.createModel(listenerId);
            resourceListeners.get(listenerId).accept(modelOverride);
        }
    }
    
    public static <M extends Model> void addModelLoader(ResourceLocation modelPath, Supplier<M> makeNewModel, Consumer<M> applyModel) {
        addListener(modelPath, parsedModel -> {
            try {
                M newModel = makeNewModel.get();
                BlockbenchStandModelHelper.replaceModelParts(newModel, parsedModel.getNamedModelParts());
                applyModel.accept(newModel);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                JojoMod.getLogger().error("Failed to load model {}", modelPath);
                e.printStackTrace();
            }
        });
    }
    
    public static void addListener(ResourceLocation id, Consumer<EntityModelUnbaked> onLoad) {
        resourceListeners.put(id, onLoad);
    }
}
