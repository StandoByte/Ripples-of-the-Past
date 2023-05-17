package com.github.standobyte.jojo.util.mc.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.command.GenStandStatsCommand;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.StandStatsDataPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandStatsDataPacket.StandStatsDataEntry;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.FileUtil;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.registries.IForgeRegistry;

public class StandStatsManager extends JsonReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String RESOURCE_NAME = "stand_stats";
    
    private static StandStatsManager instance = null;
    
    private Map<StandType<?>, StandStats> overridenStats = new HashMap<>();

    private StandStatsManager() {
        super(GSON, RESOURCE_NAME);
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
        Map<StandType<?>, StandStats> stats = new HashMap<>();
        
        IForgeRegistry<StandType<?>> registry = JojoCustomRegistries.STANDS.getRegistry();
        resourceList.forEach((location, object) -> {
            if (registry.containsKey(location)) {
                StandType<?> stand = registry.getValue(location);
                if (stand != null) {
                    try {
                        JsonObject jsonObject = JSONUtils.convertToJsonObject(object, RESOURCE_NAME);
                        stats.put(stand, GSON.fromJson(jsonObject, stand.getStatsClass()));
                        // FIXME can i also update all summoned stand entities' data parameters?
                    }
                    catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                        LOGGER.error("Parsing error loading custom stand stats {}: {}", location, jsonparseexception.getMessage());
                    }
                }
            }
        });
        
        this.overridenStats = stats;
        
        JojoModConfig.getCommonConfigInstance(false).onStatsDataPackLoad();
    }
    
    public static Path getPackFolderPath(MinecraftServer server) {
        return server.getWorldPath(FolderName.DATAPACK_DIR).resolve(GenStandStatsCommand.PACK_NAME);
    }

    private final Set<Path> savedStatsWorlds = new HashSet<>();
    public void writeDefaultStandStats(ServerWorld world) throws StatsStatsSaveException {
        Path folderPath = getPackFolderPath(world.getServer());
        if (!savedStatsWorlds.contains(folderPath)) {
            Path dataFolderPath = folderPath.resolve("data");
            Set<Map.Entry<RegistryKey<StandType<?>>, StandType<?>>> standTypes = JojoCustomRegistries.STANDS.getRegistry().getEntries();
            for (Map.Entry<RegistryKey<StandType<?>>, StandType<?>> entry : standTypes) {
                Path jsonFilePath = getJsonPath(dataFolderPath, entry.getKey().location());
                File jsonFile = jsonFilePath.toFile();
                if (jsonFile.getParentFile() != null) {
                    jsonFile.getParentFile().mkdirs();
                }
                try (OutputStream outputStream = new FileOutputStream(jsonFile);
                    Writer writer = new OutputStreamWriter(outputStream, Charsets.UTF_8.newEncoder())) {
                    GSON.toJson(entry.getValue().getDefaultStats(), writer);
                } catch (IOException e) {
                    LOGGER.error("Couldn't save default stand stats to {}", jsonFile, e);
                    throw new StatsStatsSaveException("Couldn't save default stand stats of " + entry.getKey().location());
                }
            }
            savedStatsWorlds.add(folderPath);
            try {
                Files.write(
                        "{\n" + 
                        "  \"pack\": {\n" + 
                        "    \"pack_format\": 6,\n" + 
                        "    \"description\": \"Stand stats for Ripples of the Past mod\"\n" + 
                        "  }\n" + 
                        "}", 
                        folderPath.resolve("pack.mcmeta").toFile(), 
                        StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOGGER.error("Couldn't write pack.mcmeta file for Stand stats", e);
                throw new StatsStatsSaveException("Couldn't write pack.mcmeta file for Stand stats");
            }

            try (InputStream inputStream = JojoMod.class.getResourceAsStream("/statsreadme.txt")) {
                File dataPackFile = folderPath.resolve("readme.txt").toFile();
                Files.asByteSink(dataPackFile).writeFrom(inputStream);
                inputStream.close();
            } catch (IOException e) {
                LOGGER.error("Couldn't write readme.txt file for Stand stats", e);
                throw new StatsStatsSaveException("Couldn't write readme.txt file for Stand stats");
            }
        }
    }
    
    private Path getJsonPath(Path mainFolder, ResourceLocation standLocation) {
        String standNamespace = standLocation.getNamespace();
        String standPath = standLocation.getPath();
        if (standPath.contains("//")) {
            throw new ResourceLocationException("Invalid resource path: " + standLocation);
        }
        Path jsonFile;
        try {
            Path modIdStatsPath = mainFolder.resolve(standNamespace).resolve(RESOURCE_NAME);
            jsonFile = FileUtil.createPathToResource(modIdStatsPath, standPath, ".json");
        }
        catch (InvalidPathException e) {
            throw new ResourceLocationException("Invalid resource path: " + standLocation, e);
        }
        if (jsonFile.startsWith(mainFolder)) {
            return jsonFile;
        }
        else {
            throw new ResourceLocationException("Invalid resource path: " + jsonFile);
        }
    }

    public void syncToClients(PlayerList playerList) {
        playerList.getPlayers().forEach(this::syncToClient);
    }
    
    public void syncToClient(ServerPlayerEntity player) {
        PacketManager.sendToClient(new StandStatsDataPacket(overridenStats), player);
    }
    
    public void clSetStats(Iterable<StandStatsDataEntry> stats) {
        IForgeRegistry<StandType<?>> registry = JojoCustomRegistries.STANDS.getRegistry();
        Map<StandType<?>, StandStats> map = new HashMap<>();
        stats.forEach(entry -> {
            StandType<?> stand = registry.getValue(entry.getStandTypeLocation());
            if (stand != null) {
                map.put(stand, entry.getStats());
            }
        });
        this.overridenStats = map;
    }
    
    public <T extends StandStats> T getStats(StandType<T> stand) {
        if (overridenStats.containsKey(stand)) {
            return (T) overridenStats.get(stand);
        }
        return stand.getDefaultStats();
    }
    
    public static class StatsStatsSaveException extends Exception {
        
        private StatsStatsSaveException(String message) {
            super(message);
        }
    }
}
