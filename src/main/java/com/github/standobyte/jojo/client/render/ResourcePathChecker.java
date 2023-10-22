package com.github.standobyte.jojo.client.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;

import net.minecraft.util.ResourceLocation;

public class ResourcePathChecker {
    private static final Collection<ResourcePathChecker> ALL = new ArrayList<>();
    private final ResourceLocation path;
    private boolean resourceExists;
    private boolean checked = false;
    
    private ResourcePathChecker(ResourceLocation path) {
        this.path = path;
    }
    
    public static ResourcePathChecker create(ResourceLocation path) {
        ResourcePathChecker pathChecker = new ResourcePathChecker(path);
        ALL.add(pathChecker);
        return pathChecker;
    }
    
    public ResourceLocation or(ResourceLocation orDefault) {
        return resourceExists() ? path : orDefault;
    }
    
    public ResourceLocation or(Supplier<ResourceLocation> orDefault) {
        return resourceExists() ? path : orDefault.get();
    }
    
    public ResourceLocation getPath() {
        return path;
    }
    
    public boolean resourceExists() {
        if (!checked) {
            resourceExists = ClientUtil.resourceExists(path);
            checked = true;
        }
        return resourceExists;
    }
    
    public static void onResourcesReload() {
        ALL.forEach(path -> path.checked = false);
    }

}
