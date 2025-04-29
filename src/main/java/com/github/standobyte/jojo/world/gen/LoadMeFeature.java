package com.github.standobyte.jojo.world.gen;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.gen.feature.template.TemplateManager;

public interface LoadMeFeature {
    default void loadTemplate(MinecraftServer server) {
        loadTemplate(server.getStructureManager());
    }
    void loadTemplate(TemplateManager templateManager);
}
