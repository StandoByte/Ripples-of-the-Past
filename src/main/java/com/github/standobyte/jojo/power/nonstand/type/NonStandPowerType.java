package com.github.standobyte.jojo.power.nonstand.type;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.TypeSpecificData;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class NonStandPowerType<T extends TypeSpecificData> extends ForgeRegistryEntry<NonStandPowerType<?>> implements IPowerType<INonStandPower, NonStandPowerType<?>> {
    private final int color;
    protected final Action<INonStandPower>[] attacks;
    protected final Action<INonStandPower>[] abilities;
    private final Supplier<T> dataFactory;
    private String translationKey;
    private ResourceLocation iconTexture;
    
    public NonStandPowerType(int color, Action<INonStandPower>[] startingAttacks, Action<INonStandPower>[] startingAbilities, Supplier<T> dataFactory) {
        this.color = color;
        this.attacks = startingAttacks;
        this.abilities = startingAbilities;
        this.dataFactory = dataFactory;
    }
    
    @Override
    public int getColor() {
        return color;
    }
    
    public void onClear(INonStandPower power) {}
    
    public void afterClear(INonStandPower power) {}
    
    @Override
    public String getTranslationKey() {
        if (translationKey == null) {
            translationKey = Util.makeDescriptionId("non_stand", ModNonStandPowers.Registry.getRegistry().getKey(this));
        }
        return this.translationKey;
    }
    
    @Override
    public ResourceLocation getIconTexture() {
        if (iconTexture == null) {
            iconTexture = JojoModUtil.makeTextureLocation("power", getRegistryName().getNamespace(), getRegistryName().getPath());
        }
        return this.iconTexture;
    }
    
    @Override
    public String getEnergyString() {
        return getRegistryName().getPath();
    }
    
    @Override
    public Action<INonStandPower>[] getAttacks() {
        return attacks;
    }

    @Override
    public Action<INonStandPower>[] getAbilities() {
        return abilities;
    }

    public float getMaxEnergyFactor(INonStandPower power) {
        return 1;
    }
    
    public abstract float getEnergyTickInc(INonStandPower power);
    
    public float reduceEnergyConsumed(float amount, INonStandPower power, LivingEntity user) {
        return amount;
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
}
