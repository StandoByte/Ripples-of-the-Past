package com.github.standobyte.jojo.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;

public class RockPaperScissorsCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("rockpaperscissors").then(Commands.argument("targets", EntityArgument.player())
                .executes(ctx -> game(ctx.getSource(), EntityArgument.getPlayer(ctx, "targets")))));
    }
    
    private static int game(CommandSource source, ServerPlayerEntity player2) {
    	Entity player1 = source.getEntity();
    	if (player1 == null) {
    		return 0;
    	}
    	
    	return 0;
    }
}
