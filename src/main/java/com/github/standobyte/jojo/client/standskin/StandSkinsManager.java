package com.github.standobyte.jojo.client.standskin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ResourcePathChecker;
import com.github.standobyte.jojo.client.resources.CustomResources;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Color;

public class StandSkinsManager extends ReloadListener<Map<ResourceLocation, StandSkin>> {
    private Map<ResourceLocation, StandSkin> skins = new HashMap<>();
    
    private Map<ResourceLocation, List<StandSkin>> skinsByStand = new HashMap<>();
    private static final List<StandSkin> EMPTY = ImmutableList.of();
    
    @Nullable
    public StandSkin getStandSkin(StandType<?> standType, @Nullable ResourceLocation skinId) {
        StandSkin skin = null;
        if (skinId != null) {
            skin = skins.get(skinId);
        }
        if (skin == null) {
            skin = skins.get(standType.getRegistryName());
        }
        return skin;
    }
    
    public List<StandSkin> getStandSkinsView(ResourceLocation standId) {
        return skinsByStand.getOrDefault(standId, EMPTY);
    }
    
    
    
    public static StandSkinsManager getInstance() {
        return CustomResources.getStandSkinsLoader();
    }
    
    public static int getUiColor(IStandPower standPower) {
        return standPower.getStandInstance().map(StandSkinsManager::getUiColor).orElse(-1);
    }
    
    public static int getUiColor(StandInstance standInstance) {
        Optional<StandSkin> resourceSkin = standInstance.getSelectedSkin()
                .flatMap(skinId -> Optional.ofNullable(getInstance().getStandSkin(standInstance.getType(), skinId)));
        
        return resourceSkin.map(skin -> skin.color).orElse(standInstance.getType().getColor());
    }
    
    
    
    // JSON deserialization below
    
    private static final JsonParser PARSER = new JsonParser();

    @Override
    protected Map<ResourceLocation, StandSkin> prepare(IResourceManager resourceManager, IProfiler profiler) {
        Map<ResourceLocation, StandSkin> skinsMap = new HashMap<>();
        profiler.startTick();

        for (String namespace : resourceManager.getNamespaces()) {
            profiler.push(namespace);

            try {
                for (IResource definition : resourceManager.getResources(new ResourceLocation(namespace, "jojo_stand_skins.json"))) {
                    profiler.push(definition.getSourceName());

                    try (
                            InputStream inputstream = definition.getInputStream();
                            Reader reader = new InputStreamReader(inputstream, StandardCharsets.UTF_8);
                            ) {
                        profiler.push("parse");
                        
                        JsonObject skinsDefinitionJson = PARSER.parse(reader).getAsJsonObject().getAsJsonObject("skins");
                        for (Map.Entry<String, JsonElement> skinEntry : skinsDefinitionJson.entrySet()) {
                            JsonObject skinJson = skinEntry.getValue().getAsJsonObject();
                            ResourceLocation skinId = new ResourceLocation(skinEntry.getKey());
                            ResourceLocation standType = new ResourceLocation(skinJson.get("stand_type").getAsString());
                            int color = parseColor(skinJson.get("color"));
                            
                            StandSkin skin = new StandSkin(skinId, standType, color);
                            skinsMap.put(skinId, skin);
                        }
                        
                        profiler.popPush("register");

//                        for(Entry<String, SoundList> entry : map.entrySet()) {
//                            soundhandler$loader.handleRegistration(new ResourceLocation(s, entry.getKey()), entry.getValue(), pResourceManager);
//                        }
//
                        profiler.pop();
                    } catch (RuntimeException runtimeexception) {
                        JojoMod.getLogger().warn("Invalid jojo_stand_skins.json in resourcepack: '{}'", definition.getSourceName(), runtimeexception);
                    }

                    profiler.pop();
                }
            } catch (IOException ioexception) {
            }

            profiler.pop();
        }

        profiler.endTick();
        return skinsMap;
    }
    
    private static int parseColor(JsonElement jsonElement) throws JsonParseException {
        if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
            if (primitive.isString()) {
                String str = primitive.getAsString();
                try {
                    int numeric = Integer.decode(str);
                    return numeric & 0xFFFFFF;
                }
                catch (NumberFormatException e) {}

                Color mojangColor = Color.parseColor(str);
                if (mojangColor != null) return mojangColor.getValue();
            }
        }
        
