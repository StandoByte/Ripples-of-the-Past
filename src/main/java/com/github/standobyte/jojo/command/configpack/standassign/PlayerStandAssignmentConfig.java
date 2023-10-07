package com.github.standobyte.jojo.command.configpack.standassign;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.command.StandArgument;
import com.github.standobyte.jojo.command.configpack.DataConfigEventHandler;
import com.github.standobyte.jojo.command.configpack.JsonDataConfig;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.StandAssignmentDataPacket;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class PlayerStandAssignmentConfig extends JsonDataConfig {
    private static PlayerStandAssignmentConfig instance;
    private static final String RESOURCE_NAME = "stand_assign";
    private static final ResourceLocation FILE_PATH = new ResourceLocation(JojoMod.MOD_ID, RESOURCE_NAME);
    
    private StandAssignmentList assignedStands = new StandAssignmentList();
    private StandAssignmentEntry localClientEntry;
    
    public static PlayerStandAssignmentConfig init(IEventBus forgeEventBus) {
        if (instance == null) {
            instance = new PlayerStandAssignmentConfig();
        }
        DataConfigEventHandler.registerEventHandler(instance, forgeEventBus);
        return instance;
    }
    
    public static PlayerStandAssignmentConfig getInstance() {
        return instance;
    }
    
    private PlayerStandAssignmentConfig() {
        super(new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(StandAssignmentList.class, StandAssignmentList.SERIALIZER)
                .create(), RESOURCE_NAME);
    }
    
    @Override
    public LiteralArgumentBuilder<CommandSource> commandRegister(LiteralArgumentBuilder<CommandSource> builder, String literal) {
        return builder.then(Commands.literal(literal)
                .then(Commands.literal("add")
                        .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("stand", new StandArgument())
                        .executes(ctx -> assignStandTo(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), StandArgument.getStandType(ctx, "stand"))))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("stand", new StandArgument())
                        .executes(ctx -> removeAssignedStandFrom(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), StandArgument.getStandType(ctx, "stand"))))))
                .then(Commands.literal("clear")
                        .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> clearAssignedStandsFrom(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("clear_all")
                        .executes(ctx -> fullAssignmentsClear(ctx.getSource()))));
    }
    
    
    
    /** 
     * 
     * @param player
     * @return 
     *   The collection of the Stands assigned to the player by the config.
     *   null return value means that there are no assigned Stand types,
     *   so the player isn't restricted in that regard.
     *   If the json file contains a player entry with an empty "stands" array,
     *   the player is blocked from getting any Stand.
     */
    @Nullable
    public List<StandType<?>> getAssignedStands(PlayerEntity player) {
        StandAssignmentEntry entry = getAssignmentEntry(player);
        
        if (entry == null) {
            return null;
        }
        return entry.getAssignedStands();
    }
    
    @Nullable
    private StandAssignmentEntry getAssignmentEntry(PlayerEntity player) {
        if (!player.level.isClientSide()) {
            return assignedStands.get(player.getGameProfile());
        }
        else {
            return player.isLocalPlayer() ? localClientEntry : null;
        }
    }
    
    public List<StandType<?>> limitToAssignedStands(PlayerEntity player, List<StandType<?>> availableStands) {
        List<StandType<?>> assignedStands = getAssignedStands(player);
        if (assignedStands == null) {
            return availableStands;
        }
        return availableStands.stream().filter(assignedStands::contains).collect(Collectors.toList());
    }
    
    
    
    private static final Dynamic2CommandExceptionType FAILED_ADDING_ENTRY = new Dynamic2CommandExceptionType(
            (stand, player) -> new TranslationTextComponent("commands.jojoconfigpack.stand_assign.failed.add", stand, player));
    private int assignStandTo(CommandSource source, ServerPlayerEntity player, StandType<?> standType) throws CommandSyntaxException {
        if (assignedStands.addAssignedStand(player.getGameProfile(), standType)) {
            saveStandAssignments(source);
            
            source.sendSuccess(new TranslationTextComponent("commands.jojoconfigpack.stand_assign.added", 
                    standType.getName(), player.getDisplayName())
                    .withStyle(style -> styleMessageWithLink(source, style)), 
                    true);
            syncToClient(player);
            
            return 1;
        }
        else {
            throw FAILED_ADDING_ENTRY.create(standType.getName(), player.getDisplayName());
        }
    }
    
    private static final Dynamic2CommandExceptionType FAILED_REMOVING_ENTRY = new Dynamic2CommandExceptionType(
            (stand, player) -> new TranslationTextComponent("commands.jojoconfigpack.stand_assign.failed.remove", stand, player));
    private int removeAssignedStandFrom(CommandSource source, ServerPlayerEntity player, StandType<?> standType) throws CommandSyntaxException {
        if (assignedStands.removeAssignedStand(player.getGameProfile(), standType, true)) {
            saveStandAssignments(source);
            
            source.sendSuccess(new TranslationTextComponent("commands.jojoconfigpack.stand_assign.removed", 
                    standType.getName(), player.getDisplayName())
                    .withStyle(style -> styleMessageWithLink(source, style)), 
                    true);
            syncToClient(player);
            
            return 1;
        }
        else {
            throw FAILED_REMOVING_ENTRY.create(standType.getName(), player.getDisplayName());
        }
    }
    
    private static final DynamicCommandExceptionType FAILED_CLEARING_ENTRIES = new DynamicCommandExceptionType(
            player -> new TranslationTextComponent("commands.jojoconfigpack.stand_assign.failed.clear", player));
    private int clearAssignedStandsFrom(CommandSource source, ServerPlayerEntity player) throws CommandSyntaxException {
        if (assignedStands.remove(player.getGameProfile())) {
            saveStandAssignments(source);
            
            source.sendSuccess(new TranslationTextComponent("commands.jojoconfigpack.stand_assign.cleared", 
                    player.getDisplayName())
                    .withStyle(style -> styleMessageWithLink(source, style)), 
                    true);
            syncToClient(player);
            
            return 1;
        }
        else {
            throw FAILED_CLEARING_ENTRIES.create(player.getDisplayName());
        }
    }
    
    private int fullAssignmentsClear(CommandSource source) throws CommandSyntaxException {
        assignedStands.clear();
        
        source.sendSuccess(new TranslationTextComponent("commands.jojoconfigpack.stand_assign.cleared_all") 
                .withStyle(style -> styleMessageWithLink(source, style)), 
                true);
        source.getServer().getPlayerList().getPlayers().forEach(player -> syncToClient(player));
        return 1;
    }
    
    private void saveStandAssignments(CommandSource source) throws CommandSyntaxException {
        try {
            genDataPackBase(source);
            try {
                genJsonFromObj(assignedStands, FILE_PATH, RESOURCE_NAME, source.getServer());
            }
            catch (JsonWriteException e) {
                LOGGER.error("Couldn't save Stand assignments to {}", e.jsonFilePath, e.getCause());
                throw e.getCause();
            }
        } catch (Throwable e) {
            SimpleCommandExceptionType exceptionType = new SimpleCommandExceptionType(new StringTextComponent(e.getMessage()));
            throw exceptionType.create();
        }
    }
    
    private Style styleMessageWithLink(CommandSource source, Style messageStyle) {
        return messageStyle.applyFormat(TextFormatting.GRAY)
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, 
                        dataPackPath(source.getServer()).resolve(String.format("data/%s/%s", JojoMod.MOD_ID, RESOURCE_NAME)).normalize().toString()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                        new TranslationTextComponent("commands.jojoconfigpack.stand_assign.folder_link", 
                                new StringTextComponent("datapacks").withStyle(TextFormatting.ITALIC)
                                )));
    }
    
    
    @Override
    public void syncToClient(ServerPlayerEntity player) {
        PacketManager.sendToClient(new StandAssignmentDataPacket(getAssignmentEntry(player)), player);
    }
    
    public void handleClientPacket(StandAssignmentDataPacket packet) {
        this.localClientEntry = new StandAssignmentEntry(ClientUtil.getClientPlayer().getGameProfile(), packet.stands.orElse(null));
    }
    
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, IResourceManager resourceManager, IProfiler profiler) {
        JsonElement json = resourceList.get(FILE_PATH);
        assignedStands = getGson().fromJson(json, StandAssignmentList.class);
        if (assignedStands == null) {
            assignedStands = new StandAssignmentList();
        }
    }
}
