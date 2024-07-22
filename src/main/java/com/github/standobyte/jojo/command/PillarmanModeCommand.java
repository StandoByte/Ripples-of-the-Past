package com.github.standobyte.jojo.command;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BinaryOperator;

import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanPowerType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class PillarmanModeCommand {
    private static final DynamicCommandExceptionType SINGLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            player -> new TranslationTextComponent("commands.pillarman.failed.single", player));
    private static final DynamicCommandExceptionType MULTIPLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            count -> new TranslationTextComponent("commands.pillarman.failed.multiple", count));

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("pillarman").requires(ctx -> ctx.hasPermission(2))
                .then(Commands.literal("set")
                        .then(Commands.literal("stage").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("stage", FloatArgumentType.floatArg(1, PillarmanData.MAX_STAGE_LEVEL))
                                .executes(ctx -> setStage(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), FloatArgumentType.getFloat(ctx, "stage"))))))
                        .then(Commands.literal("mode").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("mode", IntegerArgumentType.integer(0, 3))
                                .executes(ctx -> setMode(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "mode"))))))
                ));
        JojoCommandsCommand.addCommand("pillarman");
    }

    private static int setStage(CommandSource source, Collection<? extends ServerPlayerEntity> targets, float stage) throws CommandSyntaxException {
        return setStage(source, targets, (current, arg) -> arg, stage, "");
    }
    
    private static int setMode(CommandSource source, Collection<? extends ServerPlayerEntity> targets, int mode) throws CommandSyntaxException {
        return setMode(source, targets, (current, arg) -> arg, mode, "");
    }

    private static int setStage(CommandSource source, Collection<? extends ServerPlayerEntity> targets, BinaryOperator<Float> operation, float stage, String msg) throws CommandSyntaxException {
        int success = 0;
        for (ServerPlayerEntity player : targets) {
            success += INonStandPower.getNonStandPowerOptional(player).map(power -> {
                Optional<PillarmanData> pillarmanOptional = power.getTypeSpecificData(ModPowers.PILLAR_MAN.get());
                pillarmanOptional.ifPresent(pillarman -> {
                    float stageToSet = operation.apply((float) pillarman.getEvolutionStage(), stage);
                    pillarman.setEvolutionStage((int) stageToSet);
                    pillarman.setPillarmanBuffs(player, 1);
                    PillarmanPowerType.effectsCheck(power);
                });
                return pillarmanOptional.isPresent() ? 1 : 0;
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
                source.sendSuccess(new TranslationTextComponent("commands.pillarman.stage." + msg + "success.single", stage, targets.iterator().next().getDisplayName()), true);
            } else {
                source.sendSuccess(new TranslationTextComponent("commands.pillarman.stage." + msg + "success.multiple", stage, success), true);
            }
            return success;
        }
    }

    private static int setMode(CommandSource source, Collection<? extends ServerPlayerEntity> targets, BinaryOperator<Integer> operation, int mode, String msg) throws CommandSyntaxException {
        int success = 0;
        for (ServerPlayerEntity player : targets) {
            success += INonStandPower.getNonStandPowerOptional(player).map(power -> {
                Optional<PillarmanData> pillarmanOptional = power.getTypeSpecificData(ModPowers.PILLAR_MAN.get());
                pillarmanOptional.ifPresent(pillarman -> {
                	PillarmanData.Mode mode2 = PillarmanData.Mode.NONE; 
                	if (mode > 0 && mode <= 3) mode2 = PillarmanData.Mode.values()[mode];
                	pillarman.setMode(mode2);
                });
                return pillarmanOptional.isPresent() ? 1 : 0;
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
                source.sendSuccess(new TranslationTextComponent("commands.pillarman.mode." + msg + "success.single", mode, targets.iterator().next().getDisplayName()), true);
            } else {
                source.sendSuccess(new TranslationTextComponent("commands.pillarman.mode." + msg + "success.multiple", mode, success), true);
            }
            return success;
        }
    }
}
