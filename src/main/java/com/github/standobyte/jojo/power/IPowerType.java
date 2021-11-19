package com.github.standobyte.jojo.power;

import com.github.standobyte.jojo.action.Action;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IPowerType<T> extends IForgeRegistryEntry<T> {
    static final String NO_POWER_NAME = "";
    int getColor();
    boolean canTickMana(LivingEntity user, IPower<?> power);
    boolean isReplaceableWith(T newType);
    void tickUser(LivingEntity entity, IPower<?> power);
    Action[] getAttacks();
    Action[] getAbilities();
    int getExpRewardMultiplier();
    String getTranslationKey();
    ResourceLocation getIconTexture();
    String getManaString();
}
