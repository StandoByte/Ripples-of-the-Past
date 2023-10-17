package com.github.standobyte.jojo.client.render.entity.standskin;

import net.minecraft.util.ResourceLocation;

/*
 * TODO Stand skins
 * 
 * [V]  UI color
 * 
 * [_] in-mod textures
 * [_]   entity textures
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
    private final ResourceLocation resLoc;
    private final ResourceLocation standTypeId;
    public final int color;
    
    public StandSkin(ResourceLocation resLoc, ResourceLocation standTypeId, int color) {
        this.resLoc = resLoc;
        this.standTypeId = standTypeId;
        this.color = color;
    }
}