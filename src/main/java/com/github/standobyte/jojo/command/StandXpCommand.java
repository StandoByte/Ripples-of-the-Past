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

public class StandXpCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("standxp").requires(ctx -> ctx.hasPermission(2))
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
            int xp = power.getXp();
            source.sendSuccess(new TranslationTextComponent("commands.standxp.query", player.getDisplayName(), xp, MAX_EXP), false);
            return xp;
        }).orElse(-1);
    }

    private static int addExperience(CommandSource source, Collection<? extends ServerPlayerEntity> targets, int amount) {
        for(ServerPlayerEntity player : targets) {
            IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                power.setXp(power.getXp() + amount);
            });
        }
        if (targets.size() == 1) {
            source.sendSuccess(new TranslationTextComponent("commands.standxp.add.success.single", amount, targets.iterator().next().getDisplayName()), true);
        } else {
            source.sendSuccess(new TranslationTextComponent("commands.standxp.add.success.multiple", amount, targets.size()), true);
        }
        return targets.size();
    }

    private static int setExperience(CommandSource source, Collection<? extends ServerPlayerEntity> targets, int amount) {
        for(ServerPlayerEntity player : targets) {
            IStandPower.getStandPowerOptional(player).ifPresent(power -> {
                power.setXp(amount);
            });
        }
        if (targets.size() == 1) {
            source.sendSuccess(new TranslationTextComponent("commands.standxp.set.success.single", amount, targets.iterator().next().getDisplayName()), true);
        } else {
            source.sendSuccess(new TranslationTextComponent("commands.standxp.set.success.multiple", amount, targets.size()), true);
        }
        return targets.size();
    }
}
