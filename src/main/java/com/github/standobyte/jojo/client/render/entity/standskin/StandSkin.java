package com.github.standobyte.jojo.client.render.entity.standskin;

import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;

import net.minecraft.util.ResourceLocation;

/*
 * TODO Stand skins
 * UI color
 * 
 * in-mod textures
 *   entity textures
 *   in-mod UI
 *     actions atlas
 *   particles
 *   power icon
 * 
 * lang
 * 
 * Geckolib format model
 * 
 * sounds
 *   sounds.json
 *   sound files
 * 
 * particles definition?
 * 
 */
public class StandSkin {
    private final ResourceLocation resLoc;
    private final ResourceLocation standTypeId;
    public final int color;
    
    private StandSkin(ResourceLocation resLoc, ResourceLocation standTypeId, int color) {
        this.resLoc = resLoc;
        this.standTypeId = standTypeId;
        this.color = color;
    }
    
    
    
    public static int getUiColor(IStandPower standPower) {
        return standPower.getStandInstance().map(StandSkin::getUiColor).orElse(-1);
    }
    
    public static int getUiColor(StandInstance standInstance) {
        return standInstance.getType() != null ? standInstance.getType().getColor() : -1;
    }
}
