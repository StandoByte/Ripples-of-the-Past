package com.github.standobyte.jojo.command;

import static com.github.standobyte.jojo.power.stand.IStandPower.MAX_EXP;

import java.util.Collection;

import com.github.standobyte.jojo.power.stand.IStandPower;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class StandExpCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("standexp").requires(ctx -> ctx.hasPermission(2))
                .then(Commands.literal("add").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("amount", IntegerArgumentType.integer(-MAX_EXP, MAX_EXP)).executes(
                        ctx -> addExperience(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("set").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("amount", IntegerArgumentType.integer(0, MAX_EXP)).executes(
                        ctx -> setExperience(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("query").then(Commands.argument("targets", EntityArgument.player()).executes(
                        ctx -> queryExperience(ctx.getSource(), EntityArgument.getPlayer(ctx, "targets")))))
                );
    }

    private static int queryExperience(CommandSource source, ServerPlayerEntity player) {
        return IStandPower.getStandPowerOptional(player).map(power -> {
            int exp = power.getExp();
            source.sendSuccess(new TranslationTextComponent("commands.standexp.query", player.getDisplayName(), exp, MAX_EXP), false);
            return exp;
        }).orElse(-1);
    }

    private static int addExperience(CommandSource source, Collection<? extends ServerPlayerEntity> targets, int amount) {
        for(ServerPlayerEntity player : targets) {
            IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                power.setExp(power.getExp() + amount);
            });
        }
        if (targets.size() == 1) {
            source.sendSuccess(new TranslationTextComponent("commands.standexp.add.success.single", amount, targets.iterator().next().getDisplayName()), true);
        } else {
            source.sendSuccess(new TranslationTextComponent("commands.standexp.add.success.multiple", amount, targets.size()), true);
        }
        return targets.size();
    }

    private static int setExperience(CommandSource source, Collection<? extends ServerPlayerEntity> targets, int amount) {
        for(ServerPlayerEntity player : targets) {
            IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                power.setExp(amount);
            });
        }
        if (targets.size() == 1) {
            source.sendSuccess(new TranslationTextComponent("commands.standexp.set.success.single", amount, targets.iterator().next().getDisplayName()), true);
        } else {
            source.sendSuccess(new TranslationTextComponent("commands.standexp.set.success.multiple", amount, targets.size()), true);
        }
        return targets.size();
    }
}
