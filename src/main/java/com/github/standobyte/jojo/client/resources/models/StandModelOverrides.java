package com.github.standobyte.jojo.client.resources.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.bb.BlockbenchStandModelHelper;
import com.github.standobyte.jojo.client.render.entity.bb.EntityModelUnbaked;
import com.github.standobyte.jojo.client.render.entity.bb.ParseGeckoModel;
import com.github.standobyte.jojo.client.render.entity.bb.ParseGenericModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry.StandModelRegistryObj;
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
            createModelFromJson(entry.getKey(), entry.getValue()).ifPresent(model -> modelOverrides.put(model.getKey(), model.getValue()));
        }
    }
    
    public static Optional<Pair<ResourceLocation, StandEntityModel<?>>> createModelFromJson(ResourceLocation modelResLoc, JsonElement json) {
        Format format = modelResLoc.getPath().contains(".bb") ? Format.BB_GENERIC : Format.GECKO;
        ResourceLocation modelId = new ResourceLocation(
                modelResLoc.getNamespace(), 
                modelResLoc.getPath().replace(".geo", "").replace(".bb", ""));
        StandModelRegistryObj registeredModel = StandModelRegistry.getRegisteredModel(modelId);
        if (registeredModel != null) {
            StandEntityModel<?> modelCopy = registeredModel.createNewModelCopy();
            EntityModelUnbaked modelOverride;
            switch (format) {
            case GECKO:
                modelOverride = ParseGeckoModel.parseGeckoModel(json, registeredModel.id);
                break;
            case BB_GENERIC:
                modelOverride = ParseGenericModel.parseGenericModel(json, registeredModel.id);
                break;
            default:
                return Optional.empty();
            }
            try {
                BlockbenchStandModelHelper.replaceModelParts(modelCopy, modelOverride.getNamedModelParts());
            } catch (Exception e) {
                JojoMod.getLogger().error("Failed to import Geckolib format model as {}", modelCopy.getClass().getName());
                e.printStackTrace();
            }
            modelCopy.afterInit();
            return Optional.of(Pair.of(modelId, modelCopy));
        }
        
        return Optional.empty();
    }
    
    public static enum Format {
        GECKO,
        BB_GENERIC
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
