package com.github.standobyte.jojo.command;

import com.github.standobyte.jojo.command.configpack.ConfigFolderLink;
import com.github.standobyte.jojo.command.configpack.StandStatsConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class ConfigPackCommand {
    
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> command = Commands.literal("jojoconfigpack").requires(ctx -> ctx.hasPermission(2));
        StandStatsConfig.getInstance().commandRegister(command, "standstats");
        ConfigFolderLink.init().commandRegister(command, "folderlink");
        dispatcher.register(command);
        JojoCommandsCommand.addCommand("jojoconfigpack");
    }
}
