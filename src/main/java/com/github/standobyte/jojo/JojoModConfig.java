package com.github.standobyte.jojo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

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

    public static class Common {
        private boolean loaded = false;
        
        public final ForgeConfigSpec.BooleanValue keepStandOnDeath;
        public final ForgeConfigSpec.BooleanValue keepNonStandOnDeath;

        public final ForgeConfigSpec.BooleanValue abilitiesBreakBlocks;
        
        public final ForgeConfigSpec.IntValue timeStopChunkRange;

        public final ForgeConfigSpec.BooleanValue prioritizeLeastTakenStands;
        public final ForgeConfigSpec.BooleanValue standTiers;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> bannedStands;
        private List<ResourceLocation> bannedStandsResLocs;
        private boolean[] tiersAvaliable = new boolean[7];

        public final ForgeConfigSpec.DoubleValue hamonPointsMultiplier;
        public final ForgeConfigSpec.DoubleValue breathingTechniqueMultiplier;
        public final ForgeConfigSpec.BooleanValue breathingTechniqueDeterioration;
        
        public final ForgeConfigSpec.BooleanValue hamonTempleSpawn;
        public final ForgeConfigSpec.BooleanValue meteoriteSpawn;
        public final ForgeConfigSpec.BooleanValue pillarmanTempleSpawn;
        
        Common(ForgeConfigSpec.Builder builder) {
            keepStandOnDeath = builder
                    .comment(" Keep Stand after death.")
                    .translation("jojo.config.keepStandOnDeath") 
                    .define("keepStandOnDeath", false);
            
            keepNonStandOnDeath = builder
                    .comment(" Keep powers (Vampirism/Hamon) after death.")
                    .translation("jojo.config.keepNonStandOnDeath")
                    .define("keepNonStandOnDeath", false);
            
            abilitiesBreakBlocks = builder
                    .comment(" Whether or not Stands and abilities can break blocks.")
                    .translation("jojo.config.abilitiesBreakBlocks")
                    .define("abilitiesBreakBlocks", true);
            
            timeStopChunkRange = builder
                    .comment(" Range of Time Stop ability in chunks.",
                            "  If set to 0, the whole dimension is frozen in time.",
                            "  Defaults to 12.")
                    .translation("jojo.config.timeStopChunkRange")
                    .defineInRange("timeStopChunkRange", 12, 0, Integer.MAX_VALUE);
            
//            builder.push("Stand Arrow");
                prioritizeLeastTakenStands = builder
                        .comment(" Whether or not random Stand gain effects (Stand Arrow, /stand random) give Stands that less players already have.")
                        .translation("jojo.config.prioritizeLeastTakenStands")
                        .define("prioritizeLeastTakenStands", false);
                bannedStands = builder
                        .comment(" List of Stands excluded from Stand Arrow and /stand random pool.",
                                "  These stands will still be avaliable via /stand give command",
                                "  Their Discs won't be added to the mod's Creative tab, but they can still be found in the Search tab.\"",
                                "  The format is the same as for /stand give command (e.g., \"jojo:star_platinum\").")
                        .translation("jojo.config.bannedStands")
                        .defineList("bannedStands", Arrays.asList("jojo:example_1", "jojo:example_2"), s -> s instanceof String && ResourceLocation.tryParse((String) s) != null);
                standTiers = builder
                        .comment(" Set this to false to disable the Stand tiers mechanic.")
                        .translation("jojo.config.standTiers")
                        .define("standTiers", true);
//            builder.pop();
            
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
            builder.pop()
            
            .push("Structures Spawn");
                hamonTempleSpawn = builder
                        .translation("jojo.config.hamonTempleSpawn")
                        .define("hamonTempleSpawn", true);
            
                meteoriteSpawn = builder
                        .translation("jojo.config.meteoriteSpawn")
                        .define("meteoriteSpawn", true);
                    
                pillarmanTempleSpawn = builder
                        .translation("jojo.config.pillarmanTempleSpawn")
                        .define("pillarmanTempleSpawn", true);
            builder.pop();
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
            IForgeRegistry<StandType> registry = ModStandTypes.Registry.getRegistry();
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
        
        public boolean isStandBanned(StandType stand) {
            return bannedStandsResLocs.contains(stand.getRegistryName());
        }
        
        public boolean tierHasUnbannedStands(int tier) {
            return tiersAvaliable[tier];
        }
    }


    static final ForgeConfigSpec commonSpec;
    public static final Common COMMON;
    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }
    
    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (config.getType() == ModConfig.Type.COMMON && JojoMod.MOD_ID.equals(config.getModId())) {
            COMMON.loaded = true;
            COMMON.initBannedStands();
        }
    }
}
