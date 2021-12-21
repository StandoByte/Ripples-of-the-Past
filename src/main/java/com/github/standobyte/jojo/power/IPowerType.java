package com.github.standobyte.jojo.power;

import com.github.standobyte.jojo.action.Action;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IPowerType<P extends IPower<T>, T extends IPowerType<P, T>> extends IForgeRegistryEntry<T> {
    static final String NO_POWER_NAME = "";
    int getColor();
    boolean isReplaceableWith(T newType);
    void tickUser(LivingEntity entity, P power);
    Action<P>[] getAttacks();
    Action<P>[] getAbilities();
    int getExpRewardMultiplier();
    String getTranslationKey();
    ResourceLocation getIconTexture();
    String getManaString();
}
