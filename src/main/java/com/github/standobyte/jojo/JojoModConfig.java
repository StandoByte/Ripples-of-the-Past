package com.github.standobyte.jojo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;
import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.ResetSyncedCommonConfigPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncCommonConfigPacket;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.stand.ResolveCounter;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class JojoModConfig {
    
    public static class Common {
        private boolean loaded = false;
        
        public final ForgeConfigSpec.BooleanValue keepStandOnDeath;
        public final ForgeConfigSpec.BooleanValue keepHamonOnDeath;
        public final ForgeConfigSpec.BooleanValue keepVampirismOnDeath;
        
        public final ForgeConfigSpec.BooleanValue hamonTempleSpawn;
        public final ForgeConfigSpec.BooleanValue meteoriteSpawn;
        public final ForgeConfigSpec.BooleanValue pillarManTempleSpawn;

        public final ForgeConfigSpec.DoubleValue hamonPointsMultiplier;
        public final ForgeConfigSpec.DoubleValue breathingTechniqueMultiplier;
        public final ForgeConfigSpec.BooleanValue breathingTechniqueDeterioration;

        public final ForgeConfigSpec.BooleanValue prioritizeLeastTakenStands;
        public final ForgeConfigSpec.BooleanValue standTiers;
        
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> bannedStands;
        private List<StandType<?>> bannedStandsSynced = null;
        private List<ResourceLocation> bannedStandsResLocs;
        private boolean[] tiersAvaliable = new boolean[7];

        public final ForgeConfigSpec.BooleanValue abilitiesBreakBlocks;
        public final ForgeConfigSpec.DoubleValue standDamageMultiplier;
        public final ForgeConfigSpec.BooleanValue skipStandProgression;
        public final ForgeConfigSpec.BooleanValue standStamina;
        public final ForgeConfigSpec.ConfigValue<List<? extends Double>> resolvePoints;
        public final ForgeConfigSpec.BooleanValue soulAscension;
        public final ForgeConfigSpec.IntValue timeStopChunkRange;

        public final ForgeConfigSpec.DoubleValue hamonDamageMultiplier;
        
        private Common(ForgeConfigSpec.Builder builder) {
            
            builder.push("Keep Powers After Death");
                keepStandOnDeath = builder
                        .translation("jojo.config.keepStandOnDeath") 
                        .define("keepStandOnDeath", false);
                
                keepHamonOnDeath = builder
                        .translation("jojo.config.keepHamonOnDeath")
                        .define("keepHamonOnDeath", false);
                
                keepVampirismOnDeath = builder
                        .translation("jojo.config.keepVampirismOnDeath")
                        .define("keepVampirismOnDeath", false);
            builder.pop();
            
            builder.push("Structures Spawn");
                hamonTempleSpawn = builder
                        .translation("jojo.config.hamonTempleSpawn")
                        .define("hamonTempleSpawn", true);
            
                meteoriteSpawn = builder
                        .translation("jojo.config.meteoriteSpawn")
                        .define("meteoriteSpawn", true);
                    
                pillarManTempleSpawn = builder
                        .translation("jojo.config.pillarManTempleSpawn")
                        .define("pillarManTempleSpawn", true);
            builder.pop();
            
            builder.comment(" Settings which affect the speed of Hamon training.").push("Hamon training");
                hamonPointsMultiplier = builder
                        .comment(" Hamon Strength and Control levels growth multiplier.")
                        .translation("jojo.config.hamonPointsMultiplier")
                        .defineInRange("hamonPointsMultiplier", 1.0, 0.0, 5000.0);
                
                breathingTechniqueMultiplier = builder
                        .comment(" Breathing technique growth multiplier.")
                        .translation("jojo.config.breathingTechniqueMultiplier")
                        .defineInRange("breathingTechniqueMultiplier", 1.0, 0.0, HamonData.MAX_BREATHING_LEVEL);
                
                breathingTechniqueDeterioration = builder
                        .comment(" Whether or not breathing technique deteriorates over time.")
                        .translation("jojo.config.breathingTechniqueDeterioration")
                        .define("breathingTechniqueDeterioration", true);
            builder.pop();
            
            builder.comment(" Settings of Stand Arrow and the Stands pool.").push("Stand Arrow");
                prioritizeLeastTakenStands = builder
                        .comment(" Whether or not random Stand gain effects (Stand Arrow, /stand random) give Stands that less players already have.")
                        .translation("jojo.config.prioritizeLeastTakenStands")
                        .define("prioritizeLeastTakenStands", false);
                
                bannedStands = builder
                        .comment(" List of Stands excluded from Stand Arrow and /stand random pool.",
                                "  These stands will still be available via /stand give command",
                                "  Their Discs won't be added to the mod's Creative tab, but they can still be found in the Search tab.\"",
                                "  The format is the same as for /stand give command (e.g., \"jojo:star_platinum\").")
                        .translation("jojo.config.bannedStands")
                        .defineListAllowEmpty(Lists.newArrayList("bannedStands"), 
                                () -> Arrays.asList("jojo:example_1", "jojo:example_2"), 
                                s -> s instanceof String && ResourceLocation.tryParse((String) s) != null);
                
                standTiers = builder
                        .comment(" Whether or not the Stand tiers mechanic is enabled.")
                        .translation("jojo.config.standTiers")
                        .define("standTiers", true);
            builder.pop();
            
            builder.push("Stand settings");
                abilitiesBreakBlocks = builder
                        .comment(" Whether or not Stands and abilities can break blocks.")
                        .translation("jojo.config.abilitiesBreakBlocks")
                        .define("abilitiesBreakBlocks", true);
            
                standDamageMultiplier = builder
                        .comment(" Damage multiplier applied to all Stands.")
                        .translation("jojo.config.standPowerMultiplier")
                        .defineInRange("standPowerMultiplier", 1.0, 0.0, 128.0);
            
                skipStandProgression = builder
                        .comment(" Whether or not all of the abilities are unlocked after gaining a Stand in Survival.")
                        .translation("jojo.config.skipStandProgression")
                        .define("skipStandProgression", false);
            
                standStamina = builder
                        .comment(" Whether or not Stand stamina mechanic is enabled.")
                        .translation("jojo.config.standStamina")
                        .define("standStamina", true);
                
                resolvePoints = builder
                        .comment(" Max resolve points at each Resolve level (starting from 0)", 
                                "  All values must be higher than 0.")
                        .translation("jojo.config.resolvePoints")
                        .defineList("resolvePoints", Arrays.asList(ResolveCounter.DEFAULT_MAX_RESOLVE_VALUES), 
                                pts -> {
                                    if (pts instanceof Double) {
                                        Double num = (Double) pts;
                                        return num > 0 && Float.isFinite(num.floatValue());
                                    }
                                    return false;
                                });
                
                soulAscension = builder
                        .translation("jojo.config.soulAscension")
                        .define("soulAscension", true);
                
                timeStopChunkRange = builder
                        .comment(" Range of Time Stop ability in chunks.",
                                "  If set to 0, the whole dimension is frozen in time.",
                                "  Defaults to 12.")
                        .translation("jojo.config.timeStopChunkRange")
                        .defineInRange("timeStopChunkRange", 12, 0, Integer.MAX_VALUE);
            builder.pop();
            
            hamonDamageMultiplier = builder
                    .comment(" Damage multiplier applied to all Hamon attacks.")
                    .translation("jojo.config.hamonDamageMultiplier")
                    .defineInRange("hamonDamageMultiplier", 1.0, 0.0, 128.0);
        }
        
        public boolean isConfigLoaded() {
            return loaded;
        }
        
        public boolean inTimeStopRange(ChunkPos center, ChunkPos pos) {
            int range = timeStopChunkRange.get();
            if (range <= 0) {
                return true;
            }
            return Math.abs(center.x - pos.x) < range && Math.abs(center.z - pos.z) < range;
        }
        
        private void onLoadOrReload() {
            loaded = true;
            initBannedStands();
        }
        
        private void initBannedStands() {
            IForgeRegistry<StandType<?>> registry = ModStandTypes.Registry.getRegistry();

            
            Stream<ResourceLocation> resLocs = bannedStandsSynced != null ? 
                    bannedStandsSynced.stream()
                    .map(StandType::getRegistryName)
                    
                    : bannedStands.get().stream()
                    .map(s -> {
                        ResourceLocation resLoc = new ResourceLocation(s);
                        if ("minecraft".equals(resLoc.getNamespace())) {
                            resLoc = new ResourceLocation(JojoMod.MOD_ID, resLoc.getPath());
                        }
                        return resLoc;
                    })
                    .filter(resLoc -> registry.containsKey(resLoc));
            
            bannedStandsResLocs = resLocs
                    .collect(Collectors.toList());
            
            
            tiersAvaliable = new boolean[7];
            registry.getValues()
            .stream()
            .filter(stand -> !bannedStandsResLocs.contains(stand.getRegistryName()))
            .map(StandType::getTier)
            .distinct()
            .forEach(tier -> tiersAvaliable[tier] = true);
        }
        
        public boolean isStandBanned(StandType<?> stand) {
            return bannedStandsResLocs.contains(stand.getRegistryName());
        }
        
        public boolean tierHasUnbannedStands(int tier) {
            return tiersAvaliable[tier];
        }
        
        

        public static class SyncedValues {
            private final boolean keepStandOnDeath;
            private final boolean keepHamonOnDeath;
            private final boolean keepVampirismOnDeath;
            
            private final boolean hamonTempleSpawn;
            private final boolean meteoriteSpawn;
            private final boolean pillarManTempleSpawn;

//            private final double hamonPointsMultiplier;
//            private final double breathingTechniqueMultiplier;
            private final boolean breathingTechniqueDeterioration;

            private final boolean prioritizeLeastTakenStands;
            private final boolean standTiers;
            private final List<StandType<?>> bannedStands;

            private final boolean abilitiesBreakBlocks;
//            private final double standDamageMultiplier;
            private final boolean skipStandProgression;
            private final boolean standStamina;
            private final float[] resolvePoints;
            private final boolean soulAscension;
            private final int timeStopChunkRange;

//            private final double hamonDamageMultiplier;
            
            public SyncedValues(PacketBuffer buf) {
//                hamonPointsMultiplier = buf.readDouble();
//                breathingTechniqueMultiplier = buf.readDouble();
                bannedStands = NetworkUtil.readRegistryIdsSafe(buf, StandType.class);
//                standDamageMultiplier = buf.readDouble();
                resolvePoints = NetworkUtil.readFloatArray(buf);
                timeStopChunkRange = buf.readVarInt();
                byte[] flags = buf.readByteArray();
                keepStandOnDeath =                  (flags[0] & 1) > 0;
                keepHamonOnDeath =                  (flags[0] & 2) > 0;
                keepVampirismOnDeath =              (flags[0] & 4) > 0;
                hamonTempleSpawn =                  (flags[0] & 8) > 0;
                meteoriteSpawn =                    (flags[0] & 16) > 0;
                pillarManTempleSpawn =              (flags[0] & 32) > 0;
                breathingTechniqueDeterioration =   (flags[0] & 64) > 0;
                prioritizeLeastTakenStands =        (flags[0] & 128) > 0;
                standTiers =                        (flags[1] & 1) > 0;
                abilitiesBreakBlocks =              (flags[1] & 2) > 0;
                skipStandProgression =              (flags[1] & 4) > 0;
                standStamina =                      (flags[1] & 8) > 0;
                soulAscension =                     (flags[1] & 16) > 0;
            }

            public void writeToBuf(PacketBuffer buf) {
//                buf.writeDouble(hamonPointsMultiplier);
//                buf.writeDouble(breathingTechniqueMultiplier);
                NetworkUtil.writeRegistryIds(buf, bannedStands);
//                buf.writeDouble(standDamageMultiplier);
                NetworkUtil.writeFloatArray(buf, resolvePoints);
                buf.writeVarInt(timeStopChunkRange);
                byte[] flags = new byte[] {0, 0};
                if (keepStandOnDeath)                   flags[0] |= 1;
                if (keepHamonOnDeath)                   flags[0] |= 2;
                if (keepVampirismOnDeath)               flags[0] |= 4;
                if (hamonTempleSpawn)                   flags[0] |= 8;
                if (meteoriteSpawn)                     flags[0] |= 16;
                if (pillarManTempleSpawn)               flags[0] |= 32;
                if (breathingTechniqueDeterioration)    flags[0] |= 64;
                if (prioritizeLeastTakenStands)         flags[0] |= 128;
                if (standTiers)                         flags[1] |= 1;
                if (abilitiesBreakBlocks)               flags[1] |= 2;
                if (skipStandProgression)               flags[1] |= 4;
                if (standStamina)                       flags[1] |= 8;
                if (soulAscension)                      flags[1] |= 16;
                buf.writeByteArray(flags);
            }

            private SyncedValues(Common config) {
                keepStandOnDeath = config.keepStandOnDeath.get();
                keepHamonOnDeath = config.keepHamonOnDeath.get();
                keepVampirismOnDeath = config.keepVampirismOnDeath.get();
                hamonTempleSpawn = config.hamonTempleSpawn.get();
                meteoriteSpawn = config.meteoriteSpawn.get();
                pillarManTempleSpawn = config.pillarManTempleSpawn.get();
//                hamonPointsMultiplier = config.standDamageMultiplier.get();
//                breathingTechniqueMultiplier = config.breathingTechniqueMultiplier.get();
                breathingTechniqueDeterioration = config.breathingTechniqueDeterioration.get();
                prioritizeLeastTakenStands = config.prioritizeLeastTakenStands.get();
                standTiers = config.standTiers.get();
                bannedStands = config.bannedStandsResLocs.stream()
                        .map(key -> ModStandTypes.Registry.getRegistry().getValue(key))
                        .collect(Collectors.toList());
                abilitiesBreakBlocks = config.abilitiesBreakBlocks.get();
//                standDamageMultiplier = config.standDamageMultiplier.get()
                skipStandProgression = config.skipStandProgression.get();
                standStamina = config.standStamina.get();
                resolvePoints = Floats.toArray(config.resolvePoints.get());
                soulAscension = config.soulAscension.get();
                timeStopChunkRange = config.timeStopChunkRange.get();
            }
            
            public void changeConfigValues() {
                COMMON_SYNCED_TO_CLIENT.keepStandOnDeath.set(keepStandOnDeath);
                COMMON_SYNCED_TO_CLIENT.keepHamonOnDeath.set(keepHamonOnDeath);
                COMMON_SYNCED_TO_CLIENT.keepVampirismOnDeath.set(keepVampirismOnDeath);
                COMMON_SYNCED_TO_CLIENT.hamonTempleSpawn.set(hamonTempleSpawn);
                COMMON_SYNCED_TO_CLIENT.meteoriteSpawn.set(meteoriteSpawn);
                COMMON_SYNCED_TO_CLIENT.pillarManTempleSpawn.set(pillarManTempleSpawn);
//                COMMON_SYNCED_TO_CLIENT.hamonPointsMultiplier.set(hamonPointsMultiplier);
//                COMMON_SYNCED_TO_CLIENT.breathingTechniqueMultiplier.set(breathingTechniqueMultiplier);
                COMMON_SYNCED_TO_CLIENT.breathingTechniqueDeterioration.set(breathingTechniqueDeterioration);
                COMMON_SYNCED_TO_CLIENT.prioritizeLeastTakenStands.set(prioritizeLeastTakenStands);
                COMMON_SYNCED_TO_CLIENT.standTiers.set(standTiers);
                COMMON_SYNCED_TO_CLIENT.bannedStandsSynced = bannedStands;
                COMMON_SYNCED_TO_CLIENT.abilitiesBreakBlocks.set(abilitiesBreakBlocks);
//                COMMON_SYNCED_TO_CLIENT.standDamageMultiplier.set(standDamageMultiplier);
                COMMON_SYNCED_TO_CLIENT.skipStandProgression.set(skipStandProgression);
                COMMON_SYNCED_TO_CLIENT.standStamina.set(standStamina);
                COMMON_SYNCED_TO_CLIENT.resolvePoints.set(Floats.asList(resolvePoints).stream().map(Float::doubleValue).collect(Collectors.toList()));
                COMMON_SYNCED_TO_CLIENT.soulAscension.set(soulAscension);
                COMMON_SYNCED_TO_CLIENT.timeStopChunkRange.set(timeStopChunkRange);
                
                COMMON_SYNCED_TO_CLIENT.onLoadOrReload();
            }

            public static void resetConfig() {
                COMMON_SYNCED_TO_CLIENT.keepStandOnDeath.clearCache();
                COMMON_SYNCED_TO_CLIENT.keepHamonOnDeath.clearCache();
                COMMON_SYNCED_TO_CLIENT.keepVampirismOnDeath.clearCache();
                COMMON_SYNCED_TO_CLIENT.hamonTempleSpawn.clearCache();
                COMMON_SYNCED_TO_CLIENT.meteoriteSpawn.clearCache();
                COMMON_SYNCED_TO_CLIENT.pillarManTempleSpawn.clearCache();
//                COMMON_SYNCED_TO_CLIENT.hamonPointsMultiplier.clearCache();
//                COMMON_SYNCED_TO_CLIENT.breathingTechniqueMultiplier.clearCache();
                COMMON_SYNCED_TO_CLIENT.breathingTechniqueDeterioration.clearCache();
                COMMON_SYNCED_TO_CLIENT.prioritizeLeastTakenStands.clearCache();
                COMMON_SYNCED_TO_CLIENT.standTiers.clearCache();
                COMMON_SYNCED_TO_CLIENT.bannedStandsSynced = null;
                COMMON_SYNCED_TO_CLIENT.abilitiesBreakBlocks.clearCache();
//                COMMON_SYNCED_TO_CLIENT.standDamageMultiplier.clearCache();
                COMMON_SYNCED_TO_CLIENT.skipStandProgression.clearCache();
                COMMON_SYNCED_TO_CLIENT.standStamina.clearCache();
                COMMON_SYNCED_TO_CLIENT.resolvePoints.clearCache();
                COMMON_SYNCED_TO_CLIENT.soulAscension.clearCache();
                COMMON_SYNCED_TO_CLIENT.timeStopChunkRange.clearCache();
                
                COMMON_SYNCED_TO_CLIENT.onLoadOrReload();
            }

            
            
            public static void syncWithClient(ServerPlayerEntity player) {
                PacketManager.sendToClient(new SyncCommonConfigPacket(new SyncedValues(COMMON_FROM_FILE)), player);
            }
            
            public static void onPlayerLogout(ServerPlayerEntity player) {
                PacketManager.sendToClient(new ResetSyncedCommonConfigPacket(), player);
            }
        }
    }
    
    
    
    public static class Client {
        
        public final ForgeConfigSpec.EnumValue<ActionsOverlayGui.PositionConfig> barsPosition;
        public final ForgeConfigSpec.EnumValue<ActionsOverlayGui.PositionConfig> hotbarsPosition;
        
        public final ForgeConfigSpec.BooleanValue slotHotkeys;
        
        public final ForgeConfigSpec.BooleanValue resolveShaders;
        
        private Client(ForgeConfigSpec.Builder builder) {
            barsPosition = builder
                    .comment(" Position of Energy, Stamina and Resolve bars in the HUD.")
                    .translation("jojo.config.client.barsPosition") 
                    .defineEnum("barsPosition", ActionsOverlayGui.PositionConfig.TOP_LEFT);
            
            hotbarsPosition = builder
                    .comment(" Position of Power name, Attack and Ability hotbars in the HUD.")
                    .translation("jojo.config.client.hotbarsPosition")
                    .defineEnum("hotbarsPosition", ActionsOverlayGui.PositionConfig.TOP_LEFT);
            
            slotHotkeys = builder
                    .comment(" Enable hotkey settings for each individual attack and ability from 1 to 9.", 
                            "  If your client is launched, changing the setting requires restarting the game.")
                    .translation("jojo.config.client.slotHotkeys")
                    .define("slotHotkeys", true);
            
            resolveShaders = builder
                    .comment(" Enable shaders during Resolve effect.")
                    .translation("jojo.config.client.resolveShaders")
                    .define("resolveShaders", true);
        }
    }


    static final ForgeConfigSpec commonSpec;
    private static final Common COMMON_FROM_FILE;
    private static final Common COMMON_SYNCED_TO_CLIENT;
    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON_FROM_FILE = specPair.getLeft();

        final Pair<Common, ForgeConfigSpec> syncedSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
        syncedSpecPair.getRight().setConfig(CommentedConfig.of(InMemoryCommentedFormat.defaultInstance()));
        COMMON_SYNCED_TO_CLIENT = syncedSpecPair.getLeft();
    }
    
    public static Common getCommonConfigInstance(boolean isClientSide) {
        return isClientSide ? COMMON_SYNCED_TO_CLIENT : COMMON_FROM_FILE;
    }

    static final ForgeConfigSpec clientSpec;
    public static final Client CLIENT;
    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }
    
    @SubscribeEvent
    public static void onConfigLoad(ModConfig.ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (JojoMod.MOD_ID.equals(config.getModId()) && config.getType() == ModConfig.Type.COMMON) {
            COMMON_FROM_FILE.onLoadOrReload();
        }
    }
    
    @SubscribeEvent
    public static void onConfigReload(ModConfig.Reloading event) {
        ModConfig config = event.getConfig();
        if (JojoMod.MOD_ID.equals(config.getModId()) && config.getType() == ModConfig.Type.COMMON) {
            // FIXME sync the config to all players on the server
        }
    }
}
