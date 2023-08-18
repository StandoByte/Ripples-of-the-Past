package com.github.standobyte.jojo.command.configpack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FileUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.storage.FolderName;

public interface IDataConfig {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    LiteralArgumentBuilder<CommandSource> commandRegister(LiteralArgumentBuilder<CommandSource> builder, String literal);
    
    default String getDataPackName() {
        return "jojoconfig";
    }
    
    default Path dataPackPath(MinecraftServer server) {
        return server.getWorldPath(FolderName.DATAPACK_DIR).resolve(getDataPackName());
    }
    
    default boolean genDataPackBase(CommandSource src) throws IOException {
        boolean generatedPack = false;
        Path packPath = dataPackPath(src.getServer());
        if (!java.nio.file.Files.exists(packPath)) {
            packPath.toFile().mkdirs();
            generatedPack = true;
        }

        Path mcmetaPath = packPath.resolve("pack.mcmeta");
        if (!java.nio.file.Files.exists(mcmetaPath)) {
            try {
                com.google.common.io.Files.write(
                        "{\n" + 
                                "  \"pack\": {\n" + 
                                "    \"pack_format\": 6,\n" + 
                                "    \"description\": \"Config data pack for Ripples of the Past mod\"\n" + 
                                "  }\n" + 
                                "}", 
                                mcmetaPath.toFile(), 
                                StandardCharsets.UTF_8);
                generatedPack = true;
            }
            catch (IOException e) {
                LOGGER.error("Couldn't write pack.mcmeta file for Ripples of the Past config data pack", e);
                throw e;
            }
        }
        
        if (generatedPack) {
            src.sendSuccess(new TranslationTextComponent("commands.jojoconfigpack.base_created", 
                    new TranslationTextComponent("commands.jojoconfigpack.base_created.link_name").withStyle(TextFormatting.UNDERLINE).withStyle((style) -> {
                        return style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, packPath.normalize().toString()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                                        new TranslationTextComponent("commands.jojoconfigpack.folder_link.tooltip", 
                                                new StringTextComponent("datapacks").withStyle(TextFormatting.ITALIC)
                                                )));
                    }),
                    new StringTextComponent(getDataPackName()).withStyle(TextFormatting.ITALIC))
                    .withStyle(TextFormatting.GRAY), true);
        }
        return generatedPack;
    }
    
    default void genJsonFromObj(Object object, ResourceLocation resourcePath, String resourceName, MinecraftServer server) throws JsonWriteException {
        Path folderPath = dataPackPath(server);
        
        Path dataFolderPath = folderPath.resolve("data");
        Path jsonFilePath = getJsonPath(dataFolderPath, resourcePath, resourceName);
        File jsonFile = jsonFilePath.toFile();
        if (jsonFile.getParentFile() != null) {
            jsonFile.getParentFile().mkdirs();
        }
        try (OutputStream outputStream = new FileOutputStream(jsonFile);
            Writer writer = new OutputStreamWriter(outputStream, Charsets.UTF_8.newEncoder())) {
            getGson().toJson(object, writer);
        } catch (IOException e) {
            JsonWriteException exception = new JsonWriteException(jsonFile);
            exception.initCause(e);
            throw exception;
        }
    }
    
    public static Path getJsonPath(Path mainFolder, ResourceLocation standLocation, String resourceName) {
        String standNamespace = standLocation.getNamespace();
        String standPath = standLocation.getPath();
        if (standPath.contains("//")) {
            throw new ResourceLocationException("Invalid resource path: " + standLocation);
        }
        Path jsonFile;
        try {
            Path modIdStatsPath = mainFolder.resolve(standNamespace).resolve(resourceName);
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
    
    
    
    void syncToClient(ServerPlayerEntity player);
    
    default Gson getGson() {
        return GSON;
    }
    
    
    
    public static class JsonWriteException extends Exception {
        public final File jsonFilePath;
        
        private JsonWriteException(File jsonFilePath) {
            this.jsonFilePath = jsonFilePath;
        }
    }
}
