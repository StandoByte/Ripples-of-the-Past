package com.github.standobyte.jojo.command.configpack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.command.StandArgument;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.StandStatsDataPacket;
import com.github.standobyte.jojo.network.packets.fromserver.StandStatsDataPacket.StandStatsDataEntry;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.power.impl.stand.type.StandType.StandSurvivalGameplayPool;
import com.github.standobyte.jojo.util.general.JsonModUtil;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.IForgeRegistry;

public class StandStatsConfig extends JsonDataConfig {
    private static StandStatsConfig instance;
    private static final String RESOURCE_NAME = "stand_stats";
    
    private Map<StandType<?>, StandStats> overridenStats = new HashMap<>();
    
    public static StandStatsConfig init(IEventBus forgeEventBus) {
        if (instance == null) {
            instance = new StandStatsConfig();
        }
        DataConfigEventHandler.registerEventHandler(instance, forgeEventBus);
        return instance;
    }
    
    public static StandStatsConfig getInstance() {
        return instance;
    }
    
    private StandStatsConfig() {
        super(RESOURCE_NAME);
    }
    
    @Override
    public LiteralArgumentBuilder<CommandSource> commandRegister(LiteralArgumentBuilder<CommandSource> builder, String literal) {
        return builder.then(Commands.literal(literal)
                .executes(ctx -> genAllStandsStats(ctx.getSource()))
                .then(Commands.argument("stand", new StandArgument()).executes(ctx -> genSingleStandStats(ctx.getSource(), StandArgument.getStandType(ctx, "stand"))))
                );
    }
    
    
    
    public <T extends StandStats> T getStats(StandType<T> stand) {
        if (overridenStats.containsKey(stand)) {
            return (T) overridenStats.get(stand);
        }
        return stand.getDefaultStats();
    }
    
    
    
    private int genAllStandsStats(CommandSource source) throws CommandSyntaxException {
        try {
            // generate base datapack
            genDataPackBase(source);
            // generate the .json files
            int count = writeDefaultStandStats(source.getServer(), 
                    JojoCustomRegistries.STANDS.getRegistry().getValues().stream()
                    .filter(stand -> stand.getSurvivalGameplayPool() == StandSurvivalGameplayPool.PLAYER_ARROW)
                    .collect(Collectors.toList()));
            source.sendSuccess(new TranslationTextComponent("commands.jojoconfigpack.standstats.all", 
                    // clickable link to the files (in "jojo" namespace)
                    new TranslationTextComponent("commands.jojoconfigpack.standstats.all.link_name").withStyle(TextFormatting.UNDERLINE).withStyle((style) -> {
                        return style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, 
                                        dataPackPath(source.getServer()).resolve(String.format("data/%s/%s", JojoMod.MOD_ID, RESOURCE_NAME)).normalize().toString()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                                        new TranslationTextComponent("commands.jojoconfigpack.standstats.all.folder_link", 
                                                new StringTextComponent("datapacks").withStyle(TextFormatting.ITALIC)
                                                )));
                    }))
                    .withStyle(TextFormatting.GRAY), true);
            
