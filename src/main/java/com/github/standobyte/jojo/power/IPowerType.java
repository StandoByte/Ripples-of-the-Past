package com.github.standobyte.jojo.power;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.power.IPower.ActionType;
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
    
    Action<P>[] getAttacks();
    Action<P>[] getAbilities();
    default Action<P>[] getDefaultActions(ActionType actionType) {
        switch (actionType) {
        case ATTACK: return getAttacks();
        case ABILITY: return getAbilities();
        }
        throw new IllegalArgumentException();
    }
    Action<P> getDefaultQuickAccess();
    
    @Nullable default IBowChargeEffect<P, T> getBowChargeEffect() {
        return null;
    }
    
    float getTargetResolveMultiplier(P power, IStandPower attackingStand);
    
    String getTranslationKey();
    default IFormattableTextComponent getName() {
        return new TranslationTextComponent(getTranslationKey());
    }
    /**
     * For internal use. To get the Stand icon path, use ClientUtil.getPowerTypeIcon(IPower), as it accounts for things like Stand skins.
     */
    ResourceLocation getIconTexture();
}
