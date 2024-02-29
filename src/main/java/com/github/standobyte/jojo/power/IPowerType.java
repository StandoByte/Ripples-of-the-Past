package com.github.standobyte.jojo.power;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.controls.ControlScheme;
import com.github.standobyte.jojo.power.bowcharge.IBowChargeEffect;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IPowerType<P extends IPower<P, T>, T extends IPowerType<P, T>> extends IForgeRegistryEntry<T> {
    static final String NO_POWER_NAME = "";
    boolean isReplaceableWith(T newType);
    boolean keepOnDeath(P power);
    
    void tickUser(LivingEntity entity, P power);
    default void onNewDay(LivingEntity user, P power, long prevDay, long day) {}
    default RayTraceResult clientHitResult(P power, Entity cameraEntity, RayTraceResult vanillaHitResult) {
        return vanillaHitResult;
    }
    
    ControlScheme.DefaultControls clCreateDefaultLayout();
    void clAddMissingActions(ControlScheme controlScheme, P power);
    default boolean isActionLegalInHud(Action<P> action, P power) { return true; }
    
    @Nullable default IBowChargeEffect<P, T> getBowChargeEffect() {
        return null;
    }
    
    float getTargetResolveMultiplier(P power, IStandPower attackingStand);
    
    String getTranslationKey();
    default IFormattableTextComponent getName() {
        return new TranslationTextComponent(getTranslationKey());
    }
    
    ResourceLocation getIconTexture(@Nullable P power);
}
