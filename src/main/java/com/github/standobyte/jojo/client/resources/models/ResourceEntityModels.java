package com.github.standobyte.jojo.client.resources.models;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.github.standobyte.jojo.client.render.entity.bb.EntityModelUnbaked;
import com.github.standobyte.jojo.client.resources.models.StandModelOverrides.CustomModelPrepared;

import net.minecraft.util.ResourceLocation;

public class ResourceEntityModels {
    static final Map<ResourceLocation, Consumer<EntityModelUnbaked>> resourceListeners = new HashMap<>();
    
    
    static void loadEntityModel(ResourceLocation listenerId, CustomModelPrepared readJson) {
        if (resourceListeners.containsKey(listenerId)) {
            EntityModelUnbaked modelOverride = readJson.createModel(listenerId);
            resourceListeners.get(listenerId).accept(modelOverride);
        }
    }
    
    public static void addListener(ResourceLocation id, Consumer<EntityModelUnbaked> onLoad) {
        resourceListeners.put(id, onLoad);
    }
}
