package com.github.standobyte.jojo.world.gen;

import java.util.function.Supplier;

import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;

public class ConfiguredFeatureSupplier<FC extends IFeatureConfig, F extends Feature<FC>> implements Supplier<ConfiguredFeature<FC, ? extends Feature<FC>>> {
    private final Supplier<F> structure;
    private final FC config;
    private ConfiguredFeature<FC, ? extends Feature<FC>> configured = null;
    
    public ConfiguredFeatureSupplier(Supplier<F> structure, FC config) {
        this.structure = structure;
        this.config = config;
    }
    
    public ConfiguredFeature<FC, ? extends Feature<FC>> get() {
        if (configured == null) {
            configured = structure.get().configured(config);
        }
        return configured;
    }

}