        else if (jsonElement.isJsonArray()) {
            JsonArray array = jsonElement.getAsJsonArray();
            if (array.size() == 3) {
                int r = array.get(0).getAsInt() & 0xFF;
                int g = array.get(1).getAsInt() & 0xFF;
                int b = array.get(2).getAsInt() & 0xFF;
                return (r << 16) | (g << 8) | b;
            }
        }
        
        throw new JsonParseException("Couldn't parse color");
    }

    @Override
    protected void apply(Map<ResourceLocation, StandSkin> skinsMap, IResourceManager resourceManager, IProfiler profiler) {
        JojoCustomRegistries.STANDS.getRegistry().getValues().forEach(standType -> {
            ResourceLocation id = standType.getRegistryName();
            skinsMap.computeIfAbsent(id, s -> new StandSkin(id, id, standType.getColor()));
        });
        this.skins = skinsMap;
        this.skinsByStand = skinsMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.groupingBy(entry -> entry.standTypeId, 
                        toSortedList(Comparator.comparing(skin -> skin.resLoc, SKINS_ID_ORDER))));
        
        
//        pObject.apply(this.registry, this.soundEngine);
//
//        for(ResourceLocation resourcelocation : this.registry.keySet()) {
//            SoundEventAccessor soundeventaccessor = this.registry.get(resourcelocation);
//            if (soundeventaccessor.getSubtitle() instanceof TranslationTextComponent) {
//                String s = ((TranslationTextComponent)soundeventaccessor.getSubtitle()).getKey();
//                if (!I18n.exists(s)) {
//                    LOGGER.debug("Missing subtitle {} for event: {}", s, resourcelocation);
//                }
//            }
//        }
//
//        if (LOGGER.isDebugEnabled()) {
//            for(ResourceLocation resourcelocation1 : this.registry.keySet()) {
//                if (!Registry.SOUND_EVENT.containsKey(resourcelocation1)) {
//                    LOGGER.debug("Not having sound event for: {}", (Object)resourcelocation1);
//                }
//            }
//        }
//
//        this.soundEngine.reload();
    }
    
    private static <T> Collector<T, ?, List<T>> toSortedList(Comparator<? super T> c) {
        return Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(c)), 
                ArrayList::new);
    }
    
    private static final Comparator<ResourceLocation> SKINS_ID_ORDER = (rl1, rl2) -> {
        boolean fromMainMod1 = JojoMod.MOD_ID.equals(rl1.getNamespace());
        boolean fromMainMod2 = JojoMod.MOD_ID.equals(rl2.getNamespace());
        if (fromMainMod1 ^ fromMainMod2) return fromMainMod1 ? -1 : 1;
        return rl1.compareTo(rl2);
    };
    
    
    
    private Map<ResLocPair, ResourcePathChecker> pathCheckCache = new HashMap<>();
    
    public static ResourceLocation getPathRemapped(StandSkin standSkin, ResourceLocation orDefault) {
        return getPathRemapped(standSkin.getNonDefaultLocation(), orDefault);
    }
    
    public static ResourceLocation getPathRemapped(Optional<ResourceLocation> standSkinPath, ResourceLocation orDefault) {
        return standSkinPath.map(skin -> StandSkinsManager.getInstance().getPathChecker(skin, orDefault)
                .or(orDefault))
                .orElse(orDefault);
    }
    
    public ResourcePathChecker getPathChecker(ResourceLocation skinPath, ResourceLocation originalResPath) {
        return pathCheckCache.computeIfAbsent(new ResLocPair(skinPath, originalResPath), pair -> {
            ResourceLocation pathRemapped = pathRemapFunc(skinPath, originalResPath);
            return ResourcePathChecker.create(pathRemapped);
        });
    }
    
    public static ResourceLocation pathRemapFunc(ResourceLocation skinPath, ResourceLocation originalResPath) {
        return new ResourceLocation(
                skinPath.getNamespace(), 
                "stand_skins/" + skinPath.getPath() + "/assets/" + originalResPath.getNamespace() + "/" + originalResPath.getPath()
                );
    }
    
    private static class ResLocPair {
        private final ResourceLocation left;
        private final ResourceLocation right;
        
        public ResLocPair(ResourceLocation left, ResourceLocation right) {
            this.left = left;
            this.right = right;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj instanceof ResLocPair) {
                ResLocPair other = (ResLocPair) obj;
                return this.left.equals(other.left) && this.right.equals(other.right);
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return left.hashCode() ^ right.hashCode();
        }
    }
}
