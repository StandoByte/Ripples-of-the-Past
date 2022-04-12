package com.github.standobyte.jojo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.client.ui.hud.ActionsOverlayGui;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.power.nonstand.type.HamonData;
import com.github.standobyte.jojo.power.stand.type.StandType;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class JojoModConfig {
    
    // FIXME (!!) sync common config with clients
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
        private List<ResourceLocation> bannedStandsResLocs;
        private boolean[] tiersAvaliable = new boolean[7];

        public final ForgeConfigSpec.DoubleValue hamonDamageMultiplier;
        
        public final ForgeConfigSpec.BooleanValue skipStandProgression;
        public final ForgeConfigSpec.BooleanValue standStamina;
        public final ForgeConfigSpec.DoubleValue standDamageMultiplier;
        public final ForgeConfigSpec.BooleanValue soulAscension;

        public final ForgeConfigSpec.BooleanValue abilitiesBreakBlocks;
        
        public final ForgeConfigSpec.IntValue timeStopChunkRange;
        
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
                        .defineList("bannedStands", Arrays.asList("jojo:example_1", "jojo:example_2"), s -> s instanceof String && ResourceLocation.tryParse((String) s) != null);
                standTiers = builder
                        .comment(" Whether or not the Stand tiers mechanic is enabled.")
                        .translation("jojo.config.standTiers")
                        .define("standTiers", true);
            builder.pop();
            
            skipStandProgression = builder
                    .comment(" Whether or not all of the abilities are unlocked after gaining a Stand in Survival.")
                    .translation("jojo.config.skipStandProgression")
                    .define("skipStandProgression", false);
        
            standStamina = builder
                    .comment(" Whether or not Stand stamina mechanic is enabled.")
                    .translation("jojo.config.standStamina")
                    .define("standStamina", true);
        
            standDamageMultiplier = builder
                    .comment(" Damage multiplier applied to all Stands.")
                    .translation("jojo.config.standPowerMultiplier")
                    .defineInRange("standPowerMultiplier", 1.0, 0.0, 128.0);
            
            soulAscension = builder
                    .translation("jojo.config.soulAscension")
                    .define("soulAscension", true);
            
            abilitiesBreakBlocks = builder
                    .comment(" Whether or not Stands and abilities can break blocks.")
                    .translation("jojo.config.abilitiesBreakBlocks")
                    .define("abilitiesBreakBlocks", true);
            
            hamonDamageMultiplier = builder
                    .comment(" Damage multiplier applied to all Hamon attacks.")
                    .translation("jojo.config.hamonDamageMultiplier")
                    .defineInRange("hamonDamageMultiplier", 1.0, 0.0, 128.0);
            
            timeStopChunkRange = builder
                    .comment(" Range of Time Stop ability in chunks.",
                            "  If set to 0, the whole dimension is frozen in time.",
                            "  Defaults to 12.")
                    .translation("jojo.config.timeStopChunkRange")
                    .defineInRange("timeStopChunkRange", 12, 0, Integer.MAX_VALUE);
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
        
        private void initBannedStands() {
            IForgeRegistry<StandType<?>> registry = ModStandTypes.Registry.getRegistry();
            bannedStandsResLocs = bannedStands.get()
                    .stream()
                    .map(s -> {
                        ResourceLocation resLoc = new ResourceLocation(s);
                        if ("minecraft".equals(resLoc.getNamespace())) {
                            resLoc = new ResourceLocation(JojoMod.MOD_ID, resLoc.getPath());
                        }
                        return resLoc;
                    })
                    .filter(resLoc -> registry.containsKey(resLoc))
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
    private static final Common COMMON;
    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }
    
    private static Common COMMON_REMOTE_SERVER = COMMON;
    // FIXME (!!) if on remote server - instance with values from server
    public static Common getCommonConfigInstance() {
        return COMMON;
    }

    static final ForgeConfigSpec clientSpec;
    public static final Client CLIENT;
    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }
    
    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (JojoMod.MOD_ID.equals(config.getModId()) && config.getType() == ModConfig.Type.COMMON) {
            COMMON.loaded = true;
            COMMON.initBannedStands();
        }
    }
}
