package com.github.standobyte.jojo.init;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.util.ForgeBusEventSubscriber;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;
import com.github.standobyte.jojo.world.gen.ConfiguredStructureSupplier;
import com.github.standobyte.jojo.world.gen.structures.HamonTemplePieces;
import com.github.standobyte.jojo.world.gen.structures.HamonTempleStructure;
import com.github.standobyte.jojo.world.gen.structures.MeteoritePieces;
import com.github.standobyte.jojo.world.gen.structures.MeteoriteStructure;
import com.github.standobyte.jojo.world.gen.structures.PillarmanTemplePieces;
import com.github.standobyte.jojo.world.gen.structures.PillarmanTempleStructure;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(modid = JojoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModStructures {
    public static final DeferredRegister<Structure<?>> STRUCTURES = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, JojoMod.MOD_ID);
    

    public static final RegistryObject<Structure<NoFeatureConfig>> HAMON_TEMPLE = STRUCTURES.register("hamon_temple", 
            () -> (new HamonTempleStructure(NoFeatureConfig.CODEC)));
    public static final ConfiguredStructureSupplier<?, ?> CONFIGURED_HAMON_TEMPLE = new ConfiguredStructureSupplier<>(HAMON_TEMPLE, IFeatureConfig.NONE);

    public static final RegistryObject<Structure<NoFeatureConfig>> METEORITE = STRUCTURES.register("meteorite", 
            () -> (new MeteoriteStructure(NoFeatureConfig.CODEC)));
    public static final ConfiguredStructureSupplier<?, ?> CONFIGURED_METEORITE = new ConfiguredStructureSupplier<>(METEORITE, IFeatureConfig.NONE);

    public static final RegistryObject<PillarmanTempleStructure> PILLARMAN_TEMPLE = STRUCTURES.register("pillarman_temple", 
            () -> (new PillarmanTempleStructure(NoFeatureConfig.CODEC)));
    public static final ConfiguredStructureSupplier<?, ?> CONFIGURED_PILLARMAN_TEMPLE = new ConfiguredStructureSupplier<>(PILLARMAN_TEMPLE, IFeatureConfig.NONE);

    public static final Predicate<BiomeLoadingEvent> HAMON_TEMPLE_BIOMES = biome -> biome.getCategory() == Biome.Category.EXTREME_HILLS;
    public static final Predicate<BiomeLoadingEvent> METEORITE_BIOMES = biome -> biome.getClimate().precipitation == Biome.RainType.SNOW && biome.getCategory() != Biome.Category.OCEAN;
    public static final Predicate<BiomeLoadingEvent> PILLARMAN_TEMPLE_BIOMES = biome -> biome.getCategory() == Biome.Category.JUNGLE;
    
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public static final void afterStructuresRegister(RegistryEvent.Register<Structure<?>> event) {
        Registry<StructureFeature<?, ?>> registry = WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE;

        setupMapSpacingAndLand(HAMON_TEMPLE.get(), new StructureSeparationSettings(20, 8, 139567129), true);
        setupMapSpacingAndLand(METEORITE.get(), new StructureSeparationSettings(40, 12, 286704381), false);
        setupMapSpacingAndLand(PILLARMAN_TEMPLE.get(), new StructureSeparationSettings(64, 20, 64023956), false);

        HamonTemplePieces.initPieceType();
        MeteoritePieces.initPieceType();
        PillarmanTemplePieces.initPieceType();

        registerConfiguredStructure(registry, CONFIGURED_HAMON_TEMPLE.get(), 
                new ResourceLocation(JojoMod.MOD_ID, "configured_hamon_temple"), HAMON_TEMPLE.get(), 
                HAMON_TEMPLE_BIOMES.and(b -> JojoModConfig.getCommonConfigInstance(false).hamonTempleSpawn.get()));
        registerConfiguredStructure(registry, CONFIGURED_METEORITE.get(), 
                new ResourceLocation(JojoMod.MOD_ID, "configured_meteorite"), METEORITE.get(), 
                METEORITE_BIOMES.and(b -> JojoModConfig.getCommonConfigInstance(false).meteoriteSpawn.get()));
        registerConfiguredStructure(registry, CONFIGURED_PILLARMAN_TEMPLE.get(), 
                new ResourceLocation(JojoMod.MOD_ID, "configured_pillarman_temple"), PILLARMAN_TEMPLE.get(), 
                PILLARMAN_TEMPLE_BIOMES.and(b -> JojoModConfig.getCommonConfigInstance(false).pillarManTempleSpawn.get()));
    }
    
    private static <F extends Structure<?>> void setupMapSpacingAndLand(
            F structure,
            StructureSeparationSettings structureSeparationSettings,
            boolean transformSurroundingLand) {
        Structure.STRUCTURES_REGISTRY.put(structure.getRegistryName().toString(), structure);

        if (transformSurroundingLand) {
            Structure.NOISE_AFFECTING_FEATURES = ImmutableList.<Structure<?>>builder()
                    .addAll(Structure.NOISE_AFFECTING_FEATURES)
                    .add(structure)
                    .build();
        }

        DimensionStructuresSettings.DEFAULTS = ImmutableMap.<Structure<?>, StructureSeparationSettings>builder()
                .putAll(DimensionStructuresSettings.DEFAULTS)
                .put(structure, structureSeparationSettings)
                .build();

        WorldGenRegistries.NOISE_GENERATOR_SETTINGS.entrySet().forEach(settings -> {
            Map<Structure<?>, StructureSeparationSettings> structureMap = settings.getValue().structureSettings().structureConfig();
            
            if (structureMap instanceof ImmutableMap) {
                Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(structureMap);
                tempMap.put(structure, structureSeparationSettings);
                settings.getValue().structureSettings().structureConfig = tempMap;
            }
            else{
                structureMap.put(structure, structureSeparationSettings);
            }
        });
    }
    
    private static void registerConfiguredStructure(Registry<StructureFeature<?, ?>> registry, StructureFeature<?, ?> configured, 
            ResourceLocation resLoc, Structure<?> structure, @Nullable Predicate<BiomeLoadingEvent> structureBiome) {
        Registry.register(registry, resLoc, configured);
        CommonReflection.flatGenSettingsStructures().put(structure, configured);
        if (structureBiome != null) {
            ForgeBusEventSubscriber.structureBiomes.put(() -> configured, structureBiome);
        }
    }
}
