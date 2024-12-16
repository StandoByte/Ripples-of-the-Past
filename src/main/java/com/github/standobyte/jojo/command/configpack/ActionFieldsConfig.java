package com.github.standobyte.jojo.command.configpack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.command.argument.ActionArgument;
import com.github.standobyte.jojo.command.argument.StandArgument;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.ActionConfigDataPacket;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.google.common.base.Charsets;
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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.IForgeRegistry;

public class ActionFieldsConfig extends JsonDataConfig {
    private static ActionFieldsConfig instance;
    private static final String RESOURCE_NAME = "ability_config";
    
    public static ActionFieldsConfig init(IEventBus forgeEventBus) {
        if (instance == null) {
            instance = new ActionFieldsConfig();
        }
        DataConfigEventHandler.registerEventHandler(instance, forgeEventBus);
        return instance;
    }
    
    public static ActionFieldsConfig getInstance() {
        return instance;
    }
    
    private ActionFieldsConfig() {
        super(RESOURCE_NAME);
    }
    
    @Override
    public LiteralArgumentBuilder<CommandSource> commandRegister(LiteralArgumentBuilder<CommandSource> builder, String literal) {
        return builder.then(Commands.literal(literal)
                .then(Commands.literal("stand").then(Commands.argument("stand_type", new StandArgument())
                        .executes(ctx -> genStandActionsConfig(ctx.getSource(), StandArgument.getStandType(ctx, "stand_type")))))
                .then(Commands.literal("single").then(Commands.argument("ability", new ActionArgument())
                        .executes(ctx -> genActionConfig(ctx.getSource(), ActionArgument.getAction(ctx, "ability")))))
                );
    }
    
    
    
    private int genStandActionsConfig(CommandSource source, StandType<?> standType) throws CommandSyntaxException {
        try {
            // generate base datapack
            genDataPackBase(source);
            // generate the .json files
            int count = writeDefaultActionConfig(source.getServer(), standType.getAllUnlockableActions());
            
            source.sendSuccess(generatePackLink(source, 
                    "commands.jojoconfigpack.abilities.stand", 
                    "commands.jojoconfigpack.abilities.stand.link_name", 
                    LOCAL_FILE_TOOLTIP, 
                    dataPackPath(source.getServer()).resolve(String.format("data/%s/%s", standType.getRegistryName().getNamespace(), RESOURCE_NAME)),
                    standType.getName()), 
                    true);
            
            return count;
        } catch (Throwable e) {
            SimpleCommandExceptionType exceptionType = new SimpleCommandExceptionType(new StringTextComponent(e.getMessage()));
            throw exceptionType.create();
        }
    }
    
    private int genActionConfig(CommandSource source, Action<?> action) throws CommandSyntaxException {
        try {
            genDataPackBase(source);
            
            int count = writeDefaultActionConfig(source.getServer(), Collections.singletonList(action));
            
            source.sendSuccess(generatePackLink(source, 
                    "commands.jojoconfigpack.abilities.single", 
                    "commands.jojoconfigpack.abilities.single.link_name", 
                    LOCAL_FILE_TOOLTIP, 
                    dataPackPath(source.getServer()).resolve(String.format("data/%s/%s", action.getRegistryName().getNamespace(), RESOURCE_NAME)),
                    new StringTextComponent(action.getRegistryName().toString())), 
                    true);
            
            return count;
        } catch (Throwable e) {
            SimpleCommandExceptionType exceptionType = new SimpleCommandExceptionType(new StringTextComponent(e.getMessage()));
            throw exceptionType.create();
        }
    }
    
    
    
    private int writeDefaultActionConfig(MinecraftServer server, Iterable<? extends Action<?>> actions) throws Throwable {
        int i = 0;
        
        for (Action<?> action : actions) {
            ResourceLocation key = action.getRegistryName();
            try {
                if (genJsonFromAction(action, key, RESOURCE_NAME, server)) {
                    i++;
                }
            }
            catch (JsonWriteException e) {
                IDataConfig.LOGGER.error("Couldn't save default action configs to {}", e.jsonFilePath, e.getCause());
                throw e.getCause();
            }
        }

        return i;
    }
    
    public boolean genJsonFromAction(Action<?> action, ResourceLocation resourcePath, String resourceName, MinecraftServer server) throws JsonWriteException {
        Path folderPath = dataPackPath(server);
        
        Path dataFolderPath = folderPath.resolve("data");
        Path jsonFilePath = IDataConfig.getJsonPath(dataFolderPath, resourcePath, resourceName);
        File jsonFile = jsonFilePath.toFile();
        if (jsonFile.getParentFile() != null) {
            jsonFile.getParentFile().mkdirs();
        }
        
        JsonObject json = action.getOrCreateConfigs().defaultSettings;
        
        if (json.size() > 0) {
            try (OutputStream outputStream = new FileOutputStream(jsonFile);
                    Writer writer = new OutputStreamWriter(outputStream, Charsets.UTF_8.newEncoder())) {
                getGson().toJson(json, writer);
            } catch (IOException e) {
                JsonWriteException exception = new IDataConfig.JsonWriteException(jsonFile);
                exception.initCause(e);
                throw exception;
            }
            return true;
        }
        
        return false;
    }
    
    
    
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, IResourceManager resourceManager, IProfiler profiler) {
        this.srvActionsToSync = new ArrayList<>();
        IForgeRegistry<Action<?>> registry = JojoCustomRegistries.ACTIONS.getRegistry();
        resourceList.forEach((location, object) -> {
            if (registry.containsKey(location)) {
                Action<?> action = registry.getValue(location);
                if (action != null) {
                    try {
                        JsonObject parsedJson = JSONUtils.convertToJsonObject(object, RESOURCE_NAME);
                        action.getOrCreateConfigs().applyFromJson(parsedJson);
                        srvActionsToSync.add(action);
                    }
                    catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                        IDataConfig.LOGGER.error("Parsing error loading custom stand stats {}: {}", location, jsonparseexception.getMessage());
                    }
                }
            }
        });
    }
    
    
    @Override
    public void syncToClient(ServerPlayerEntity player) {
        if (srvActionsToSync != null) {
            PacketManager.sendToClient(new ActionConfigDataPacket(srvActionsToSync), player);
        }
    }

    private List<Action<?>> srvActionsToSync;
    private List<Action<?>> clPrevSyncActions = new ArrayList<>();
    public void clHandlePacket(List<Action<?>> actions) {
        for (Action<?> action : actions) {
            clPrevSyncActions.remove(action);
        }
        
        for (Action<?> oldAction : clPrevSyncActions) {
            oldAction.getOrCreateConfigs().restoreFromDefaults();
        }
        
        
        this.clPrevSyncActions = actions;
    }
}
