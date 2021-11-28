package com.github.standobyte.jojo.command;

import java.util.Collection;

import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class StandCommand {
    private static final DynamicCommandExceptionType GIVE_SINGLE_EXCEPTION = new DynamicCommandExceptionType(
            player -> new TranslationTextComponent("commands.stand.give.failed.single", player));
    private static final DynamicCommandExceptionType GIVE_MULTIPLE_EXCEPTION = new DynamicCommandExceptionType(
            count -> new TranslationTextComponent("commands.stand.give.failed.multiple", count));
    private static final DynamicCommandExceptionType QUERY_SINGLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            player -> new TranslationTextComponent("commands.stand.query.failed.single", player));
    private static final DynamicCommandExceptionType QUERY_MULTIPLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            count -> new TranslationTextComponent("commands.stand.query.failed.multiple", count));

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("stand").requires(ctx -> ctx.hasPermission(2))
                .then(Commands.literal("give").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("stand", new StandArgument())
                        .executes(ctx -> giveStands(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), StandArgument.getStandType(ctx, "stand"))))))
                .then(Commands.literal("random").then(Commands.argument("targets", EntityArgument.players()) // /stand random <player(s)>
                        .executes(ctx -> giveRandomStands(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets")))))
                .then(Commands.literal("remove").then(Commands.argument("targets", EntityArgument.players())
                        .executes(ctx -> removeStands(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets")))))
                .then(Commands.literal("name").then(Commands.argument("targets", EntityArgument.player())
                        .executes(ctx -> queryStand(ctx.getSource(), EntityArgument.getPlayer(ctx, "targets")))))
                );
    }

    private static int giveStands(CommandSource source, Collection<ServerPlayerEntity> targets, StandType standType) throws CommandSyntaxException {
        int i = 0;
        for (ServerPlayerEntity player : targets) {
            IStandPower power = IStandPower.getStandPowerOptional(player).orElse(null);
            if (power != null) {
                if (power.givePower(standType)) {
                    i++;
                }
                else if (targets.size() == 1) {
                    throw GIVE_SINGLE_EXCEPTION.create(targets.iterator().next().getName());
                }
            }
        }
        if (i == 0) {
            if (targets.size() == 1) {
                throw GIVE_SINGLE_EXCEPTION.create(targets.iterator().next().getName());
            } else {
                throw GIVE_MULTIPLE_EXCEPTION.create(targets.size());
            }
        }
        else {
            if (targets.size() == 1) {
                source.sendSuccess(new TranslationTextComponent(
                        "commands.stand.give.success.single", 
                        new TranslationTextComponent(standType.getTranslationKey()), targets.iterator().next().getDisplayName()), true);
            }
            else {
                source.sendSuccess(new TranslationTextComponent(
                        "commands.stand.give.success.multiple", 
                        new TranslationTextComponent(standType.getTranslationKey()), i), true);
            }
            return i;
        }
    }
    
    private static int giveRandomStands(CommandSource source, Collection<ServerPlayerEntity> targets) throws CommandSyntaxException {
        int i = 0;
        StandType stand = null;
        if (!targets.isEmpty()) {
            for (ServerPlayerEntity player : targets) {
                IStandPower power = IStandPower.getStandPowerOptional(player).orElse(null);
                if (power != null) {
                    stand = StandUtil.randomStandByTier(-1, player, player.getRandom());
                    if (power.givePower(stand)) {
                        i++;
                    }
                    else if (targets.size() == 1) {
                        throw GIVE_SINGLE_EXCEPTION.create(targets.iterator().next().getName());
                    }
                }
            }
        }
        if (i == 0) {
            if (targets.size() == 1) {
                throw GIVE_SINGLE_EXCEPTION.create(targets.iterator().next().getName());
            } else {
                throw GIVE_MULTIPLE_EXCEPTION.create(targets.size());
            }
        }
        else {
            if (targets.size() == 1) {
                source.sendSuccess(new TranslationTextComponent("commands.stand.give.success.single", 
                        stand != null ? new TranslationTextComponent(stand.getTranslationKey()) : "", 
                                targets.iterator().next().getDisplayName()), true);
            }
            else {
                source.sendSuccess(new TranslationTextComponent(
                        "commands.stand.give.success.multiple.random", i), true);
            }
            return i;
        }
    }

    private static int removeStands(CommandSource source, Collection<ServerPlayerEntity> targets) throws CommandSyntaxException {
        int i = 0;
        StandType removedStand = null;
        for (ServerPlayerEntity player : targets) {
            IStandPower power = IStandPower.getStandPowerOptional(player).orElse(null);
            if (power != null) {
                StandType toBeRemoved = power.getType();
                if (power.clear()) {
                    i++;
                    removedStand = toBeRemoved;
                }
            }
        }
        if (i == 0) {
            if (targets.size() == 1) {
                throw QUERY_SINGLE_FAILED_EXCEPTION.create(targets.iterator().next().getName());
            } else {
                throw QUERY_MULTIPLE_FAILED_EXCEPTION.create(targets.size());
            }
        } else {
            if (targets.size() == 1) {
                source.sendSuccess(new TranslationTextComponent("commands.stand.remove.success.single", 
                        removedStand != null ? new TranslationTextComponent(removedStand.getTranslationKey()) : "", 
                                targets.iterator().next().getDisplayName()), true);
            } else {
                source.sendSuccess(new TranslationTextComponent("commands.stand.remove.success.multiple", i), true);
            }
            return i;
        }
    }

    private static int queryStand(CommandSource source, ServerPlayerEntity player) throws CommandSyntaxException {
        IStandPower power = IStandPower.getStandPowerOptional(player).orElse(null);
        if (power != null) {
            if (power.hasPower()) {
                source.sendSuccess(new TranslationTextComponent("commands.stand.query.success", player.getDisplayName(), new TranslationTextComponent(power.getType().getTranslationKey())), false);
                return 1;
            }
        }
        throw QUERY_SINGLE_FAILED_EXCEPTION.create(player.getName());
    }
}
