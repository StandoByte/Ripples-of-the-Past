package com.github.standobyte.jojo.client.standskin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.standobyte.jojo.client.ResourcePathChecker;

import net.minecraft.util.ResourceLocation;

/*
 * TODO Stand skins
 * 
 * [V]  UI color
 * 
 * [_] in-mod textures
 * [_]   entity textures
 * [V]     Stand entity
 * [_]     stuff like projectiles
 * [V]   power icon
 * [_]   in-mod UI
 * [_]     actions atlas
 * [_]   particles
 *     
 * [_] lang
 *     
 * [_] Geckolib format model
 *     
 * [_] sounds
 * [_]   sounds.json
 * [_]   sound files
 *     
 * [_] particles definition?
 * 
 */
public class StandSkin {
    public final ResourceLocation resLoc;
    public final ResourceLocation standTypeId;
    public final int color;
    public final boolean defaultSkin;
    
    private final Map<ResourceLocation, ResourcePathChecker> resourceCheckCache = new HashMap<>();
    
    public StandSkin(ResourceLocation resLoc, ResourceLocation standTypeId, int color) {
        this.resLoc = resLoc;
        this.standTypeId = standTypeId;
        this.color = color;
        this.defaultSkin = resLoc.equals(standTypeId);
    }
    
    public Optional<ResourceLocation> getNonDefaultLocation() {
        return defaultSkin ? Optional.empty() : Optional.of(resLoc);
    }
    
    public ResourcePathChecker getRemappedResPath(ResourceLocation originalResPath) {
        return resourceCheckCache.computeIfAbsent(originalResPath, 
                path -> ResourcePathChecker.getOrCreate(
                        StandSkinsManager.pathRemapFunc(this.resLoc, path)));
    }
}
