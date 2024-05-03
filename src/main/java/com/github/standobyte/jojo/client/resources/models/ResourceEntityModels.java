package com.github.standobyte.jojo.client.resources.models;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.github.standobyte.jojo.client.render.entity.bb.EntityModelUnbaked;
import com.github.standobyte.jojo.client.resources.models.StandModelOverrides.Format;
import com.google.gson.JsonElement;

import net.minecraft.util.ResourceLocation;

public class ResourceEntityModels {
    static final Map<ResourceLocation, Consumer<EntityModelUnbaked>> resourceListeners = new HashMap<>();
    
    
    static void loadEntityModel(ResourceLocation listenerId, JsonElement modelJson, Format format) {
        if (resourceListeners.containsKey(listenerId)) {
            EntityModelUnbaked modelOverride = format.parse(modelJson, listenerId);
            resourceListeners.get(listenerId).accept(modelOverride);
        }
    }
    
    public static void addListener(ResourceLocation id, Consumer<EntityModelUnbaked> onLoad) {
        resourceListeners.put(id, onLoad);
    }
}
