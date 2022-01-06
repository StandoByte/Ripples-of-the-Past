package com.github.standobyte.jojo.util.data;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.power.stand.stats.StandStatsV2;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.registries.IForgeRegistry;

public class StandStatsManager extends JsonReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final FolderName outputDir = FolderName.GENERATED_DIR;
    
    private static StandStatsManager instance = null;

    private StandStatsManager() {
        super(GSON, "stand_stats");
    }
    
    public static void init() {
        if (instance == null) {
            instance = new StandStatsManager();
        }
    }
    
    public static StandStatsManager getInstance() {
        return instance;
    }
    
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, IResourceManager resourceManager, IProfiler profiler) {
        IForgeRegistry<StandType<?>> registry = ModStandTypes.Registry.getRegistry();
        resourceList.forEach((location, object) -> {
            if (registry.containsKey(location)) {
                StandType<?> stand = registry.getValue(location);
                if (stand != null) {
                    try {
                        JsonObject jsonObject = JSONUtils.convertToJsonObject(object, "stand stats");
                        // FIXME override stats
//                        stand.setStats(StandStatsV2.changeStatsFromJson(stand.getDefaultStats(), jsonObject));
                        // FIXME sync to client
                        // FIXME can i also update all summoned stand entities' data parameters?
                    }
                    catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                        LOGGER.error("Parsing error loading custom stand stats {}: {}", location, jsonparseexception.getMessage());
                    }
                }
            }
        });
        registry.getEntries().forEach(entry -> {
            outputJsonFile(entry.getKey().location(), entry.getValue().getStats());
        });
        // FIXME readme.txt
    }
    
    private void outputJsonFile(ResourceLocation standLocation, StandStatsV2 stats) {
        // FIXME output as full /<world>/generated/<namespace>/<path>.json files
//        Path saveFileDir = _.getLevelPath(outputDir).normalize();
//        if (saveFileDir.toFile().isDirectory()) {
//            Files.write(GSON.toJson(stats), outputPath.crea, StandardCharsets.UTF_8);
//        }
    }
}
