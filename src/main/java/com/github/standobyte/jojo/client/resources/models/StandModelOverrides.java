package com.github.standobyte.jojo.client.resources.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.render.entity.bb.BlockbenchStandModelHelper;
import com.github.standobyte.jojo.client.render.entity.bb.EntityModelUnbaked;
import com.github.standobyte.jojo.client.render.entity.bb.ParseGeckoModel;
import com.github.standobyte.jojo.client.render.entity.bb.ParseGenericModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry.StandModelRegistryObj;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class StandModelOverrides extends ReloadListener<Map<StandModelOverrides.ModelType, Map<ResourceLocation, StandModelOverrides.CustomModelPrepared>>> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Gson gson;
    private final Map<ResourceLocation, StandEntityModel<?>> standModelOverrides = new HashMap<>();

    public StandModelOverrides(Gson gson) {
        this.gson = gson;
    }
    
    public static ModelPathInfo[] FILE_PATHS = new ModelPathInfo[] {
            new ModelPathInfo("geo", ".geo.json", Format.GECKO),
            new ModelPathInfo("bb", ".bb.json", Format.BB_GENERIC),
            new ModelPathInfo("bb", ".bbmodel", Format.BB_GENERIC)
    };
    
    @Override
    protected Map<ModelType, Map<ResourceLocation, CustomModelPrepared>> prepare(IResourceManager pResourceManager, IProfiler pProfiler) {
        Map<ModelType, Map<ResourceLocation, CustomModelPrepared>> map = Maps.newHashMap();
        for (ModelType modelType : ModelType.values()) {
            Map<ResourceLocation, CustomModelPrepared> entriesMap = map.compute(modelType, (__, ___) -> new HashMap<>());
            for (ModelPathInfo path : FILE_PATHS) {
                addEntries(path.directory + modelType.subDir, path.filePostfix, pResourceManager, 
                        path.format, modelType, entriesMap);
            }
        }
        return map;
    }
    
    protected void addEntries(String directory, String pathPostfix, IResourceManager pResourceManager, 
            Format format, ModelType modelType, Map<ResourceLocation, CustomModelPrepared> entriesMap) {
        for (ResourceLocation path : pResourceManager.listResources(directory, p -> p.endsWith(pathPostfix))) {
            String fileName = path.getPath();
            fileName = fileName.substring(directory.length() + 1, fileName.length() - pathPostfix.length());
            ResourceLocation preparedPath = new ResourceLocation(path.getNamespace(), fileName);
            
            try (
                    IResource iresource = pResourceManager.getResource(path);
                    InputStream inputstream = iresource.getInputStream();
                    Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
                    ) {
                JsonElement json = JSONUtils.fromJson(this.gson, reader, JsonElement.class);
                if (json != null) {
                    CustomModelPrepared preparedData = new CustomModelPrepared(json, format, modelType);
                    CustomModelPrepared alreadyPresent = entriesMap.get(preparedPath);
                    if (alreadyPresent != null) {
                        LOGGER.warn("Duplicate model {} found", preparedPath);
                    }
                    entriesMap.put(preparedPath, preparedData);
                } else {
                    LOGGER.error("Couldn't load data file {} from {} as it's null or empty", preparedPath, path);
                }
            } catch (IllegalArgumentException | IOException | JsonParseException e) {
                LOGGER.error("Couldn't parse data file {} from {}", preparedPath, path, e);
            }
        }
    }
    
    
    
    @Override
    protected void apply(Map<ModelType, Map<ResourceLocation, CustomModelPrepared>> pObject, 
            IResourceManager pResourceManager, IProfiler pProfiler) {
        standModelOverrides.clear();
        for (ModelType modelType : ModelType.values()) {
            for (Map.Entry<ResourceLocation, CustomModelPrepared> entry : pObject.get(modelType).entrySet()) {
                CustomModelPrepared modelRead = entry.getValue();
                ResourceLocation modelId = modelRead.clearFormatExtension(entry.getKey());
                switch (modelType) {
                case STAND:
                    Optional<Pair<ResourceLocation, StandEntityModel<?>>> standModel = createStandModelFromJson(modelId, modelRead);
                    standModel.ifPresent(model -> standModelOverrides.put(model.getKey(), model.getValue()));
                    break;
                case CLOTHES:
                    // TODO
                    break;
                case MISC:
                    ResourceEntityModels.loadEntityModel(modelId, modelRead);
                    break;
                default:
                    throw new AssertionError();
                }
            }
        }
    }
    
    public static Optional<Pair<ResourceLocation, StandEntityModel<?>>> createStandModelFromJson(
            ResourceLocation modelId, CustomModelPrepared readJson) {
        StandModelRegistryObj registeredModel = StandModelRegistry.getRegisteredModel(modelId);
        if (registeredModel != null) {
            StandEntityModel<?> modelCopy = registeredModel.createNewModelCopy();
            EntityModelUnbaked modelOverride = readJson.createModel(modelId);
            
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
        GECKO {
            @Override
            public EntityModelUnbaked parse(JsonElement json, ResourceLocation modelId) {
                return ParseGeckoModel.parseGeckoModel(json, modelId);
            }
        },
        BB_GENERIC {
            @Override
            public EntityModelUnbaked parse(JsonElement json, ResourceLocation modelId) {
                return ParseGenericModel.parseGenericModel(json, modelId);
            }
        };
        
        public abstract EntityModelUnbaked parse(JsonElement json, ResourceLocation modelId);
    }
    
    public static enum ModelType {
        STAND(""),
        CLOTHES("/clothes"),
        MISC("/misc");
        
        public final String subDir;
        private ModelType(String subDir) {
            this.subDir = subDir;
        }
    }
    
    public static class ModelPathInfo {
        public final String directory;
        public final String filePostfix;
        public final Format format;
        
        public ModelPathInfo(String directory, String filePostfix, Format format) {
            this.directory = directory;
            this.filePostfix = filePostfix;
            this.format = format;
        }
    }
    
    public static class CustomModelPrepared {
        public final JsonElement modelJson;
        public final Format format;
        public final ModelType modelType;
        
        public CustomModelPrepared(JsonElement modelJson, Format format, ModelType modelType) {
            this.modelJson = modelJson;
            this.format = format;
            this.modelType = modelType;
        }
        
        public EntityModelUnbaked createModel(ResourceLocation modelId) {
            EntityModelUnbaked model = format.parse(modelJson, modelId);
            return model;
        }
        
        public ResourceLocation clearFormatExtension(ResourceLocation modelId) {
            String path = modelId.getPath();
            path = path.replace(".geo", "").replace(".bb", "");
            return new ResourceLocation(modelId.getNamespace(), path);
        }
    }
    
    public <T extends StandEntity, M extends StandEntityModel<T>> M overrideModel(M model) {
        ResourceLocation modelId = model.getModelId();
        if (modelId != null) {
            StandEntityModel<?> resourcePackModel = standModelOverrides.get(modelId);
            if (resourcePackModel != null) {
                return (M) resourcePackModel;
            }
        }
        
        return model;
    }
}
