package com.github.standobyte.jojo.client.standskin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ResourcePathChecker;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/*
 * TODO Stand skins
 * 
 * [V] UI color
 * [_] appearance part
 * 
 * [_] in-mod textures
 * [_]   entity textures
 * [V]     Stand entity
 * [_]     stuff like projectiles
 * [V]   power icon
 * [_]   in-mod UI
 * [V]     actions
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
    private final ITextComponent partName;
    public final boolean defaultSkin;
//    public final boolean inModSkin;
    
    private final Map<ResourceLocation, ResourcePathChecker> resourceCheckCache = new HashMap<>();
    
    public StandSkin(ResourceLocation resLoc, ResourceLocation standTypeId, 
            int color, @Nullable ITextComponent partName) {
        this.resLoc = resLoc;
        this.standTypeId = standTypeId;
        this.color = color;
        this.partName = partName;
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
    
    public ITextComponent getPartName(StandType<?> standTypeObj) {
        return partName != null ? partName : standTypeObj.getPartName();
    }
}
