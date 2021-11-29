package com.github.standobyte.jojo.command;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class JojoControlsCommand {
    public static final String LITERAL = "jojocontrols";
    private static final ITextComponent[] TEXT_PAGES = { 
            new TranslationTextComponent("chat.command.controls.overlay", 
                    new KeybindTextComponent("jojo.key.switch_mode").withStyle(TextFormatting.ITALIC)).withStyle(TextFormatting.GRAY),
            new TranslationTextComponent("chat.command.controls.overlay.scroll", 
                    new KeybindTextComponent("jojo.key.scroll_attack").withStyle(TextFormatting.ITALIC),
                    new KeybindTextComponent("jojo.key.scroll_ability").withStyle(TextFormatting.ITALIC),
                    new KeybindTextComponent("key.sneak").withStyle(TextFormatting.ITALIC)).withStyle(TextFormatting.GRAY),
            new TranslationTextComponent("chat.command.controls.overlay.use", 
                    new KeybindTextComponent("key.attack").withStyle(TextFormatting.ITALIC),
                    new KeybindTextComponent("key.use").withStyle(TextFormatting.ITALIC)).withStyle(TextFormatting.GRAY),
            new TranslationTextComponent("chat.command.controls.stand", 
                    new KeybindTextComponent("jojo.key.toggle_stand").withStyle(TextFormatting.ITALIC),
                    new KeybindTextComponent("jojo.key.stand_remote_control").withStyle(TextFormatting.ITALIC)).withStyle(TextFormatting.GRAY)
    };
    private static final ITextComponent[] LAST_PAGE_VARIANTS = {
            new TranslationTextComponent("chat.command.controls.changeable", new TranslationTextComponent("chat.command.controls.keep_off")).withStyle(TextFormatting.GRAY),
            new TranslationTextComponent("chat.command.controls.changeable", new TranslationTextComponent("chat.command.controls.keep_stand_off")).withStyle(TextFormatting.GRAY),
            new TranslationTextComponent("chat.command.controls.changeable", new TranslationTextComponent("chat.command.controls.keep_non_stand_off")).withStyle(TextFormatting.GRAY),
            new TranslationTextComponent("chat.command.controls.changeable").withStyle(TextFormatting.GRAY)
    };

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal(LITERAL).executes((ctx -> {
            return writePage(0, ctx);
        })).then(Commands.argument("page", IntegerArgumentType.integer(1, Integer.MAX_VALUE)).executes((ctx -> {
            return writePage(IntegerArgumentType.getInteger(ctx, "page"), ctx);
        }))));
    }

    private static int writePage(int page, CommandContext<CommandSource> ctx) {
        page = MathHelper.clamp(page, 1, TEXT_PAGES.length + 1);
        IFormattableTextComponent text = new TranslationTextComponent("chat.command.controls.page", String.valueOf(page), TEXT_PAGES.length + 1).withStyle(TextFormatting.DARK_GRAY);
        text.append(getPage(page - 1));
        if (page <= TEXT_PAGES.length) {
            final int pageNext = page + 1;
            text.append(new TranslationTextComponent("chat.command.controls.next_page").withStyle((style) -> {
                return style.withColor(TextFormatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + JojoControlsCommand.LITERAL + " " + pageNext))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("chat.controls.tooltip")));
            }));
        }
        ctx.getSource().sendSuccess(text, false);
        return 0;
    }
    
    private static final ITextComponent getPage(int pageNum) {
        if (pageNum >= TEXT_PAGES.length) {
            byte i = 0;
            if (JojoModConfig.COMMON.keepNonStandOnDeath.get()) {
                i |= 1;
            }
            if (JojoModConfig.COMMON.keepStandOnDeath.get()) {
                i |= 2;
            }
            JojoMod.LOGGER.debug(i);
            return LAST_PAGE_VARIANTS[i];
        }
        return TEXT_PAGES[pageNum];
    }
}
