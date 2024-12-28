package com.github.standobyte.jojo.client.resources.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.standobyte.jojo.client.render.entity.model.animnew.ParseGeckoAnims;
import com.github.standobyte.jojo.client.render.entity.model.animnew.mojang.Animation;
import com.github.standobyte.jojo.client.render.entity.model.animnew.molang.MolangInterpreter;
import com.github.standobyte.jojo.client.render.entity.model.animnew.stand.GeckoStandAnimator;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry.StandModelRegistryObj;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class GeckoAnimLoader extends ReloadListener<Map<ResourceLocation, JsonElement>> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Gson gson;
    private final Map<ResourceLocation, GeckoStandAnimator> loadedAnims = new HashMap<>();
    
    public GeckoAnimLoader(Gson gson) {
        this.gson = gson;
    }

    private static final String DIRECTORY = "animations";
    private static final String SUFFIX = ".animation.json";
    @Override
    protected Map<ResourceLocation, JsonElement> prepare(IResourceManager pResourceManager, IProfiler pProfiler) {
        Map<ResourceLocation, JsonElement> map = Maps.newHashMap();
        
        for (ResourceLocation path : pResourceManager.listResources(DIRECTORY, p -> p.endsWith(SUFFIX))) {
            String fileName = path.getPath();
            fileName = fileName.substring(DIRECTORY.length() + 1, fileName.length() - SUFFIX.length());
            ResourceLocation preparedPath = new ResourceLocation(path.getNamespace(), fileName);
            
            try (
                    IResource iresource = pResourceManager.getResource(path);
                    InputStream inputstream = iresource.getInputStream();
                    Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
                    ) {
                JsonElement json = JSONUtils.fromJson(this.gson, reader, JsonElement.class);
                if (json != null) {
                    boolean alreadyPresent = map.put(preparedPath, json) != null;
                    if (alreadyPresent) {
                        throw new IllegalStateException("Duplicate data file ignored with ID " + preparedPath);
                    }
                } else {
                    LOGGER.error("Couldn't load data file {} from {} as it's null or empty", preparedPath, path);
                }
            } catch (IllegalArgumentException | IOException | JsonParseException e) {
                LOGGER.error("Couldn't parse data file {} from {}", preparedPath, path, e);
            }
        }
        
        return map;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, IResourceManager pResourceManager, IProfiler pProfiler) {
        MolangInterpreter.init();
        loadedAnims.clear();
        StandModelRegistry.values().forEach(StandModelRegistryObj::beforeGeckoAnimReload);
        
        for (Map.Entry<ResourceLocation, JsonElement> rawModelEntry : pObject.entrySet()) {
            ResourceLocation key = rawModelEntry.getKey();
            StandModelRegistryObj standModel = StandModelRegistry.getRegisteredModel(key);
            GeckoStandAnimator standModelAnims = new GeckoStandAnimator();
            standModelAnims.setExists();
            
            JsonObject modelAnimsJson = rawModelEntry.getValue().getAsJsonObject().getAsJsonObject("animations");
            for (Map.Entry<String, JsonElement> animJsonEntry : modelAnimsJson.entrySet()) {
                try {
                    JsonObject animJson = animJsonEntry.getValue().getAsJsonObject();
                    Animation animation = ParseGeckoAnims.parseAnim(animJson);
                    standModelAnims.animFromJson(animation, animJson, animJsonEntry.getKey());
                }
                catch (Exception e) {
                    LOGGER.error("Failed to load animation {} from {}", animJsonEntry.getKey(), rawModelEntry.getKey(), e);
                    continue;
                }
            }
            loadedAnims.put(key, standModelAnims);
            
            standModelAnims.onLoad();
            if (standModel != null) {
                standModel.onGeckoAnimLoaded(standModelAnims);
            }
        }
    }
    
    
//    @Nullable
//    public StandActionAnimation getAnim(ResourceLocation modelId, String animName) {
//        GeckoStandAnimator modelAnims = loadedAnims.get(modelId);
//        return modelAnims != null ? modelAnims.getNamedAnim(animName) : null;
//    }
    

}
