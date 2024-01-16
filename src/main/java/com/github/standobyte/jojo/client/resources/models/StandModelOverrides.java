package com.github.standobyte.jojo.client.resources.models;

import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.bb.BlockbenchStandModelHelper;
import com.github.standobyte.jojo.client.render.entity.bb.EntityModelUnbaked;
import com.github.standobyte.jojo.client.render.entity.bb.ParseGeckoModel;
import com.github.standobyte.jojo.client.render.entity.bb.ParseGenericModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel.StandModelRegistryObj;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class StandModelOverrides extends JsonReloadListener {
    private final Map<ResourceLocation, StandEntityModel<?>> modelOverrides = new HashMap<>();
    
    public StandModelOverrides(Gson gson) {
        super(gson, "geo");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, IResourceManager pResourceManager,
            IProfiler pProfiler) {
        modelOverrides.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
            ResourceLocation location = entry.getKey();
            boolean bb = location.getPath().contains(".bb");
            location = new ResourceLocation(location.getNamespace(), location.getPath().replace(".geo", "").replace(".bb", ""));
            StandModelRegistryObj standModel = StandEntityModel.getRegisteredModel(location);
            if (standModel != null) {
                JsonElement json = entry.getValue();
                if (json.isJsonObject()) {
                    StandEntityModel<?> modelCopy = standModel.createNewModelCopy();
                    EntityModelUnbaked modelOverride;
                    if (bb) modelOverride = ParseGenericModel.parseGenericModel(json);
                    else    modelOverride = ParseGeckoModel.parseGeckoModel(json);
                    try {
                        BlockbenchStandModelHelper.replaceModelParts(modelCopy, modelOverride.getNamedModelParts());
                    } catch (Exception e) {
                        JojoMod.getLogger().error("Failed to import Geckolib format model as {}", modelCopy.getClass().getName());
                        e.printStackTrace();
                    }
                    modelCopy.afterInit();
                    modelOverrides.put(location, modelCopy);
                }
            }
        }
    }
    
    public <T extends StandEntity, M extends StandEntityModel<T>> M overrideModel(M model) {
        ResourceLocation modelId = model.getModelId();
        if (modelId != null) {
            StandEntityModel<?> resourcePackModel = modelOverrides.get(modelId);
            if (resourcePackModel != null) {
                return (M) resourcePackModel;
            }
        }
        
        return model;
    }
}
