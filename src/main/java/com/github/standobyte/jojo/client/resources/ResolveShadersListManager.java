package com.github.standobyte.jojo.client.resources;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class ResolveShadersListManager extends ReloadListener<Map<ResourceLocation, JsonArray>> {
    private static final ResourceLocation DEFAULT_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "default");
    private static final ResourceLocation NO_STAND_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "no_stand");
    private List<ResourceLocation> shadersDefault;
    private List<ResourceLocation> shadersNoStand;
    private Map<ResourceLocation, List<ResourceLocation>> shadersOverrides = new HashMap<>();

    @Override
    protected Map<ResourceLocation, JsonArray> prepare(IResourceManager resourceManager, IProfiler preparationsProfiler) {
        Map<ResourceLocation, JsonArray> map = Maps.newHashMap();
        map.put(DEFAULT_LOCATION, readArray(resourceManager, DEFAULT_LOCATION));
        map.put(NO_STAND_LOCATION, readArray(resourceManager, NO_STAND_LOCATION));

        JojoCustomRegistries.STANDS.getRegistry().getKeys().forEach(stand -> {
            JsonArray jsonArray = readArray(resourceManager, stand);
            if (jsonArray != null) {
                map.put(stand, jsonArray);
            }
        });
        return map;
    }

    @Nullable
    private JsonArray readArray(IResourceManager resourceManager, ResourceLocation location) {
        location = new ResourceLocation(location.getNamespace(), "shaders/resolve/" + location.getPath() + ".json");
        if (resourceManager.hasResource(location)) {
            try (
                    IResource resource = resourceManager.getResource(location);
                    Reader reader = new InputStreamReader(resource.getInputStream(), Charsets.UTF_8);) {
                return JSONUtils.getAsJsonArray(JSONUtils.parse(reader), "shaders", null);
            } catch (IOException e) {
                return null;
            }
        }
        else {
            return null;
        }
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonArray> arrays, IResourceManager resourceManager,
            IProfiler reloadProfiler) {
        shadersDefault = toResourceLocationList(arrays.get(DEFAULT_LOCATION));
        arrays.remove(DEFAULT_LOCATION);
        shadersNoStand = toResourceLocationList(arrays.get(NO_STAND_LOCATION));
        arrays.remove(DEFAULT_LOCATION);

        shadersOverrides.clear();
        arrays.forEach((stand, jsonArray) -> shadersOverrides.put(stand, toResourceLocationList(jsonArray)));
    }

    private List<ResourceLocation> toResourceLocationList(JsonArray jsonArray) {
        return jsonArray == null ? Collections.emptyList() : Streams.stream(jsonArray)
                .map(name -> JSONUtils.convertToString(name, "shader") + ".json")
                .map(ResourceLocation::new)
                .collect(ImmutableList.toImmutableList());
    }



    public List<ResourceLocation> getShadersImmutableList(ResourceLocation standName) {
        return shadersOverrides.getOrDefault(standName, shadersDefault);
    }

    @Nullable
    public ResourceLocation getRandomShader(IStandPower standPower, Random random) {
        List<ResourceLocation> shaders = standPower.hasPower() ? getShadersImmutableList(standPower.getType().getRegistryName()) : shadersNoStand;
        if (shaders.isEmpty()) {
            return null;
        }
        return shaders.get(random.nextInt(shaders.size()));
    }
}
