package com.github.standobyte.jojo.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;

public class StandLevelCommand {
    private static final DynamicCommandExceptionType STAND_SINGLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            player -> new TranslationTextComponent("commands.stand.query.failed.single", player));
    private static final DynamicCommandExceptionType STAND_RESOLVE_SINGLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            player -> new TranslationTextComponent("commands.stand.resolve.failed.single", player));
    private static final DynamicCommandExceptionType STAND_RESOLVE_MULTIPLE_FAILED_EXCEPTION = new DynamicCommandExceptionType(
            count -> new TranslationTextComponent("commands.stand.resolve.failed.multiple", count));

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("standlevel").requires(ctx -> ctx.hasPermission(2))
                .then(Commands.literal("set").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("level", IntegerArgumentType.integer(0))
                        .executes(ctx -> setStandLevel(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "level"))))))
                .then(Commands.literal("add").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("levels", IntegerArgumentType.integer(0))
                        .executes(ctx -> addStandLevel(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "levels"))))))
                .then(Commands.literal("query").then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> getStandLevel(ctx.getSource(), EntityArgument.getPlayer(ctx, "target")))))
                );
        JojoCommandsCommand.addCommand("standlevel");
    }

    private static int getStandLevel(CommandSource source, ServerPlayerEntity target) throws CommandSyntaxException {
        IStandPower stand = getStands(Util.make(new ArrayList<>(), list -> list.add(target))).iterator().next();
        int level = stand.getResolveLevel();
        source.sendSuccess(new TranslationTextComponent("commands.standlevel.query.success", target.getDisplayName(), level), false);
        return level;
    }

    private static int addStandLevel(CommandSource source, Collection<? extends ServerPlayerEntity> targets, int levels) throws CommandSyntaxException {
        Collection<IStandPower> stands = getStands(targets);
        for (IStandPower stand : stands) {
            stand.setResolveLevel(stand.getResolveLevel() + levels);
        }
        
        if (stands.size() == 1) {
            source.sendSuccess(new TranslationTextComponent("commands.standlevel.add.success.single", levels, targets.iterator().next().getDisplayName()), true);
        } else {
            source.sendSuccess(new TranslationTextComponent("commands.standlevel.add.success.multiple", levels, stands.size()), true);
        }
        
        return stands.size();
    }

    private static int setStandLevel(CommandSource source, Collection<? extends ServerPlayerEntity> targets, int level) throws CommandSyntaxException {
        Collection<IStandPower> stands = getStands(targets);
        for (IStandPower stand : stands) {
            stand.setResolveLevel(level);
        }
        
        if (stands.size() == 1) {
            source.sendSuccess(new TranslationTextComponent("commands.standlevel.set.success.single", level, targets.iterator().next().getDisplayName()), true);
        } else {
            source.sendSuccess(new TranslationTextComponent("commands.standlevel.set.success.multiple", level, stands.size()), true);
        }
        
        return stands.size();
    }
    
    private static Collection<IStandPower> getStands(Collection<? extends ServerPlayerEntity> targets) throws CommandSyntaxException {
        List<IStandPower> stands = new ArrayList<>();
        boolean noStand = false;
        for (ServerPlayerEntity player : targets) {
            IStandPower stand = IStandPower.getStandPowerOptional(player).orElse(null);
            if (stand == null || !stand.hasPower()) {
                noStand = true;
            }
            else if (stand.usesResolve()) {
                stands.add(stand);
            }
        }
        if (stands.isEmpty()) {
            if (targets.size() == 1) {
                if (noStand) {
                    throw STAND_SINGLE_FAILED_EXCEPTION.create(targets.iterator().next().getName());
                }
                else {
                    throw STAND_RESOLVE_SINGLE_FAILED_EXCEPTION.create(targets.iterator().next().getName());
                }
            }
            else {
                throw STAND_RESOLVE_MULTIPLE_FAILED_EXCEPTION.create(targets.iterator().next().getName());
            }
        }
        else {
            return stands;
        }
    }
}
