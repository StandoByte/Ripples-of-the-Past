package com.github.standobyte.jojo.util.mod;

import java.util.Comparator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.util.ResourceLocation;

public class ModInteractionUtil {
    private static final ResourceLocation MOWZIES_FROZEN_EFFECT = new ResourceLocation("mowziesmobs", "frozen");
    private static final ResourceLocation TWILIGHT_FOREST_FROSTED_EFFECT = new ResourceLocation("twilightforest", "frosted");
    public static float getEntityFreeze(LivingEntity entity) {
        return Math.min(entity.getActiveEffectsMap().entrySet().stream().map(entry -> {
            if (MOWZIES_FROZEN_EFFECT.equals(entry.getKey().getRegistryName())) {
                return 1F;
            }
            if (TWILIGHT_FOREST_FROSTED_EFFECT.equals(entry.getKey().getRegistryName())) {
                return Math.min((entry.getValue().getAmplifier() + 1) * 0.25F, 1);
            }
            return 0F;
        }).max(Comparator.naturalOrder()).orElse(0F), 1);
    }

    private static final ResourceLocation MUTANT_ENDERMAN_ID = new ResourceLocation("mutantbeasts", "mutant_enderman");
    private static final ResourceLocation MUTANT_ENDERMAN_ID_2 = new ResourceLocation("mutantbeasts", "endersoul_clone");
    private static final ResourceLocation MUTANT_ENDERMAN_ID_3 = new ResourceLocation("mutantbeasts", "endersoul_fragment");
    public static boolean isEntityEnderman(Entity entity) {
        return entity instanceof EndermanEntity
                || entity.getType().getRegistryName().equals(MUTANT_ENDERMAN_ID)
                || entity.getType().getRegistryName().equals(MUTANT_ENDERMAN_ID_2)
                || entity.getType().getRegistryName().equals(MUTANT_ENDERMAN_ID_3);
    }
}
