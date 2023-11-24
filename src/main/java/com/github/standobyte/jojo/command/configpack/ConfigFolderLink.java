package com.github.standobyte.jojo.command.configpack;

import java.io.IOException;
import java.nio.file.Path;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class ConfigFolderLink implements IDataConfig {
    private static ConfigFolderLink instance;
    
    public static ConfigFolderLink init() {
        if (instance == null) {
            instance = new ConfigFolderLink();
        }
        return instance;
    }
    
    public static ConfigFolderLink getInstance() {
        return instance;
    }
    
    @Override
    public LiteralArgumentBuilder<CommandSource> commandRegister(LiteralArgumentBuilder<CommandSource> builder, String literal) {
        return builder.then(Commands.literal(literal)
                .executes(ctx -> sendDataPackLink(ctx.getSource()))
                );
    }
    
    private int sendDataPackLink(CommandSource src) throws CommandSyntaxException {
        try {
            if (genDataPackBase(src)) {
                return 1;
            }
            else {
                Path packPath = dataPackPath(src.getServer());
                src.sendSuccess(new TranslationTextComponent("commands.jojoconfigpack.folder_link", 
                        new StringTextComponent(getDataPackName()).withStyle(TextFormatting.ITALIC)).withStyle(TextFormatting.UNDERLINE).withStyle((style) -> {
                            return style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, packPath.normalize().toString()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                                            new TranslationTextComponent("commands.jojoconfigpack.folder_link.tooltip", 
                                                    new StringTextComponent("datapacks").withStyle(TextFormatting.ITALIC)
                                                    )));
                        })
                        .withStyle(TextFormatting.GRAY), true);
                return 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
            SimpleCommandExceptionType exceptionType = new SimpleCommandExceptionType(new StringTextComponent(e.getMessage()));
            throw exceptionType.create();
        }
    }
    
    @Override
    public void syncToClient(ServerPlayerEntity player) {}
}
