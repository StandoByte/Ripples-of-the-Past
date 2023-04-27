package com.github.standobyte.jojo.command;

import java.util.HashSet;
import java.util.Set;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class JojoCommandsCommand {
    private static final Set<String> COMMANDS = new HashSet<>();
    private static IFormattableTextComponent finalText = new StringTextComponent("");

    public static void addCommand(String literal) {
        COMMANDS.add(literal);
        finalText = 
                COMMANDS.stream()
                .sorted()
                .map(lit-> {
                    String command = "/" + lit;
                    return (IFormattableTextComponent) new TranslationTextComponent("jojo.command_description",  
                            new StringTextComponent(command)
                            .withStyle(style -> {
                                return style.withColor(TextFormatting.GREEN)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(command)));
                            }), 
                            new TranslationTextComponent("jojo.command.desc." + lit));
                })
                .reduce((line1, line2) -> line1.append("\n").append(line2))
                .orElse(finalText);
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("jojocommands").executes((ctx -> {
            return writeContents(ctx);
        })));
    }

    private static int writeContents(CommandContext<CommandSource> ctx) {
        ctx.getSource().sendSuccess(finalText, false);
        return 0;
    }
}
