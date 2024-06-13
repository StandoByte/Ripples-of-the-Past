package com.github.standobyte.jojo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.CommonConfigPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ResetSyncedCommonConfigPacket;
import com.github.standobyte.jojo.power.impl.nonstand.type.hamon.HamonData;
import com.github.standobyte.jojo.power.impl.stand.ResolveCounter;
import com.github.standobyte.jojo.power.impl.stand.StandUtil;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class JojoModConfig {
    
    public static class Common {
        private boolean loaded = false;
        
        public final ForgeConfigSpec.BooleanValue keepStandOnDeath;
        public final ForgeConfigSpec.BooleanValue keepHamonOnDeath;
        public final ForgeConfigSpec.BooleanValue keepVampirismOnDeath;
        public final ForgeConfigSpec.BooleanValue dropStandDisc;
        
        public final ForgeConfigSpec.BooleanValue hamonTempleSpawn;
        public final ForgeConfigSpec.BooleanValue meteoriteSpawn;
        public final ForgeConfigSpec.BooleanValue pillarManTempleSpawn;
        
        public final ForgeConfigSpec.DoubleValue hamonDamageMultiplier;
        public final ForgeConfigSpec.BooleanValue hamonEnergyTicksDown;
        
        public final ForgeConfigSpec.DoubleValue hamonPointsMultiplier;
        public final ForgeConfigSpec.DoubleValue breathingTrainingMultiplier;
        public final ForgeConfigSpec.BooleanValue breathingTrainingDeterioration;
        public final ForgeConfigSpec.IntValue breathingHamonStatGap;
        public final ForgeConfigSpec.BooleanValue mixHamonTechniques;
        public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> techniqueSkillsRequirement;
        
        public final ForgeConfigSpec.ConfigValue<List<? extends Double>> maxBloodMultiplier;
        public final ForgeConfigSpec.ConfigValue<List<? extends Double>> bloodDrainMultiplier;
        public final ForgeConfigSpec.ConfigValue<List<? extends Double>> bloodTickDown;
        public final ForgeConfigSpec.ConfigValue<List<? extends Double>> bloodHealCost;
        public final ForgeConfigSpec.BooleanValue vampiresAggroMobs;
        public final ForgeConfigSpec.BooleanValue undeadMobsSunDamage;
        public final ForgeConfigSpec.IntValue vampirismCuringDuration;

        public final ForgeConfigSpec.IntValue standXpCostInitial;
        public final ForgeConfigSpec.IntValue standXpCostIncrease;
        public final ForgeConfigSpec.EnumValue<StandUtil.StandRandomPoolFilter> standRandomPoolFilter;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> bannedStands;
        private List<StandType<?>> bannedStandsSynced = null;
        private List<ResourceLocation> bannedStandsResLocs;

//        public final ForgeConfigSpec.BooleanValue abilitiesBreakBlocks;
        public final ForgeConfigSpec.DoubleValue standDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue standResistanceMultiplier;
        public final ForgeConfigSpec.BooleanValue skipStandProgression;
        public final ForgeConfigSpec.BooleanValue standStamina;
        public final ForgeConfigSpec.ConfigValue<List<? extends Double>> resolveLvlPoints;
        public final ForgeConfigSpec.BooleanValue soulAscension;
        public final ForgeConfigSpec.IntValue timeStopChunkRange;
        public final ForgeConfigSpec.DoubleValue timeStopDamageMultiplier;
        
        public final ForgeConfigSpec.BooleanValue endermenBeyondTimeSpace;
        public final ForgeConfigSpec.BooleanValue saveDestroyedBlocks;
        
        private Common(ForgeConfigSpec.Builder builder) {
            this(builder, null);
        }
        
        private Common(ForgeConfigSpec.Builder builder, @Nullable String mainPath) {
            if (mainPath != null) {
                builder.push(mainPath);
            }
            
            builder.push("Keep Powers After Death");
                keepHamonOnDeath = builder
                        .translation("jojo.config.keepHamonOnDeath")
                        .define("keepHamonOnDeath", true);
                
                keepVampirismOnDeath = builder
                        .comment("    The weak vampirism version from the 'Blood Gift' ability will not be kept.")
                        .translation("jojo.config.keepVampirismOnDeath")
                        .define("keepVampirismOnDeath", true);
                
                keepStandOnDeath = builder
                        .translation("jojo.config.keepStandOnDeath") 
                        .define("keepStandOnDeath", true);
            
                dropStandDisc = builder
                        .comment("    If enabled, Stand users drop their Stand's Disc upon death.", 
                                 "    Works only when keepStandOnDeath is set to false.")
                        .translation("jojo.config.dropStandDisc")
                        .define("dropStandDisc", false);
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
            
            builder.push("Hamon Settings");
                hamonDamageMultiplier = builder
                        .comment("    Damage multiplier applied to all Hamon attacks.")
                        .translation("jojo.config.hamonDamageMultiplier")
                        .defineInRange("hamonDamageMultiplier", 1.0, 0.0, 128.0);
                
                hamonEnergyTicksDown = builder
                        .comment("    Whether or not Hamon energy ticks down a few seconds after the user performs Hamon Breath.")
                        .translation("jojo.config.hamonEnergyTicksDown")
                        .define("hamonEnergyTicksDown", true);
            
                builder.comment(" Settings which affect the speed of Hamon training.").push("Hamon training");
                    hamonPointsMultiplier = builder
                            .comment("    Hamon Strength and Control levels growth multiplier.")
                            .translation("jojo.config.hamonPointsMultiplier")
                            .defineInRange("hamonPointsMultiplier", 1.0, 0.0, 5000.0);
                    
                    breathingTrainingMultiplier = builder
                            .comment("    Breathing training growth multiplier.")
                            .translation("jojo.config.breathingTrainingMultiplier")
                            .defineInRange("breathingTrainingMultiplier", 1.0, 0.0, HamonData.MAX_BREATHING_LEVEL);
                    
                    breathingTrainingDeterioration = builder
                            .comment("    Whether or not breathing training deteriorates over time.")
                            .translation("jojo.config.breathingTrainingDeterioration")
                            .define("breathingTrainingDeterioration", true);
                    
                    breathingHamonStatGap = builder
                            .comment("    If enabled, this number will be the maximum difference between Hamon Strength/Control and Breathing training.",
                                    "     If the Breathing training level is too low, the player won't be able to reach higher levels of the Hamon stats.",
                                    "     By default this mechanic is disabled (the value is set to -1).")
                           .translation("jojo.config.breathingStatGap")
                           .defineInRange("breathingHamonStatGap", -1, -1, (int) HamonData.MAX_BREATHING_LEVEL);
                    
                    mixHamonTechniques = builder
                            .comment("    Whether or not picking skills from different character-specific Hamon techniques is allowed.")
                            .translation("jojo.config.mixHamonTechniques")
                            .define("mixHamonTechniques", false);
                    
                    techniqueSkillsRequirement = builder
                            .comment("    At what levels of Hamon Strength and Hamon Control each slot for a skill from the Technique tab is unlocked, and how many slots are there.", 
                                    "     Could be used to increase the default limit of 3 slots when paired with mixHamonTechniques setting enabled.")
                            .translation("jojo.config.techniqueSkillRequirements")
                            .defineListAllowEmpty(Lists.newArrayList("techniqueSkillRequirements"), 
                                    () -> Arrays.asList(20, 30, 40), 
                                    s -> s instanceof Integer && (Integer) s >= 0);
                builder.pop();
            builder.pop();
            
            builder.push("Vampirism settings");
                maxBloodMultiplier = builder
                        .comment("    Max vampire energy multiplier on each difficulty level (from Peaceful to Hard).")
                        .translation("jojo.config.maxBloodMultiplier")
                        .defineList("maxBloodMultiplier", Arrays.asList(1D, 1D, 1D, 1D), e -> isElementNonNegativeFloat(e, true));

                bloodDrainMultiplier = builder
                        .comment("    Blood drain multiplier on each difficulty level.")
                        .translation("jojo.config.bloodDrainMultiplier")
                        .defineList("bloodDrainMultiplier", Arrays.asList(0D, 1D, 1.75D, 2.5D), e -> isElementNonNegativeFloat(e, false));

                bloodTickDown = builder
                        .comment("    Vampire energy decrease per tick on each difficulty level.")
                        .translation("jojo.config.bloodTickDown")
                        .defineList("bloodTickDown", Arrays.asList(0.13889D, 0.00278D, 0.00278D, 0D), e -> isElementNonNegativeFloat(e, false));

                bloodHealCost = builder
                        .comment("    Vampire energy cost per 1 hp of healing on each difficulty level.")
                        .translation("jojo.config.bloodHealCost")
                        .defineList("bloodHealCost", Arrays.asList(10D, 4D, 2D, 1D), e -> isElementNonNegativeFloat(e, false));
                
                vampiresAggroMobs = builder
                        .comment("    Whether or not hostile mobs are agressive towards vampire players.")
                        .translation("jojo.config.vampiresAggroMobs")
                        .define("vampiresAggroMobs", false);
                
                undeadMobsSunDamage = builder
                        .comment("    Whether or not undead mobs take damage under the sun similarly to vampires.")
                        .translation("jojo.config.undeadMobsSunDamage")
                        .define("undeadMobsSunDamage", false);
                
                vampirismCuringDuration = builder
                        .comment("    Duration of the vampirism curing process in ticks.")
                        .translation("jojo.config.vampirismCuringDuration")
                        .defineInRange("vampirismCuringDuration", 48000, 1, Integer.MAX_VALUE);
            builder.pop();
            
            builder.comment(" Settings of Stand-giving Arrows and the commands giving Stands at random.").push("Arrow");
                standXpCostInitial = builder
                        .comment("    The initial cost of getting a Stand from a Stand Arrow (in experience levels).")
                        .translation("jojo.config.standXpCostInitial")
                        .defineInRange("standXpCostInitial", 30, 0, 9999);

                standXpCostIncrease = builder
                        .comment("    The increase of the cost for getting a Stand for each previous one the player has got before.")
                        .translation("jojo.config.standXpCostIncrease")
                        .defineInRange("standXpCostIncrease", 5, 0, 9999);
                
                standRandomPoolFilter = builder
                        .comment("    Special rule limiting the pool of Stands randomly chosen from on multiplayer servers.", 
                                 "     NONE -        can randomly give any of the available Stands", 
                                 "     LEAST_TAKEN - can only choose from the Stands less players on the server have", 
                                 "     NOT_TAKEN -   can only give a Stand no other player on the server has gotten")
                        .translation("jojo.config.standArrowMode")
                        .defineEnum("standArrowMode", StandUtil.StandRandomPoolFilter.NONE);
                
                bannedStands = builder
                        .comment("    List of Stands excluded from the pool used by Arrows, \"/stand random\" and \"/standdisc random\".",
                                 "     These stands will still be available via commands such as \"/stand give\", but won't be suggested for autocompletion.",
                                 "     Their Discs won't be added to the mod's Creative tab, but they can still be found in the Search tab (although they can't be used to gain a banned Stand).\"",
                                 "     The format is the same as for \"/stand give\" command (e.g., \"jojo:star_platinum\").")
                        .translation("jojo.config.bannedStands")
                        .defineListAllowEmpty(Lists.newArrayList("bannedStands"), 
                                () -> Arrays.asList("jojo:example_1", "jojo:example_2"), 
                                s -> s instanceof String && ResourceLocation.tryParse((String) s) != null);
            builder.pop();
            
            builder.push("Stand settings");
                builder.push("Stand Progression");
                    skipStandProgression = builder
                            .comment("    Whether or not all of the abilities are unlocked after gaining a Stand in Survival.")
                            .translation("jojo.config.skipStandProgression")
                            .define("skipStandProgression", false);
                    
                    resolveLvlPoints = builder
                            .comment("    Max resolve points at each Resolve level (starting from 0).", 
                                     "     Decrease these values to make getting to each level easier.", 
                                     "     All values must be higher than 0.")
                            .translation("jojo.config.resolveLvlPoints")
                            .defineList("resolveLvlPoints", Arrays.asList(ResolveCounter.DEFAULT_MAX_RESOLVE_VALUES), e -> isElementNonNegativeFloat(e, true));
                builder.pop();

                builder.push("Time Stop");
                    timeStopChunkRange = builder
                            .comment("    Range of Time Stop ability in chunks.",
                                     "     If set to 0, the whole dimension is frozen in time.",
                                     "     Defaults to 12.")
                            .translation("jojo.config.timeStopChunkRange")
                            .defineInRange("timeStopChunkRange", 12, 0, Integer.MAX_VALUE);
                    
                    timeStopDamageMultiplier = builder
                            .comment("    Damage multiplier for entities frozen in time.")
                            .translation("jojo.config.timeStopDamageMultiplier")
                            .defineInRange("timeStopDamageMultiplier", 1.0, 0.0, 1.0);
                builder.pop();
                
//                abilitiesBreakBlocks = builder
//                        .comment("    Whether or not Stands and abilities can break blocks.")
//                        .translation("jojo.config.abilitiesBreakBlocks")
//                        .define("abilitiesBreakBlocks", true);
            
                standDamageMultiplier = builder
                        .comment("    Damage multiplier applied to all Stands.")
                        .translation("jojo.config.standPowerMultiplier")
                        .defineInRange("standPowerMultiplier", 1.0, 0.0, 128.0);
            
                standResistanceMultiplier = builder
                        .comment("    Damage resistance applied to all Stands.")
                        .translation("jojo.config.standResistanceMultiplier")
                        .defineInRange("standResistanceMultiplier", 1.0, 1.0, 128.0);
            
                standStamina = builder
                        .comment("    Whether or not Stand stamina mechanic is enabled.")
                        .translation("jojo.config.standStamina")
                        .define("standStamina", true);
                
                soulAscension = builder
                        .translation("jojo.config.soulAscension")
                        .define("soulAscension", true);
            builder.pop();
            
            saveDestroyedBlocks = builder
                    .comment("    Whether or not blocks which can be restored by Crazy Diamond's terrain restoration ability are saved in the save files.", 
                             "     It may cause longer saving & loading time for larger worlds.")
                    .translation("jojo.config.saveDestroyedBlocks")
                    .define("saveDestroyedBlocks", false);
            
            endermenBeyondTimeSpace = builder
                    .comment("    Disable this to make endermen also be frozen in stopped time.",
                            "      But what if there is an in-mod lore reason for this...")
                    .translation("jojo.config.endermenBeyondTimeSpace")
                    .define("endermenBeyondTimeSpace", true);
            
            if (mainPath != null) {
                builder.pop();
            }
        }
        
        public boolean isConfigLoaded() {
            return loaded;
        }
        
        private void onLoadOrReload() {
            loaded = true;
            initBannedStands();
        }
        
        @Deprecated
        public void onStatsDataPackLoad() {
//            initBannedStands();
        }
        
        private void initBannedStands() {
            IForgeRegistry<StandType<?>> registry = JojoCustomRegistries.STANDS.getRegistry();
            
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
        }
        
        public boolean isStandBanned(StandType<?> stand) {
            return bannedStandsResLocs.contains(stand.getRegistryName());
        }
        
        

        public static class SyncedValues {
            private final boolean keepStandOnDeath;
            private final boolean keepHamonOnDeath;
            private final boolean keepVampirismOnDeath;
            
            private final boolean hamonTempleSpawn;
            private final boolean meteoriteSpawn;
            private final boolean pillarManTempleSpawn;
            
//            private final double hamonDamageMultiplier;
            private final boolean hamonEnergyTicksDown;
          
//            private final double hamonPointsMultiplier;
//            private final double breathingTrainingMultiplier;
            private final boolean breathingTrainingDeterioration;
            private final int breathingStatGap;
            private final boolean mixHamonTechniques;
            private final int[] techniqueSkillsRequirement;

            private final float[] maxBloodMultiplier;
//            private final float[] bloodDrainMultiplier;
            private final float[] bloodTickDown;
//            private final float[] bloodHealCost;

            private final int standXpCostInitial;
            private final int standXpCostIncrease;
            private final StandUtil.StandRandomPoolFilter standRandomPoolMode;
            private final List<StandType<?>> bannedStands;
            
//            private final boolean abilitiesBreakBlocks;
//            private final double standDamageMultiplier;
            private final boolean skipStandProgression;
            private final boolean standStamina;
            private final boolean dropStandDisc;
            private final float[] resolvePoints;
            private final boolean soulAscension;
            private final int timeStopChunkRange;
            
            private final boolean endermenBeyondTimeSpace;
            
            public SyncedValues(PacketBuffer buf) {
//                hamonPointsMultiplier = buf.readDouble();
//                breathingTrainingMultiplier = buf.readDouble();
                breathingStatGap = buf.readVarInt();
                techniqueSkillsRequirement = buf.readVarIntArray();
                
                maxBloodMultiplier = NetworkUtil.readFloatArray(buf);
//                bloodDrainMultiplier = NetworkUtil.readFloatArray(buf);
                bloodTickDown = NetworkUtil.readFloatArray(buf);
//                bloodHealCost = NetworkUtil.readFloatArray(buf);
                
                standXpCostInitial = buf.readVarInt();
                standXpCostIncrease = buf.readVarInt();
                standRandomPoolMode = buf.readEnum(StandUtil.StandRandomPoolFilter.class);
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
                breathingTrainingDeterioration =    (flags[0] & 64) > 0;
//                _ =                      (flags[0] & 128) > 0;
                
//                abilitiesBreakBlocks =              (flags[1] & 1) > 0;
                skipStandProgression =              (flags[1] & 2) > 0;
                standStamina =                      (flags[1] & 4) > 0;
                dropStandDisc =                     (flags[1] & 8) > 0;
                soulAscension =                     (flags[1] & 16) > 0;
                endermenBeyondTimeSpace =           (flags[1] & 32) > 0;
                mixHamonTechniques =                (flags[1] & 64) > 0;
                hamonEnergyTicksDown =              (flags[1] & 128) > 0;
            }

            public void writeToBuf(PacketBuffer buf) {
//                buf.writeDouble(hamonPointsMultiplier);
//                buf.writeDouble(breathingTrainingMultiplier);
                buf.writeVarInt(breathingStatGap);
                buf.writeVarIntArray(techniqueSkillsRequirement);
                
                NetworkUtil.writeFloatArray(buf, maxBloodMultiplier);
//                NetworkUtil.writeFloatArray(buf, bloodDrainMultiplier);
                NetworkUtil.writeFloatArray(buf, bloodTickDown);
//                NetworkUtil.writeFloatArray(buf, bloodHealCost);
                
                buf.writeVarInt(standXpCostInitial);
                buf.writeVarInt(standXpCostIncrease);
                buf.writeEnum(standRandomPoolMode);
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
                if (breathingTrainingDeterioration)     flags[0] |= 64;
//                if (_)                       flags[0] |= 128;
                
//                if (abilitiesBreakBlocks)               flags[1] |= 1;
                if (skipStandProgression)               flags[1] |= 2;
                if (standStamina)                       flags[1] |= 4;
                if (dropStandDisc)                      flags[1] |= 8;
                if (soulAscension)                      flags[1] |= 16;
                if (endermenBeyondTimeSpace)            flags[1] |= 32;
                if (mixHamonTechniques)                 flags[1] |= 64;
                if (hamonEnergyTicksDown)               flags[1] |= 128;
                buf.writeByteArray(flags);
            }

            private SyncedValues(Common config) {
                keepStandOnDeath = config.keepStandOnDeath.get();
                keepHamonOnDeath = config.keepHamonOnDeath.get();
                keepVampirismOnDeath = config.keepVampirismOnDeath.get();
                dropStandDisc = config.dropStandDisc.get();
                
                hamonTempleSpawn = config.hamonTempleSpawn.get();
                meteoriteSpawn = config.meteoriteSpawn.get();
                pillarManTempleSpawn = config.pillarManTempleSpawn.get();
                hamonEnergyTicksDown = config.hamonEnergyTicksDown.get();
                
//                hamonPointsMultiplier = config.standDamageMultiplier.get();
//                breathingTrainingMultiplier = config.breathingTrainingMultiplier.get();
                breathingTrainingDeterioration = config.breathingTrainingDeterioration.get();
                breathingStatGap = config.breathingHamonStatGap.get();
                mixHamonTechniques = config.mixHamonTechniques.get();
                techniqueSkillsRequirement = config.techniqueSkillsRequirement.get().stream().mapToInt(Integer::intValue).toArray();
                
                maxBloodMultiplier = Floats.toArray(config.maxBloodMultiplier.get());
//                bloodDrainMultiplier = Floats.toArray(config.bloodDrainMultiplier.get());
                bloodTickDown = Floats.toArray(config.bloodTickDown.get());
//                bloodHealCost = Floats.toArray(config.bloodHealCost.get());
//                vampiresAggroMobs = config.vampiresAggroMobs.get();
//                undeadMobsSunDamage = config.undeadMobsSunDamage.get();
                
                standXpCostInitial = config.standXpCostInitial.get();
                standXpCostIncrease = config.standXpCostIncrease.get();
                standRandomPoolMode = config.standRandomPoolFilter.get();
                bannedStands = config.bannedStandsResLocs.stream()
                        .map(key -> JojoCustomRegistries.STANDS.getRegistry().getValue(key))
                        .collect(Collectors.toList());
                
//                abilitiesBreakBlocks = config.abilitiesBreakBlocks.get();
//                standDamageMultiplier = config.standDamageMultiplier.get()
                skipStandProgression = config.skipStandProgression.get();
                standStamina = config.standStamina.get();
                resolvePoints = Floats.toArray(config.resolveLvlPoints.get());
                soulAscension = config.soulAscension.get();
                timeStopChunkRange = config.timeStopChunkRange.get();
                endermenBeyondTimeSpace = config.endermenBeyondTimeSpace.get();
            }
            
            public void changeConfigValues() {
                COMMON_SYNCED_TO_CLIENT.keepStandOnDeath.set(keepStandOnDeath);
                COMMON_SYNCED_TO_CLIENT.keepHamonOnDeath.set(keepHamonOnDeath);
                COMMON_SYNCED_TO_CLIENT.keepVampirismOnDeath.set(keepVampirismOnDeath);
                COMMON_SYNCED_TO_CLIENT.dropStandDisc.set(dropStandDisc);
                
                COMMON_SYNCED_TO_CLIENT.hamonTempleSpawn.set(hamonTempleSpawn);
                COMMON_SYNCED_TO_CLIENT.meteoriteSpawn.set(meteoriteSpawn);
                COMMON_SYNCED_TO_CLIENT.pillarManTempleSpawn.set(pillarManTempleSpawn);
                COMMON_SYNCED_TO_CLIENT.hamonEnergyTicksDown.set(hamonEnergyTicksDown);
                
//                COMMON_SYNCED_TO_CLIENT.hamonPointsMultiplier.set(hamonPointsMultiplier);
//                COMMON_SYNCED_TO_CLIENT.breathingTrainingMultiplier.set(breathingTrainingMultiplier);
                COMMON_SYNCED_TO_CLIENT.breathingTrainingDeterioration.set(breathingTrainingDeterioration);
                COMMON_SYNCED_TO_CLIENT.breathingHamonStatGap.set(breathingStatGap);
                COMMON_SYNCED_TO_CLIENT.mixHamonTechniques.set(mixHamonTechniques);
                COMMON_SYNCED_TO_CLIENT.techniqueSkillsRequirement.set(IntStream.of(techniqueSkillsRequirement).boxed().collect(Collectors.toList()));
                
                COMMON_SYNCED_TO_CLIENT.maxBloodMultiplier.set(Floats.asList(maxBloodMultiplier).stream().map(Float::doubleValue).collect(Collectors.toList()));
//                COMMON_SYNCED_TO_CLIENT.bloodDrainMultiplier.set(Floats.asList(bloodDrainMultiplier).stream().map(Float::doubleValue).collect(Collectors.toList()));
                COMMON_SYNCED_TO_CLIENT.bloodTickDown.set(Floats.asList(bloodTickDown).stream().map(Float::doubleValue).collect(Collectors.toList()));
//                COMMON_SYNCED_TO_CLIENT.bloodHealCost.set(Floats.asList(bloodHealCost).stream().map(Float::doubleValue).collect(Collectors.toList()));
                
                COMMON_SYNCED_TO_CLIENT.standXpCostInitial.set(standXpCostInitial);
                COMMON_SYNCED_TO_CLIENT.standXpCostIncrease.set(standXpCostIncrease);
                COMMON_SYNCED_TO_CLIENT.standRandomPoolFilter.set(standRandomPoolMode);
                COMMON_SYNCED_TO_CLIENT.bannedStandsSynced = bannedStands;
                
//                COMMON_SYNCED_TO_CLIENT.abilitiesBreakBlocks.set(abilitiesBreakBlocks);
//                COMMON_SYNCED_TO_CLIENT.standDamageMultiplier.set(standDamageMultiplier);
                COMMON_SYNCED_TO_CLIENT.skipStandProgression.set(skipStandProgression);
                COMMON_SYNCED_TO_CLIENT.standStamina.set(standStamina);
                COMMON_SYNCED_TO_CLIENT.resolveLvlPoints.set(Floats.asList(resolvePoints).stream().map(Float::doubleValue).collect(Collectors.toList()));
                COMMON_SYNCED_TO_CLIENT.soulAscension.set(soulAscension);
                COMMON_SYNCED_TO_CLIENT.timeStopChunkRange.set(timeStopChunkRange);
                COMMON_SYNCED_TO_CLIENT.endermenBeyondTimeSpace.set(endermenBeyondTimeSpace);
                
                COMMON_SYNCED_TO_CLIENT.onLoadOrReload();
            }

            public static void resetConfig() {
                COMMON_SYNCED_TO_CLIENT.keepStandOnDeath.clearCache();
                COMMON_SYNCED_TO_CLIENT.keepHamonOnDeath.clearCache();
                COMMON_SYNCED_TO_CLIENT.keepVampirismOnDeath.clearCache();
                COMMON_SYNCED_TO_CLIENT.dropStandDisc.clearCache();
                
                COMMON_SYNCED_TO_CLIENT.hamonTempleSpawn.clearCache();
                COMMON_SYNCED_TO_CLIENT.meteoriteSpawn.clearCache();
                COMMON_SYNCED_TO_CLIENT.pillarManTempleSpawn.clearCache();
                COMMON_SYNCED_TO_CLIENT.hamonEnergyTicksDown.clearCache();
                
//                COMMON_SYNCED_TO_CLIENT.hamonPointsMultiplier.clearCache();
//                COMMON_SYNCED_TO_CLIENT.breathingTrainingMultiplier.clearCache();
                COMMON_SYNCED_TO_CLIENT.breathingTrainingDeterioration.clearCache();
                COMMON_SYNCED_TO_CLIENT.breathingHamonStatGap.clearCache();
                COMMON_SYNCED_TO_CLIENT.mixHamonTechniques.clearCache();
                COMMON_SYNCED_TO_CLIENT.techniqueSkillsRequirement.clearCache();
                
                COMMON_SYNCED_TO_CLIENT.maxBloodMultiplier.clearCache();
                COMMON_SYNCED_TO_CLIENT.bloodDrainMultiplier.clearCache();
                COMMON_SYNCED_TO_CLIENT.bloodTickDown.clearCache();
                COMMON_SYNCED_TO_CLIENT.bloodHealCost.clearCache();
                
                COMMON_SYNCED_TO_CLIENT.standXpCostInitial.clearCache();
                COMMON_SYNCED_TO_CLIENT.standXpCostIncrease.clearCache();
                COMMON_SYNCED_TO_CLIENT.standRandomPoolFilter.clearCache();
                COMMON_SYNCED_TO_CLIENT.bannedStandsSynced = null;
                
//                COMMON_SYNCED_TO_CLIENT.abilitiesBreakBlocks.clearCache();
//                COMMON_SYNCED_TO_CLIENT.standDamageMultiplier.clearCache();
                COMMON_SYNCED_TO_CLIENT.skipStandProgression.clearCache();
                COMMON_SYNCED_TO_CLIENT.standStamina.clearCache();
                COMMON_SYNCED_TO_CLIENT.resolveLvlPoints.clearCache();
                COMMON_SYNCED_TO_CLIENT.soulAscension.clearCache();
                COMMON_SYNCED_TO_CLIENT.timeStopChunkRange.clearCache();
                COMMON_SYNCED_TO_CLIENT.endermenBeyondTimeSpace.clearCache();
                
                COMMON_SYNCED_TO_CLIENT.onLoadOrReload();
            }

            
            
            public static void syncWithClient(ServerPlayerEntity player) {
                PacketManager.sendToClient(new CommonConfigPacket(new SyncedValues(COMMON_FROM_FILE)), player);
            }
            
            public static void onPlayerLogout(ServerPlayerEntity player) {
                PacketManager.sendToClient(new ResetSyncedCommonConfigPacket(), player);
            }
        }
    }
    
    private static boolean isElementNonNegativeFloat(Object num, boolean moreThanZero) {
        if (num instanceof Double) {
            Double numDouble = (Double) num;
            return (numDouble > 0 || !moreThanZero && numDouble == 0) && Float.isFinite(numDouble.floatValue());
        }
        return false;
    }
    
    
    
    static final ForgeConfigSpec commonSpec;
    private static final Common COMMON_FROM_FILE;
    private static final Common COMMON_SYNCED_TO_CLIENT;
    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON_FROM_FILE = specPair.getLeft();

        // how tf do the configs work?
        final Pair<Common, ForgeConfigSpec> syncedSpecPair = new ForgeConfigSpec.Builder().configure(builder -> new Common(builder, "synced"));
        CommentedConfig config = CommentedConfig.of(InMemoryCommentedFormat.defaultInstance());
        ForgeConfigSpec syncedSpec = syncedSpecPair.getRight();
        syncedSpec.correct(config);
        syncedSpec.setConfig(config);
        COMMON_SYNCED_TO_CLIENT = syncedSpecPair.getLeft();
    }
    
    public static Common getCommonConfigInstance(boolean isClientSide) {
        return isClientSide && !ClientUtil.isLocalServer() ? COMMON_SYNCED_TO_CLIENT : COMMON_FROM_FILE;
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
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                server.getPlayerList().getPlayers().forEach(player -> {
                    Common.SyncedValues.syncWithClient(player);
                });
            }
        }
    }
}
