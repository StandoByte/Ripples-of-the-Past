package com.github.standobyte.jojo.power.impl.nonstand.type;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.NonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.TypeSpecificData;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.layout.ActionsLayout;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class NonStandPowerType<T extends TypeSpecificData> extends ForgeRegistryEntry<NonStandPowerType<?>> implements IPowerType<INonStandPower, NonStandPowerType<?>> {
    protected final Action<INonStandPower>[] attacks;
    protected final Action<INonStandPower>[] abilities;
    protected final Action<INonStandPower> defaultQuickAccess;
    private String translationKey;
    private ResourceLocation iconTexture;
    
    private final Supplier<T> dataFactory;

    public NonStandPowerType(Action<INonStandPower>[] startingAttacks, Action<INonStandPower>[] startingAbilities, 
            Action<INonStandPower> defaultQuickAccess, Supplier<T> dataFactory) {
        this.attacks = startingAttacks;
        this.abilities = startingAbilities;
        this.defaultQuickAccess = defaultQuickAccess;
        this.dataFactory = dataFactory;
    }
    
    public void onClear(INonStandPower power) {}
    
    public void afterClear(INonStandPower power) {}
    
    @Override
    public ActionsLayout<INonStandPower> createDefaultLayout() {
        return new ActionsLayout<>(attacks, abilities, defaultQuickAccess);
    }

    public float getMaxEnergy(INonStandPower power) {
        return NonStandPower.BASE_MAX_ENERGY;
    }
    
    public abstract float tickEnergy(INonStandPower power);
    
    public boolean hasEnergy(INonStandPower power, float amount) {
        return power.getEnergy() >= amount;
    }
    
    public boolean consumeEnergy(INonStandPower power, float amount) {
        if (power.isUserCreative()) {
            return true;
        }
        if (power.hasEnergy(amount)) {
            power.setEnergy(power.getEnergy() - amount);
            return true;
        }
        return false;
    }
    
    public float getMaxStaminaFactor(INonStandPower power, IStandPower standPower) {
        return 1;
    }
    
    public float getStaminaRegenFactor(INonStandPower power, IStandPower standPower) {
        return 1;
    }
    
    public boolean isLeapUnlocked(INonStandPower power) {
        return false;
    }
    
    public void onLeap(INonStandPower power) {}
    
    public float getLeapStrength(INonStandPower power) {
        return 0;
    }
    
    public int getLeapCooldownPeriod() {
        return 0;
    }
    
    public float getLeapEnergyCost() {
        return 0;
    }

    public TypeSpecificData newSpecificDataInstance() {
        return dataFactory.get();
    }
    
    @Override
    public String getTranslationKey() {
        if (translationKey == null) {
            translationKey = Util.makeDescriptionId("non_stand", JojoCustomRegistries.NON_STAND_POWERS.getRegistry().getKey(this));
        }
        return this.translationKey;
    }

    @Override
    public ResourceLocation getIconTexture(@Nullable INonStandPower power) {
        if (iconTexture == null) {
            iconTexture = JojoModUtil.makeTextureLocation("power", getRegistryName().getNamespace(), getRegistryName().getPath());
        }
        return this.iconTexture;
    }
}
