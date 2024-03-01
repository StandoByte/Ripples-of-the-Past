package com.github.standobyte.jojo.itemtracking.tmp;

import com.github.standobyte.jojo.itemtracking.itemcap.TrackerItemStack;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TestTrackItemCommand {
    public static final SimpleCommandExceptionType ERROR_NOT_LIVING = new SimpleCommandExceptionType(new StringTextComponent("Invalid entity"));
    public static final SimpleCommandExceptionType ERROR_EMPTY_ITEM = new SimpleCommandExceptionType(new StringTextComponent("The entity is not holding any item in main hand"));
    public static final SimpleCommandExceptionType ERROR_ITEM_MULTIPLE = new SimpleCommandExceptionType(new StringTextComponent("Only stacks with size of 1 are supported"));
    public static final SimpleCommandExceptionType ERROR_TRACKING = new SimpleCommandExceptionType(new StringTextComponent("The item is already being tracked, or another error has occured"));
    
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("tmptest_trackhelditem").requires((src) -> {
            return src.hasPermission(2);
        }).then(Commands.argument("target", EntityArgument.entity()).executes((ctx) -> {
            return trackItem(ctx.getSource(), EntityArgument.getEntity(ctx, "target"), ctx.getSource().getPlayerOrException());
        })));
    }
    
    private static int trackItem(CommandSource src, Entity entity, ServerPlayerEntity player) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity)) {
            throw ERROR_NOT_LIVING.create();
        }
        ItemStack item = ((LivingEntity) entity).getItemInHand(Hand.MAIN_HAND);
        if (item.isEmpty()) {
            throw ERROR_EMPTY_ITEM.create();
        }
        try {
            if (TrackerItemStack.setTracked(item, player)) {
                src.sendSuccess(new TranslationTextComponent("%s is now tracking %s in %s's hand", 
                        player.getDisplayName(),
                        item.getDisplayName(), entity.getDisplayName()), true);
                return 1;
            }
            else {
                throw ERROR_TRACKING.create();
            }
        }
        catch (IllegalArgumentException e) {
            throw ERROR_ITEM_MULTIPLE.create();
        }
    }
}
