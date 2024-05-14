package com.github.standobyte.jojo.client.standskin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ResourcePathChecker;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager.SkinResourcePrepare;
import com.github.standobyte.jojo.client.standskin.resource.StandModelReskin;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/*
 * TODO Stand skins
 * 
 * [V] UI color
 * [V] appearance part
 * 
 *     in-mod textures
 *       entity textures
 * [V]     Stand entity
 * [_]     stuff like projectiles
 * [V]   power icon
 * [_]   in-mod UI
 * [V]     actions
 * [_]   particles
 * [_]   shaders
 *     
 * [_] lang
 *     
 * [V] Stand model
 *     
 * [_] sounds
 * [_]   sounds.json
 * [_]   sound files
 * 
 */
public class StandSkin {
    public final ResourceLocation resLoc;
    public final ResourceLocation standTypeId;
    public final int color;
    public final boolean defaultSkin;
//    public final boolean inModSkin;
    
    private final ITextComponent partName;
    
    /*  Is used to save the resource pack data before it can be processed
     *  and turned into some elements of the skin, e.g. custom Stand model. 
     *  Otherwise is unused and == null most of the time.
     */
    public SkinResourcePrepare resourcePrepare = null;
    public final StandModelReskin standModels = new StandModelReskin();
    
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
                        pathRemapFunc(this.resLoc, path)));
    }
    
    public static ResourceLocation pathRemapFunc(ResourceLocation skinPath, ResourceLocation originalResPath) {
        return new ResourceLocation(
                skinPath.getNamespace(), 
                "stand_skins/" + skinPath.getPath() + "/assets/" + originalResPath.getNamespace() + "/" + originalResPath.getPath()
                );
    }
    
    public static ResourceLocation remapBack(ResourceLocation skinPath, ResourceLocation remappedResPath) {
        String path = remappedResPath.getPath()
                .replace("stand_skins/" + skinPath.getPath() + "/assets/", "");
        String namespace = path.split("/")[0];
        return new ResourceLocation(namespace, path.substring(namespace.length() + 1));
    }
    
    
    public ITextComponent getPartName(StandType<?> standTypeObj) {
        return partName != null ? partName : standTypeObj.getPartName();
    }
}