            return count;
        } catch (Throwable e) {
            SimpleCommandExceptionType exceptionType = new SimpleCommandExceptionType(new StringTextComponent(e.getMessage()));
            throw exceptionType.create();
        }
    }
    
    private int genSingleStandStats(CommandSource source, StandType<?> standType) throws CommandSyntaxException {
        try {
            genDataPackBase(source);
            
            int count = writeDefaultStandStats(source.getServer(), Collections.singletonList(standType));
            source.sendSuccess(new TranslationTextComponent("commands.jojoconfigpack.standstats.single", 
                    new TranslationTextComponent("commands.jojoconfigpack.standstats.single.link_name").withStyle(TextFormatting.UNDERLINE).withStyle((style) -> {
                        return style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, 
                                        dataPackPath(source.getServer()).resolve(String.format("data/%s/%s", standType.getRegistryName().getNamespace(), RESOURCE_NAME)).normalize().toString()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                                        new TranslationTextComponent("commands.jojoconfigpack.standstats.single.folder_link", 
                                                new StringTextComponent("datapacks").withStyle(TextFormatting.ITALIC)
                                                )));
                    }),
                    standType.getName())
                    .withStyle(TextFormatting.GRAY), true);
            
            return count;
        } catch (Throwable e) {
            SimpleCommandExceptionType exceptionType = new SimpleCommandExceptionType(new StringTextComponent(e.getMessage()));
            throw exceptionType.create();
        }
    }
    
    
    
    private int writeDefaultStandStats(MinecraftServer server, Collection<StandType<?>> stands) throws Throwable {
        int i = 0;
        
        for (StandType<?> standType : stands) {
            ResourceLocation key = standType.getRegistryName();
            try {
                genJsonFromObj(standType.getDefaultStats(), key, RESOURCE_NAME, server);
                i++;
            }
            catch (JsonWriteException e) {
                IDataConfig.LOGGER.error("Couldn't save default stand stats to {}", e.jsonFilePath, e.getCause());
                throw e.getCause();
            }
        }

        // and a standstats_readme.txt file
        Path readmePath = dataPackPath(server).resolve(String.format("data/%s/%s/README.txt", JojoMod.MOD_ID, RESOURCE_NAME));
        addReadmeFile(server, readmePath);
        
        return i;
    }
    
    
    
    private void addReadmeFile(MinecraftServer server, Path destPath) {
        try (InputStream inputStream = getReadmeSourcePath(MCUtil.getLanguageCode(server))) {
            if (inputStream == null) {
                IDataConfig.LOGGER.error("Couldn't find readme file for Stand stats");
            }
            File dataPackFile = destPath.toFile();
            Files.asByteSink(dataPackFile).writeFrom(inputStream);
            inputStream.close();
        } catch (IOException e) {
            IDataConfig.LOGGER.error("Couldn't write readme file for Stand stats", e);
        }
    }
    
    private InputStream getReadmeSourcePath(String languageCode) {
        String name = "/assets/jojo/texts/readme_%s_standstats.txt";
        InputStream inputStream = JojoMod.class.getResourceAsStream(String.format(name, languageCode));
        if (!"en_us".equals(languageCode) && inputStream == null) {
            inputStream = JojoMod.class.getResourceAsStream(String.format(name, "en_us"));
        }
        return inputStream;
    }
    
    
    
    // parsing the json files, forming the new map of overriden stats
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, IResourceManager resourceManager, IProfiler profiler) {
        Map<StandType<?>, StandStats> stats = new HashMap<>();
        Gson gson = getGson();
        
        IForgeRegistry<StandType<?>> registry = JojoCustomRegistries.STANDS.getRegistry();
        resourceList.forEach((location, object) -> {
            if (registry.containsKey(location)) {
                StandType<?> stand = registry.getValue(location);
                if (stand != null) {
                    try {
                        JsonObject parsedJson = JSONUtils.convertToJsonObject(object, RESOURCE_NAME);
                        JsonObject statsJson = gson.toJsonTree(stand.getDefaultStats()).getAsJsonObject();
                        JsonModUtil.replaceValues(statsJson, parsedJson);
                        stats.put(stand, gson.fromJson(statsJson, stand.getStatsClass()));
                        // FIXME can i also update all summoned stand entities' data parameters?
                    }
                    catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                        IDataConfig.LOGGER.error("Parsing error loading custom stand stats {}: {}", location, jsonparseexception.getMessage());
                    }
                }
            }
        });
        
        this.overridenStats = stats;
        
        JojoModConfig.getCommonConfigInstance(false).onStatsDataPackLoad();
    }
    
    
    // sending data from server to a player
    @Override
    public void syncToClient(ServerPlayerEntity player) {
        PacketManager.sendToClient(new StandStatsDataPacket(overridenStats), player);
    }
    
    // ...and handling that packet on client
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
}
