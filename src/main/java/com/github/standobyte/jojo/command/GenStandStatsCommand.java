package com.github.standobyte.jojo.command;

import java.nio.file.Path;

import com.github.standobyte.jojo.util.mc.data.StandStatsManager;
import com.github.standobyte.jojo.util.mc.data.StandStatsManager.StatsStatsSaveException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.storage.FolderName;

public class GenStandStatsCommand {
    public static final String PACK_NAME = "standstatspack";

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("genstandstats").requires(ctx -> ctx.hasPermission(2))
                .executes(ctx -> genStandStatsDataPack(ctx.getSource())));
        JojoCommandsCommand.addCommand("genstandstats");
    }
    
    private static int genStandStatsDataPack(CommandSource source) throws CommandSyntaxException {
        try {
            StandStatsManager.getInstance().writeDefaultStandStats(source.getLevel());
        } catch (StatsStatsSaveException e) {
            SimpleCommandExceptionType exceptionType = new SimpleCommandExceptionType(new StringTextComponent(e.getMessage()));
            throw exceptionType.create();
        }

        Path dataPackFolder = source.getServer().getWorldPath(FolderName.DATAPACK_DIR).resolve(GenStandStatsCommand.PACK_NAME);
        source.sendSuccess(new TranslationTextComponent("commands.genstandstats.success", 
                new StringTextComponent(PACK_NAME).withStyle(TextFormatting.UNDERLINE).withStyle((style) -> {
                    return style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, dataPackFolder.normalize().toString()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                                    new TranslationTextComponent("commands.genstandstats.folder_link", 
                                            new StringTextComponent("datapacks").withStyle(TextFormatting.ITALIC)
                                            )));
                }))
                .withStyle(TextFormatting.GRAY), true);
        source.sendSuccess(new TranslationTextComponent("commands.genstandstats.hint", 
                new StringTextComponent("readme.txt").withStyle(TextFormatting.ITALIC))
                .withStyle(TextFormatting.GRAY), false);
        
        return 1;
    }
}
