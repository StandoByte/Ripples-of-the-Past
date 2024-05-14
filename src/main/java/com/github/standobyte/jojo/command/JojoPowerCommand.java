package com.github.standobyte.jojo.command;

import java.util.Collection;

import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class JojoPowerCommand {
    private static final DynamicCommandExceptionType GIVE_SINGLE_EXCEPTION = new DynamicCommandExceptionType(
            player -> new TranslationTextComponent("commands.non_stand.give.failed.single", player));
    private static final DynamicCommandExceptionType GIVE_MULTIPLE_EXCEPTION = new DynamicCommandExceptionType(
            count -> new TranslationTextComponent("commands.non_stand.give.failed.multiple", count));
    private static final DynamicCommandExceptionType QUERY_SINGLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            player -> new TranslationTextComponent("commands.non_stand.query.failed.single", player));
    private static final DynamicCommandExceptionType QUERY_MULTIPLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            count -> new TranslationTextComponent("commands.non_stand.query.failed.multiple", count));

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("jojopower").requires(ctx -> ctx.hasPermission(2))
                .then(Commands.literal("give").then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("type", new NonStandTypeArgument())
                                .executes(ctx -> giveNonStandPowers(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), NonStandTypeArgument.getPowerType(ctx, "type"))))))
                .then(Commands.literal("clear").then(Commands.argument("targets", EntityArgument.players())
                        .executes(ctx -> removeNonStandPowers(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets")))))
                );
        JojoCommandsCommand.addCommand("jojopower");
    }
    
    private static int giveNonStandPowers(CommandSource source, Collection<ServerPlayerEntity> targets, NonStandPowerType<?> powerType) throws CommandSyntaxException {
        int i = 0;
        for (ServerPlayerEntity player : targets) {
            INonStandPower power = INonStandPower.getNonStandPowerOptional(player).orElse(null);
            if (power != null) {
                if (!power.hasPower() && power.givePower(powerType)) {
                    i++;
                    power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).ifPresent(vampirism -> vampirism.setVampireFullPower(true));
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
                        "commands.non_stand.give.success.single", 
                        new TranslationTextComponent(powerType.getTranslationKey()), targets.iterator().next().getDisplayName()), true);
            }
            else {
                source.sendSuccess(new TranslationTextComponent(
                        "commands.non_stand.give.success.multiple", 
                        new TranslationTextComponent(powerType.getTranslationKey()), i), true);
            }
            return i;
        }
    }

    private static int removeNonStandPowers(CommandSource source, Collection<ServerPlayerEntity> targets) throws CommandSyntaxException {
        int i = 0;
        NonStandPowerType<?> removedPower = null;
        for (ServerPlayerEntity player : targets) {
            INonStandPower power = INonStandPower.getNonStandPowerOptional(player).orElse(null);
            if (power != null) {
                NonStandPowerType<?> toBeRemoved = power.getType();
                if (power.clear()) {
                    i++;
                    removedPower = toBeRemoved;
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
                source.sendSuccess(new TranslationTextComponent("commands.non_stand.remove.success.single", 
                        removedPower != null ? new TranslationTextComponent(removedPower.getTranslationKey()) : "", targets.iterator().next().getDisplayName()), true);
            } else {
                source.sendSuccess(new TranslationTextComponent("commands.non_stand.remove.success.multiple", i), true);
            }
            return i;
        }
    }
}
