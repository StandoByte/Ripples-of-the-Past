package com.github.standobyte.jojo.client.standskin;

import java.util.Optional;

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
 * [_]   power icon
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
    
    public StandSkin(ResourceLocation resLoc, ResourceLocation standTypeId, int color) {
        this.resLoc = resLoc;
        this.standTypeId = standTypeId;
        this.color = color;
        this.defaultSkin = resLoc.equals(standTypeId);
    }
    
    public Optional<ResourceLocation> getNonDefaultLocation() {
        return defaultSkin ? Optional.empty() : Optional.of(resLoc);
    }
}
