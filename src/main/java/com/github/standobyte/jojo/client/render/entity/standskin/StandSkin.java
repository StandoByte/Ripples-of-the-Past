package com.github.standobyte.jojo.client.render.entity.standskin;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.util.ResourceLocation;

/*
 * TODO Stand skins
 * UI color
 * in-mod textures
 *   entity textures
 *   in-mod UI
 *     actions atlas
 *   particles
 *   power icon
 * sounds
 *   sounds.json
 *   sound files
 * lang
 * particles definition?
 * Geckolib format model
 * 
 */
public class StandSkin {
    private final ResourceLocation resLoc;
    public final int color;
    
    private StandSkin(ResourceLocation resLoc, int color) {
        this.resLoc = resLoc;
        this.color = color;
    }
    
    
    
//    public static StandSkin getSkin(StandType<?> standType, ResourceLocation location) {
//        
//    }
    
    public static int getUiColor(IStandPower standPower) {
        return standPower.getStandInstance().map(stand -> stand.getType().getColor()).orElse(-1);
    }
}
