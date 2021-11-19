package com.github.standobyte.jojo.world.gen;

import java.util.function.Supplier;

import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;

public class ConfiguredStructureSupplier<FC extends IFeatureConfig, F extends Structure<FC>> implements Supplier<StructureFeature<FC, ? extends Structure<FC>>> {
    private final Supplier<F> structure;
    private final FC config;
    private StructureFeature<FC, ? extends Structure<FC>> configured = null;
    
    public ConfiguredStructureSupplier(Supplier<F> structure, FC config) {
        this.structure = structure;
        this.config = config;
    }
    
    public StructureFeature<FC, ? extends Structure<FC>> get() {
        if (configured == null) {
            configured = structure.get().configured(config);
        }
        return configured;
    }

}
