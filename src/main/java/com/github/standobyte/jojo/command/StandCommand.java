package com.github.standobyte.jojo.command;

import java.util.Collection;

import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class StandCommand {
    private static final DynamicCommandExceptionType GIVE_SINGLE_EXCEPTION_ALREADY_HAS = new DynamicCommandExceptionType(
            player -> new TranslationTextComponent("commands.stand.give.failed.single.has", player));
    private static final DynamicCommandExceptionType GIVE_MULTIPLE_EXCEPTION_ALREADY_HAVE = new DynamicCommandExceptionType(
            count -> new TranslationTextComponent("commands.stand.give.failed.multiple.have", count));
    private static final DynamicCommandExceptionType GIVE_SINGLE_EXCEPTION_RANDOM = new DynamicCommandExceptionType(
            player -> new TranslationTextComponent("commands.stand.give.failed.single.random", player));
    private static final DynamicCommandExceptionType GIVE_MULTIPLE_EXCEPTION_RANDOM = new DynamicCommandExceptionType(
            count -> new TranslationTextComponent("commands.stand.give.failed.multiple.random", count));
    private static final DynamicCommandExceptionType QUERY_SINGLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            player -> new TranslationTextComponent("commands.stand.query.failed.single", player));
    private static final DynamicCommandExceptionType QUERY_MULTIPLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            count -> new TranslationTextComponent("commands.stand.query.failed.multiple", count));

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("stand").requires(ctx -> ctx.hasPermission(2))
                .then(Commands.literal("give").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("stand", new StandArgument())
                        .executes(ctx -> giveStands(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), StandArgument.getStandType(ctx, "stand"), false))
                        .then(Commands.argument("replace", BoolArgumentType.bool())
                                .executes(ctx -> giveStands(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), StandArgument.getStandType(ctx, "stand"), BoolArgumentType.getBool(ctx, "replace")))))))
                .then(Commands.literal("random").then(Commands.argument("targets", EntityArgument.players()) // /stand random <player(s)>
                        .executes(ctx -> giveRandomStands(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), false))
                        .then(Commands.argument("replace", BoolArgumentType.bool())
                                .executes(ctx -> giveRandomStands(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), BoolArgumentType.getBool(ctx, "replace"))))))
                .then(Commands.literal("clear").then(Commands.argument("targets", EntityArgument.players())
                        .executes(ctx -> removeStands(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets")))))
                .then(Commands.literal("type").then(Commands.argument("targets", EntityArgument.player())
                        .executes(ctx -> queryStand(ctx.getSource(), EntityArgument.getPlayer(ctx, "targets")))))
                );
        JojoCommandsCommand.addCommand("stand");
    }

    private static int giveStands(CommandSource source, Collection<ServerPlayerEntity> targets, StandType<?> standType, boolean replace) throws CommandSyntaxException {
        int i = 0;
        for (ServerPlayerEntity player : targets) {
            IStandPower power = IStandPower.getStandPowerOptional(player).orElse(null);
            if (power != null) {
                if (replace) {
                    power.clear();
                }
                if (power.givePower(standType)) {
                    i++;
                }
                else if (targets.size() == 1) {
                    throw GIVE_SINGLE_EXCEPTION_ALREADY_HAS.create(targets.iterator().next().getName());
                }
            }
        }
        if (i == 0) {
            if (targets.size() == 1) {
                throw GIVE_SINGLE_EXCEPTION_ALREADY_HAS.create(targets.iterator().next().getName());
            } else {
                throw GIVE_MULTIPLE_EXCEPTION_ALREADY_HAVE.create(targets.size());
            }
        }
        else {
            if (targets.size() == 1) {
                source.sendSuccess(new TranslationTextComponent(
                        "commands.stand.give.success.single", 
                        standType.getName(), targets.iterator().next().getDisplayName()), true);
            }
            else {
                source.sendSuccess(new TranslationTextComponent(
                        "commands.stand.give.success.multiple", 
                        standType.getName(), i), true);
            }
            return i;
        }
    }
    
    private static int giveRandomStands(CommandSource source, Collection<ServerPlayerEntity> targets, boolean replace) throws CommandSyntaxException {
        int i = 0;
        StandType<?> stand = null;
        if (!targets.isEmpty()) {
            for (ServerPlayerEntity player : targets) {
                IStandPower power = IStandPower.getStandPowerOptional(player).orElse(null);
                if (power != null) {
                    stand = StandUtil.randomStand(player, player.getRandom());
                    if (stand == null) {
                        if (targets.size() == 1) {
                            throw GIVE_SINGLE_EXCEPTION_RANDOM.create(targets.iterator().next().getName());
                        }
                        else {
                            throw GIVE_MULTIPLE_EXCEPTION_RANDOM.create(targets.size() - i);
                        }
                    }
                    if (replace) {
                        power.clear();
                    }
                    if (power.givePower(stand)) {
                        i++;
                    }
                    else if (targets.size() == 1) {
                        throw GIVE_SINGLE_EXCEPTION_ALREADY_HAS.create(targets.iterator().next().getName());
                    }
                }
            }
        }
        if (i == 0) {
            if (targets.size() == 1) {
                throw GIVE_SINGLE_EXCEPTION_ALREADY_HAS.create(targets.iterator().next().getName());
            } else {
                throw GIVE_MULTIPLE_EXCEPTION_ALREADY_HAVE.create(targets.size());
            }
        }
        else {
            if (targets.size() == 1) {
                source.sendSuccess(new TranslationTextComponent("commands.stand.give.success.single", 
                        stand != null ? stand.getName() : "", 
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
        StandType<?> removedStand = null;
        for (ServerPlayerEntity player : targets) {
            IStandPower power = IStandPower.getStandPowerOptional(player).orElse(null);
            if (power != null) {
                removedStand = power.getType();
                power.clear();
                power.fullStandClear();
                i++;
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
                ITextComponent message;
                if (removedStand != null) {
                    message = new TranslationTextComponent("commands.stand.remove.success.single", 
                            removedStand.getName(), targets.iterator().next().getDisplayName());
                }
                else {
                    message = new TranslationTextComponent("commands.stand.remove.success.single.no_stand", 
                            targets.iterator().next().getDisplayName());
                }
                source.sendSuccess(message, true);
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
                StandType<?> type = power.getType();
                source.sendSuccess(new TranslationTextComponent("commands.stand.query.success", player.getDisplayName(), type.getName()), false);
                return JojoCustomRegistries.STANDS.getNumericId(type.getRegistryName());
            }
        }
        throw QUERY_SINGLE_FAILED_EXCEPTION.create(player.getName());
    }
}
