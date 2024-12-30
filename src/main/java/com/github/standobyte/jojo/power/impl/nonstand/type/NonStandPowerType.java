package com.github.standobyte.jojo.power.impl.nonstand.type;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.client.controls.ControlScheme;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.NonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.TypeSpecificData;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class NonStandPowerType<T extends TypeSpecificData> extends ForgeRegistryEntry<NonStandPowerType<?>> implements IPowerType<INonStandPower, NonStandPowerType<?>> {
    protected final Action<INonStandPower>[] attacks;
    protected final Action<INonStandPower>[] abilities;
    protected final Action<INonStandPower> defaultQuickAccess;
    private String translationKey;
    private ResourceLocation iconTexture;
    private OptionalInt color = OptionalInt.empty();
    
    private final Supplier<T> dataFactory;

    public NonStandPowerType(Action<INonStandPower>[] startingAttacks, Action<INonStandPower>[] startingAbilities,
            Action<INonStandPower> defaultQuickAccess, Supplier<T> dataFactory) {
        this.attacks = startingAttacks;
        this.abilities = startingAbilities;
        this.defaultQuickAccess = defaultQuickAccess;
        this.dataFactory = dataFactory;
        initPassiveEffects();
    }
    
    public <PT extends NonStandPowerType<T>> PT withColor(int color) {
        this.color = OptionalInt.of(color);
        return (PT) this;
    }
    
    @Override
    public void tickUser(LivingEntity entity, INonStandPower power) {
    }
    
    public void onClear(INonStandPower power) {}
    
    public void afterClear(INonStandPower power) {
        LivingEntity user = power.getUser();
        for (Effect effect : getAllPossibleEffects()) {
            EffectInstance effectInstance = user.getEffect(effect);
            if (effectInstance != null && !effectInstance.isVisible() && !effectInstance.showIcon()) {
                user.removeEffect(effectInstance.getEffect());
            }
        }
    }
    
    
    protected void initPassiveEffects() {
    }
    
    public int getPassiveEffectLevel(Effect effect, INonStandPower power) {
        return -1;
    }
    
    public void updatePassiveEffects(LivingEntity entity, INonStandPower power) {
        if (!entity.level.isClientSide()) {
            for (Effect effect : getAllPossibleEffects()) {
                int amplifier = getPassiveEffectLevel(effect, power);
                boolean effectUpdated = false;
                
                float missingHp = -1;
                boolean maxHpIncreased = false;
                if (effect == Effects.HEALTH_BOOST) {
                    missingHp = entity.getMaxHealth() - entity.getHealth();
                    maxHpIncreased = amplifier >= 0 && 
                            amplifier > Optional.ofNullable(entity.getEffect(Effects.HEALTH_BOOST)).map(EffectInstance::getAmplifier).orElse(-1);
                }
                
                if (amplifier >= 0) {
                    EffectInstance currentEffect = entity.getEffect(effect);
                    if (currentEffect == null || currentEffect.getAmplifier() < amplifier
                            || currentEffect.getAmplifier() != amplifier && !currentEffect.showIcon()) {
                        effectUpdated = true;
                        entity.removeEffectNoUpdate(effect);
                        entity.addEffect(new EffectInstance(effect, Integer.MAX_VALUE, amplifier, false, false, false));
                    }
                }
                else if (entity.hasEffect(effect)) {
                    effectUpdated = true;
                    entity.removeEffect(effect);
                }
                
                if (effectUpdated && missingHp > -1) {
                    if (maxHpIncreased) {
                        entity.setHealth(entity.getMaxHealth() - missingHp);
                    }
                    else {
                        entity.setHealth(Math.min(entity.getHealth(), entity.getMaxHealth()));
                    }
                }
            }
        }
    }
    
    
    @Override
    public ControlScheme.DefaultControls clCreateDefaultLayout() {
        return new ControlScheme.DefaultControls(
                attacks, 
                abilities, 
                ControlScheme.DefaultControls.DefaultKey.mmb(defaultQuickAccess));
    }
    
    @Override
    public void clAddMissingActions(ControlScheme controlScheme, INonStandPower power) {
        for (Action<?> attack : attacks) {
            controlScheme.addIfMissing(ControlScheme.Hotbar.LEFT_CLICK, attack);
        }
        for (Action<?> ability : abilities) {
            controlScheme.addIfMissing(ControlScheme.Hotbar.RIGHT_CLICK, ability);
        }
    }
    
    @Override
    public boolean isActionLegalInHud(Action<INonStandPower> action, INonStandPower power) {
        return ArrayUtils.contains(attacks, action) || ArrayUtils.contains(abilities, action);
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
    
    
    @SafeVarargs
    protected final void initAllPossibleEffects(Supplier<? extends Effect>... effects) {
        effectsSuppliersLazy = Arrays.asList(effects);
    }

    private List<Supplier<? extends Effect>> effectsSuppliersLazy;
    private List<Effect> effects;
    public Iterable<Effect> getAllPossibleEffects() {
        if (effects == null) {
            if (effectsSuppliersLazy != null) {
                effects = effectsSuppliersLazy.stream().map(Supplier::get).collect(Collectors.toList());
            }
            else {
                effects = Collections.emptyList();
            }
        }
        return effects;
    }

    public TypeSpecificData newSpecificDataInstance() {
        return dataFactory.get();
    }
    
    public OptionalInt getColor() {
        return color;
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

    public Action<INonStandPower>[] getAttacks() {
        return attacks;
    }

    public Action<INonStandPower>[] getAbilities() {
        return abilities;
    }

    public Action<INonStandPower> getDefaultQuickAccess() {
        return defaultQuickAccess;
    }

    /**
     * This method will only return actions that are added to the HUD by default.
     * This does <u>not</u> include Hamon technique skills and Pillar Man actions.
     */
    public List<Action<INonStandPower>> getDefaultActions() {
        List<Action<INonStandPower>> allActions = new ArrayList<>();
        Collections.addAll(allActions, attacks);
        Collections.addAll(allActions, abilities);
        return allActions;
    }

    public List<Action<INonStandPower>> getUnlockedDefaultActions(INonStandPower power) {
        List<Action<INonStandPower>> allActions = new ArrayList<>();
        Collections.addAll(allActions, attacks);
        Collections.addAll(allActions, abilities);
        return allActions.stream().filter(action -> action.isUnlocked(power)).collect(Collectors.toList());
    }
}
