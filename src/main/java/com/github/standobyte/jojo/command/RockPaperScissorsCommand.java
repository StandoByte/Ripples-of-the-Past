package com.github.standobyte.jojo.command;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.entity.mob.rps.RPSPvpGamesMap;
import com.github.standobyte.jojo.entity.mob.rps.RockPaperScissorsGame;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class RockPaperScissorsCommand {
    public static final String LITERAL = "rockpaperscissors";

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal(LITERAL).then(Commands.argument("target", EntityArgument.player())
                .executes(ctx -> game(ctx.getSource(), EntityArgument.getPlayer(ctx, "target"), ctx))));
        JojoCommandsCommand.addCommand("rockpaperscissors");
    }
    
    private static int game(CommandSource source, ServerPlayerEntity opponent, CommandContext<CommandSource> ctx) {
        Entity entity = source.getEntity();
        if (opponent.is(entity)) {
            ctx.getSource().sendFailure(new TranslationTextComponent("jojo.rps.self"));
            return 0;
        }
        if (entity instanceof ServerPlayerEntity) {
            if (entity.distanceToSqr(opponent) >= 16) {
                ctx.getSource().sendFailure(new TranslationTextComponent("jojo.rps.too_far", opponent.getDisplayName()));
                return 0;
            }
            ServerPlayerEntity player = (ServerPlayerEntity) entity;
            RPSPvpGamesMap games = SaveFileUtilCapProvider.getSaveFileCap(opponent.server).getPvpRPSGames();
            RockPaperScissorsGame game = games.getOrCreateGame(player, opponent);
            game.getPlayer(player).setIsReady(true);
            if (game.getPlayer(opponent).isReady()) {
                player.getCapability(PlayerUtilCapProvider.CAPABILITY).orElseGet(null).setCurrentRockPaperScissorsGame(game);
                opponent.getCapability(PlayerUtilCapProvider.CAPABILITY).orElseGet(null).setCurrentRockPaperScissorsGame(game);
                game.gameStarted(opponent.getLevel());
            }
            else {
                String name = player.getGameProfile().getName();
                String command = "/" + LITERAL + " " + name;
                opponent.sendMessage(new TranslationTextComponent("jojo.rps.game_invite.text", player.getDisplayName(), 
                        new StringTextComponent(command).withStyle(style -> {
                            return style.withColor(TextFormatting.GREEN)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(command)));
                        })), player.getUUID()); 
                ctx.getSource().sendSuccess(new TranslationTextComponent("jojo.rps.game_invite.sent", opponent.getDisplayName()), false);
            }
            return 1;
        }
        return 0;
    }
}
