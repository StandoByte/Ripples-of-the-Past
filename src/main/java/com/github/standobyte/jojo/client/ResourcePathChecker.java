package com.github.standobyte.jojo.client;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Supplier;

import net.minecraft.util.ResourceLocation;

public class ResourcePathChecker {
    private static final Collection<ResourcePathChecker> ALL = new HashSet<>();
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
    
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof ResourcePathChecker) {
            return this.path.equals(((ResourcePathChecker) obj).path);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return path.hashCode();
    }

}
