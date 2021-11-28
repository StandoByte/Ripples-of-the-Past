package com.github.standobyte.jojo.power.nonstand.type;

import java.util.function.Supplier;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.TypeSpecificData;
import com.github.standobyte.jojo.util.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class NonStandPowerType<T extends TypeSpecificData> extends ForgeRegistryEntry<NonStandPowerType<?>> implements IPowerType<NonStandPowerType<?>> {
    private final int color;
    protected final Action[] attacks;
    protected final Action[] abilities;
    private final Supplier<T> dataFactory;
    private String translationKey;
    private final float manaRegenPoints;
    private ResourceLocation iconTexture;
    
    public NonStandPowerType(int color, Action[] startingAttacks, Action[] startingAbilities, float manaRegenPoints, Supplier<T> dataFactory) {
        this.color = color;
        this.attacks = startingAttacks;
        this.abilities = startingAbilities;
        this.manaRegenPoints = manaRegenPoints;
        this.dataFactory = dataFactory;
    }
    
    @Override
    public int getColor() {
        return color;
    }
    
    public void onClear(INonStandPower power) {}
    
    @Override
    public boolean canTickMana(LivingEntity user, IPower<?> power) {
        return true;
    }
    
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
    public String getManaString() {
        return getRegistryName().getPath();
    }
    
    @Override
    public Action[] getAttacks() {
        return attacks;
    }

    @Override
    public Action[] getAbilities() {
        return abilities;
    }

    public float getStartingManaRegenPoints() {
        return manaRegenPoints;
    }
    
    public float reduceManaConsumed(float amount, INonStandPower power, LivingEntity user) {
        return amount;
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
    
    public float getLeapManaCost() {
        return 0;
    }

    public TypeSpecificData newSpecificDataInstance() {
        return dataFactory.get();
    }
}
