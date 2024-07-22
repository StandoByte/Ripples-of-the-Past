package com.github.standobyte.jojo.command;

import com.github.standobyte.jojo.command.configpack.ActionFieldsConfig;
import com.github.standobyte.jojo.command.configpack.ConfigFolderLink;
import com.github.standobyte.jojo.command.configpack.StandStatsConfig;
import com.github.standobyte.jojo.command.configpack.standassign.PlayerStandAssignmentConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.eventbus.api.IEventBus;

public class ConfigPackCommand {

    public static void initConfigs(IEventBus eventBus) {
        StandStatsConfig.init(eventBus);
        ActionFieldsConfig.init(eventBus);
        PlayerStandAssignmentConfig.init(eventBus);
    }
    
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> command = Commands.literal("jojoconfigpack").requires(ctx -> ctx.hasPermission(2));
        
        StandStatsConfig.getInstance().commandRegister(command, "stand_stats");
        ActionFieldsConfig.getInstance().commandRegister(command, "ability_config");
        PlayerStandAssignmentConfig.getInstance().commandRegister(command, "assign_stand");
        
        ConfigFolderLink.init().commandRegister(command, "folder_link");
        
        dispatcher.register(command);
        JojoCommandsCommand.addCommand("jojoconfigpack");
    }
}
