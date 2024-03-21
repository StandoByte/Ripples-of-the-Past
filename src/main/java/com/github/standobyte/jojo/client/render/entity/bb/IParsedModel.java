package com.github.standobyte.jojo.client.render.entity.bb;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.util.ResourceLocation;

public interface IParsedModel {
    public static final ResourceLocation SILVER_CHARIOT_ARMOR = new ResourceLocation(JojoMod.MOD_ID, "silver_chariot_armor");
    void afterParse(ResourceLocation modelId);
    
    EntityModelUnbaked createUnbakedModel();
}
