package com.github.standobyte.jojo.command;

import java.util.Collection;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.command.argument.StandArgument;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.item.StandDiscItem;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class StandDiscGiveCommand {
    
    public static void register(CommandDispatcher<CommandSource> pDispatcher) {
        pDispatcher.register(Commands.literal("standdisc").requires(ctx -> ctx.hasPermission(2))
                .then(Commands.literal("give").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("stand", new StandArgument())
                        .executes(ctx -> giveStandDisc(ctx.getSource(), StandArgument.getStandType(ctx, "stand"), EntityArgument.getPlayers(ctx, "targets"))))))
                .then(Commands.literal("random").then(Commands.argument("targets", EntityArgument.players())
                        .executes(ctx -> giveRandomStandDiscs(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets")))))
                );
        JojoCommandsCommand.addCommand("standdisc");
    }
    
    private static int giveStandDisc(CommandSource source, StandType<?> standType, Collection<ServerPlayerEntity> targets) throws CommandSyntaxException {
        return giveStandDiscItem(source, standType, targets);
    }
    
    private static int giveRandomStandDiscs(CommandSource source, Collection<ServerPlayerEntity> targets) throws CommandSyntaxException {
        return giveStandDiscItem(source, null, targets);
    }
    
    /**
     * @param standType - if the argument is null, a random Stand is picked instead
     */
    private static int giveStandDiscItem(CommandSource source, @Nullable StandType<?> standType, Collection<ServerPlayerEntity> targets) throws CommandSyntaxException {
        int i = 0;
        boolean random = standType == null;
        for (ServerPlayerEntity player : targets) {
            if (random) {
                Either<StandType<?>, ITextComponent> randomStandOrError = StandUtil.randomStandOrError(player, player.getRandom());
                randomStandOrError.ifRight(error -> source.sendFailure(error));
                standType = randomStandOrError.left().orElse(null);
            }
            if (standType == null) {
                continue;
            }
            
            ItemStack discItem = createItemStack(standType);
            boolean added = player.inventory.add(discItem);
            if (added && discItem.isEmpty()) {
                discItem.setCount(1);
                ItemEntity itemEntity = player.drop(discItem, false);
                if (itemEntity != null) {
                    itemEntity.makeFakeItem();
                }

                player.level.playSound((PlayerEntity) null, 
                        player.getX(), player.getY(), player.getZ(), 
                        SoundEvents.ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, 
                        ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                player.inventoryMenu.broadcastChanges();
            } else {
                ItemEntity itemEntity = player.drop(discItem, false);
                if (itemEntity != null) {
                    itemEntity.setNoPickUpDelay();
                    itemEntity.setOwner(player.getUUID());
                }
            }
            
            i++;
        }
        
        if (i > 0) {
            if (targets.size() == 1) {
                source.sendSuccess(new TranslationTextComponent("commands.give.success.single", 1, 
                        new TranslationTextComponent(ModItems.STAND_DISC.get().getDescriptionId()), targets.iterator().next().getDisplayName()), true);
            } else {
                source.sendSuccess(new TranslationTextComponent("commands.give.success.single", 1, 
                        new TranslationTextComponent(ModItems.STAND_DISC.get().getDescriptionId()), i), true);
            }
        }
        
        return i;
    }
    
    private static ItemStack createItemStack(StandType<?> standType) {
        return StandDiscItem.withStand(new ItemStack(ModItems.STAND_DISC.get()), new StandInstance(standType));
    }
}
