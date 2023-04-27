package com.github.standobyte.jojo.command;

import java.util.Collection;
import java.util.Optional;

import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.nonstand.type.HamonSkill.HamonStat;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class HamonCommand {
    private static final DynamicCommandExceptionType SINGLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            player -> new TranslationTextComponent("commands.hamon.failed.single", player));
    private static final DynamicCommandExceptionType MULTIPLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            count -> new TranslationTextComponent("commands.hamon.failed.multiple", count));

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("hamonstat").requires(ctx -> ctx.hasPermission(2))
                .then(Commands.literal("strength").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("level", IntegerArgumentType.integer(0, HamonData.MAX_STAT_LEVEL))
                        .executes(ctx -> setHamonStat(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "level"), HamonStat.STRENGTH, true))
                        .then(Commands.argument("ignoreBreathing", BoolArgumentType.bool())
                                .executes(ctx -> setHamonStat(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "level"), HamonStat.STRENGTH, BoolArgumentType.getBool(ctx, "ignoreBreathing")))))))
                .then(Commands.literal("control").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("level", IntegerArgumentType.integer(0, HamonData.MAX_STAT_LEVEL))
                        .executes(ctx -> setHamonStat(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "level"), HamonStat.CONTROL, true))
                        .then(Commands.argument("ignoreBreathing", BoolArgumentType.bool())
                                .executes(ctx -> setHamonStat(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "level"), HamonStat.CONTROL, BoolArgumentType.getBool(ctx, "ignoreBreathing")))))))
                .then(Commands.literal("breathing").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("level", FloatArgumentType.floatArg(0, HamonData.MAX_BREATHING_LEVEL))
                        .executes(ctx -> setBreathing(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), FloatArgumentType.getFloat(ctx, "level"))))))
                );
    }

    private static int setHamonStat(CommandSource source, Collection<? extends ServerPlayerEntity> targets, int level, HamonStat stat, boolean ignoreBreathing) throws CommandSyntaxException {
        int points = HamonData.pointsAtLevel(level);
        int success = 0;
        for (ServerPlayerEntity player : targets) {
            success += INonStandPower.getNonStandPowerOptional(player).map(power -> {
                Optional<HamonData> hamonOptional = power.getTypeSpecificData(ModPowers.HAMON.get());
                hamonOptional.ifPresent(hamon -> {
                    hamon.setHamonStatPoints(stat, points, ignoreBreathing, true);
                });
                return hamonOptional.isPresent() ? 1 : 0;
            }).orElse(0);
        }
        if (success == 0) {
            if (targets.size() == 1) {
                throw SINGLE_FAILED_EXCEPTION.create(targets.iterator().next().getName());
            } else {
                throw MULTIPLE_FAILED_EXCEPTION.create(targets.size());
            }
        }
        else {
            if (targets.size() == 1) {
                source.sendSuccess(new TranslationTextComponent(stat == HamonStat.STRENGTH ? "commands.hamon.strength.success.single" : "commands.hamon.control.success.single", 
                        level, targets.iterator().next().getDisplayName()), true);
            } else {
                source.sendSuccess(new TranslationTextComponent(stat == HamonStat.STRENGTH ? "commands.hamon.strength.success.multiple" : "commands.hamon.control.success.multiple", 
                        level, success), true);
            }
            return success;
        }
    }

    private static int setBreathing(CommandSource source, Collection<? extends ServerPlayerEntity> targets, float level) throws CommandSyntaxException {
        int success = 0;
        for (ServerPlayerEntity player : targets) {
            success += INonStandPower.getNonStandPowerOptional(player).map(power -> {
                Optional<HamonData> hamonOptional = power.getTypeSpecificData(ModPowers.HAMON.get());
                hamonOptional.ifPresent(hamon -> {
                    hamon.setBreathingLevel(level);
                });
                return hamonOptional.isPresent() ? 1 : 0;
            }).orElse(0);
        }
        if (success == 0) {
            if (targets.size() == 1) {
                throw SINGLE_FAILED_EXCEPTION.create(targets.iterator().next().getName());
            } else {
                throw MULTIPLE_FAILED_EXCEPTION.create(targets.size());
            }
        }
        else {
            if (targets.size() == 1) {
                source.sendSuccess(new TranslationTextComponent("commands.hamon.breathing.success.single", level, targets.iterator().next().getDisplayName()), true);
            } else {
                source.sendSuccess(new TranslationTextComponent("commands.hamon.breathing.success.multiple", level, success), true);
            }
            return success;
        }
    }

}
