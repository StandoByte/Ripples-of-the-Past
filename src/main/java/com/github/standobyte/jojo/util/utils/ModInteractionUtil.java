package com.github.standobyte.jojo.util.utils;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

public class ModInteractionUtil {
    private static final ResourceLocation MOWZIES_FROZEN_EFFECT = new ResourceLocation("mowziesmobs", "frozen");
    
    public static float getEntityFreeze(LivingEntity entity) {
        return entity.getActiveEffectsMap().entrySet()
                .stream().anyMatch(entry -> MOWZIES_FROZEN_EFFECT.equals(entry.getKey().getRegistryName())) ? 1 : 0;
    }
}
