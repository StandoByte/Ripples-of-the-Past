package com.github.standobyte.jojo.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.capability.chunk.ChunkCap;
import com.github.standobyte.jojo.capability.chunk.ChunkCapProvider;
import com.github.standobyte.jojo.capability.chunk.ChunkCapStorage;
import com.github.standobyte.jojo.capability.entity.ClientPlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.ClientPlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.EntityUtilCap;
import com.github.standobyte.jojo.capability.entity.EntityUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.EntityUtilCapStorage;
import com.github.standobyte.jojo.capability.entity.LivingUtilCap;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapStorage;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapStorage;
import com.github.standobyte.jojo.capability.entity.hamonutil.EntityHamonChargeCap;
import com.github.standobyte.jojo.capability.entity.hamonutil.EntityHamonChargeCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.EntityHamonChargeCapStorage;
import com.github.standobyte.jojo.capability.entity.hamonutil.ProjectileHamonChargeCap;
import com.github.standobyte.jojo.capability.entity.hamonutil.ProjectileHamonChargeCapProvider;
import com.github.standobyte.jojo.capability.entity.hamonutil.ProjectileHamonChargeCapStorage;
import com.github.standobyte.jojo.capability.entity.power.NonStandCapProvider;
import com.github.standobyte.jojo.capability.entity.power.NonStandCapStorage;
import com.github.standobyte.jojo.capability.entity.power.StandCapProvider;
import com.github.standobyte.jojo.capability.entity.power.StandCapStorage;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCap;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.capability.world.SaveFileUtilCapStorage;
import com.github.standobyte.jojo.capability.world.WorldUtilCap;
import com.github.standobyte.jojo.capability.world.WorldUtilCapProvider;
import com.github.standobyte.jojo.capability.world.WorldUtilCapStorage;
import com.github.standobyte.jojo.command.ConfigPackCommand;
import com.github.standobyte.jojo.command.HamonStatCommand;
import com.github.standobyte.jojo.command.JojoCommandsCommand;
import com.github.standobyte.jojo.command.JojoControlsCommand;
import com.github.standobyte.jojo.command.JojoEnergyCommand;
import com.github.standobyte.jojo.command.JojoPowerCommand;
import com.github.standobyte.jojo.command.RockPaperScissorsCommand;
import com.github.standobyte.jojo.command.StandCommand;
import com.github.standobyte.jojo.command.StandDiscGiveCommand;
import com.github.standobyte.jojo.command.StandLevelCommand;
import com.github.standobyte.jojo.init.ModStructures;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.UpdateClientCapCachePacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.NonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonUtil;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandPower;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ForgeBusEventSubscriber {
    private static final ResourceLocation STAND_CAP = new ResourceLocation(JojoMod.MOD_ID, "stand");
    private static final ResourceLocation NON_STAND_CAP = new ResourceLocation(JojoMod.MOD_ID, "non_stand");
    private static final ResourceLocation PLAYER_UTIL_CAP = new ResourceLocation(JojoMod.MOD_ID, "player_util");
    private static final ResourceLocation CLIENT_PLAYER_UTIL_CAP = new ResourceLocation(JojoMod.MOD_ID, "client_player_util");
    private static final ResourceLocation LIVING_UTIL_CAP = new ResourceLocation(JojoMod.MOD_ID, "living_util");
    private static final ResourceLocation ENTITY_UTIL_CAP = new ResourceLocation(JojoMod.MOD_ID, "entity_util");
    private static final ResourceLocation ENTITY_HAMON_CHARGE_CAP = new ResourceLocation(JojoMod.MOD_ID, "entity_hamon_charge");
    private static final ResourceLocation PROJECTILE_HAMON_CAP = new ResourceLocation(JojoMod.MOD_ID, "projectile_hamon");
    private static final ResourceLocation WORLD_UTIL_CAP = new ResourceLocation(JojoMod.MOD_ID, "world_util");
    private static final ResourceLocation SAVE_FILE_UTIL_CAP = new ResourceLocation(JojoMod.MOD_ID, "save_file_util");
    private static final ResourceLocation CHUNK_UTIL_CAP = new ResourceLocation(JojoMod.MOD_ID, "chunk_util");
    
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
        StandCommand.register(dispatcher);
        StandDiscGiveCommand.register(dispatcher);
        StandLevelCommand.register(dispatcher);
        JojoPowerCommand.register(dispatcher);
        JojoEnergyCommand.register(dispatcher);
        JojoControlsCommand.register(dispatcher);
        HamonStatCommand.register(dispatcher);
        RockPaperScissorsCommand.register(dispatcher);
        ConfigPackCommand.register(dispatcher);
        JojoCommandsCommand.register(dispatcher);
    }
    
    
    
    @SubscribeEvent
    public static void onAttachCapabilitiesWorld(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();
        event.addCapability(WORLD_UTIL_CAP, new WorldUtilCapProvider(world));
        if (!world.isClientSide() && world.dimension() == World.OVERWORLD) {
            event.addCapability(SAVE_FILE_UTIL_CAP, new SaveFileUtilCapProvider((ServerWorld) world));
        }
    }
    
    @SubscribeEvent
    public static void onAttachCapabilitiesChunk(AttachCapabilitiesEvent<Chunk> event) {
        Chunk chunk = event.getObject();
        event.addCapability(CHUNK_UTIL_CAP, new ChunkCapProvider(chunk));
    }
    
    @SubscribeEvent
    public static void onAttachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        event.addCapability(ENTITY_UTIL_CAP, new EntityUtilCapProvider(entity));
        if (entity instanceof LivingEntity) {
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) event.getObject();
                event.addCapability(STAND_CAP, new StandCapProvider(player));
                event.addCapability(NON_STAND_CAP, new NonStandCapProvider(player));
                event.addCapability(PLAYER_UTIL_CAP, new PlayerUtilCapProvider(player));
                if (player.level.isClientSide()) {
                    event.addCapability(CLIENT_PLAYER_UTIL_CAP, new ClientPlayerUtilCapProvider(player));
                }
            }
            event.addCapability(LIVING_UTIL_CAP, new LivingUtilCapProvider((LivingEntity) entity));
        }
        if (entity instanceof ProjectileEntity && (HamonUtil.ProjectileChargeProperties.canBeChargedWithHamon(entity))) {
            event.addCapability(PROJECTILE_HAMON_CAP, new ProjectileHamonChargeCapProvider(entity));
        }
        if (entity instanceof LivingEntity || entity instanceof ItemEntity) {
            event.addCapability(ENTITY_HAMON_CHARGE_CAP, new EntityHamonChargeCapProvider(entity));
        }
    }
    
    public static void registerCapabilities() { // moved the registration here just so that it's in the same place as the attachment
        CapabilityManager.INSTANCE.register(IStandPower.class, new StandCapStorage(), () -> new StandPower(null));
        CapabilityManager.INSTANCE.register(INonStandPower.class, new NonStandCapStorage(), () -> new NonStandPower(null));
        CapabilityManager.INSTANCE.register(PlayerUtilCap.class, new PlayerUtilCapStorage(), () -> new PlayerUtilCap(null));
        CapabilityManager.INSTANCE.register(ClientPlayerUtilCap.class, new IStorage<ClientPlayerUtilCap>() {
            @Override public INBT writeNBT(Capability<ClientPlayerUtilCap> capability, ClientPlayerUtilCap instance, Direction side) { return null; }
            @Override public void readNBT(Capability<ClientPlayerUtilCap> capability, ClientPlayerUtilCap instance, Direction side, INBT nbt) {}
        }, () -> new ClientPlayerUtilCap(null));
        CapabilityManager.INSTANCE.register(LivingUtilCap.class, new LivingUtilCapStorage(), () -> new LivingUtilCap(null));
        CapabilityManager.INSTANCE.register(EntityUtilCap.class, new EntityUtilCapStorage(), () -> new EntityUtilCap(null));
        CapabilityManager.INSTANCE.register(EntityHamonChargeCap.class, new EntityHamonChargeCapStorage(), () -> new EntityHamonChargeCap(null));
        CapabilityManager.INSTANCE.register(ProjectileHamonChargeCap.class, new ProjectileHamonChargeCapStorage(), () -> new ProjectileHamonChargeCap(null));
        
        CapabilityManager.INSTANCE.register(WorldUtilCap.class, new WorldUtilCapStorage(), () -> new WorldUtilCap(null));
        CapabilityManager.INSTANCE.register(SaveFileUtilCap.class, new SaveFileUtilCapStorage(), () -> new SaveFileUtilCap(null));

        CapabilityManager.INSTANCE.register(ChunkCap.class, new ChunkCapStorage(), () -> new ChunkCap(null));
    }
    
    
    
    @SubscribeEvent
    public static void onEntityTracking(PlayerEvent.StartTracking event) {
        Entity entityTracked = event.getTarget();
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        if (entityTracked instanceof LivingEntity) {
            LivingEntity livingTracked = (LivingEntity) entityTracked;
            INonStandPower.getNonStandPowerOptional(livingTracked).ifPresent(power -> {
                power.syncWithTrackingOrUser(player);
            });
            IStandPower.getStandPowerOptional(livingTracked).ifPresent(power -> {
                power.syncWithTrackingOrUser(player);
            });
            livingTracked.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.onTracking(player);
            });
            if (livingTracked instanceof PlayerEntity) {
                livingTracked.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.onTracking(player);
                });
            }
        }
        entityTracked.getCapability(EntityHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> {
            cap.onTracking(player);
        });
        entityTracked.getCapability(ProjectileHamonChargeCapProvider.CAPABILITY).ifPresent(cap -> {
            cap.onTracking(player);
        });
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        PlayerEntity original = event.getOriginal();
        PlayerEntity player = event.getPlayer();
        
        cloneCap(INonStandPower.getNonStandPowerOptional(original), INonStandPower.getNonStandPowerOptional(player), 
                event.isWasDeath(), "Stand capability");
        cloneCap(IStandPower.getStandPowerOptional(original), IStandPower.getStandPowerOptional(player), 
                event.isWasDeath(), "non-Stand capability");
        
        original.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(oldCap -> {
            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(newCap -> {
                newCap.onClone(oldCap, event.isWasDeath());
            });
        });
        
        original.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(oldCap -> {
            player.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(newCap -> {
                newCap.onClone(oldCap, event.isWasDeath());
            });
        });
    }
    
    private static <T extends IPower<T, ?>> void cloneCap(LazyOptional<T> oldCap, LazyOptional<T> newCap, boolean wasDeath, String warning) {
        if (oldCap.isPresent() && newCap.isPresent()) {
            newCap.resolve().get().onClone(oldCap.resolve().get(), wasDeath);
        }
        else {
            JojoMod.getLogger().warn("Failed to copy " + " data!");
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        SaveFileUtilCapProvider.getSaveFileCap(player).onPlayerLogIn(player);
        JojoModConfig.Common.SyncedValues.syncWithClient(player);
        syncPowerData(event.getPlayer());
        IStandPower.getStandPowerOptional(event.getPlayer()).ifPresent(power -> {
            if (power.hasPower()) {
                power.getType().unlockNewActions(power);
            }
        });
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
        syncPowerData(event.getPlayer());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerRespawnEvent event) {
        syncPowerData(event.getPlayer());
    }
    
    private static void syncPowerData(PlayerEntity player) {
        INonStandPower.getPlayerNonStandPower(player).syncWithUserOnly();
        IStandPower.getPlayerStandPower(player).syncWithUserOnly();
        player.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            cap.syncWithClient();
        });
        player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            cap.syncWithClient();
        });
        PacketManager.sendToClient(new UpdateClientCapCachePacket(), (ServerPlayerEntity) player);
    }
    
    
    
    @SubscribeEvent
    public static void onPlayerLogout(PlayerLoggedOutEvent event) {
        JojoModConfig.Common.SyncedValues.onPlayerLogout((ServerPlayerEntity) event.getPlayer());
    }
    
    
    
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld() instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) event.getWorld();
            addDimensionalSpacing(serverWorld);
        }
    }
    
    private static void addDimensionalSpacing(ServerWorld serverWorld) {
        ResourceLocation cgRL = Registry.CHUNK_GENERATOR.getKey(CommonReflection.getCodec(serverWorld.getChunkSource().getGenerator()));
        if (cgRL != null && cgRL.getNamespace().equals("terraforged")) {
            return;
        }
        
        if (serverWorld.getChunkSource().getGenerator() instanceof FlatChunkGenerator && serverWorld.dimension().equals(World.OVERWORLD)) {
            return;
        }

        Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(
                serverWorld.getChunkSource().getGenerator().getSettings().structureConfig());
        for (RegistryObject<Structure<?>> structure : ModStructures.STRUCTURES.getEntries()) {
            tempMap.putIfAbsent(structure.get(), DimensionStructuresSettings.DEFAULTS.get(structure.get()));
        }
        serverWorld.getChunkSource().getGenerator().getSettings().structureConfig = tempMap;
    }

    public static final Map<Supplier<StructureFeature<?, ?>>, Predicate<BiomeLoadingEvent>> structureBiomes = new HashMap<>();
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBiomeLoading(BiomeLoadingEvent event) {
        List<Supplier<StructureFeature<?, ?>>> structureStarts = event.getGeneration().getStructures();
        
        for (Map.Entry<Supplier<StructureFeature<?, ?>>, Predicate<BiomeLoadingEvent>> entry : structureBiomes.entrySet()) {
            if (entry.getValue() != null && entry.getValue().test(event)) {
                structureStarts.add(entry.getKey());
            }
        }
    }
}
