package com.github.standobyte.jojo.power.stand.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.actions.StandAction;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.stand.IStandManifestation;
import com.github.standobyte.jojo.power.stand.IStandPower;
import com.github.standobyte.jojo.power.stand.StandUtil;
import com.github.standobyte.jojo.power.stand.stats.StandStats;
import com.github.standobyte.jojo.util.JojoModUtil;
import com.github.standobyte.jojo.util.OstSoundList;
import com.github.standobyte.jojo.util.damage.IStandDamageSource;
import com.github.standobyte.jojo.util.data.StandStatsManager;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class StandType<T extends StandStats> extends ForgeRegistryEntry<StandType<?>> implements IPowerType<IStandPower, StandType<?>> {
    private final int tier;
    private final int color;
    private final ITextComponent partName;
    private final StandAction[] attacks;
    private final StandAction[] abilities;
    private final T defaultStats;
    private final Class<T> statsClass;
    private String translationKey;
    private Supplier<SoundEvent> summonShoutSupplier = () -> null;
    private OstSoundList ostSupplier = null;
    private Map<Integer, List<ItemStack>> resolveLevelItems = new HashMap<>();
    private ResourceLocation iconTexture;
    
    public StandType(int tier, int color, ITextComponent partName, 
            StandAction[] attacks, StandAction[] abilities, 
            Class<T> statsClass, T defaultStats) {
        this.tier = MathHelper.clamp(tier, 0, StandUtil.TIER_XP_LEVELS.length - 1);
        this.color = color;
        this.partName = partName;
        this.attacks = attacks;
        this.abilities = abilities;
        this.statsClass = statsClass;
        this.defaultStats = defaultStats;
    }
    
    public StandType<T> addSummonShout(Supplier<SoundEvent> summonShoutSupplier) {
        if (summonShoutSupplier != null) {
            this.summonShoutSupplier = summonShoutSupplier;
        }
        return this;
    }
    
    public StandType<T> addOst(OstSoundList ostSupplier) {
        if (ostSupplier != null) {
            this.ostSupplier = ostSupplier;
        }
        return this;
    }
    
    public StandType<T> addItemOnResolveLevel(int resolveLevel, ItemStack item) {
        if (item != null && !item.isEmpty()) {
            resolveLevelItems.computeIfAbsent(resolveLevel, lvl -> new ArrayList<>()).add(item);
        }
        return this;
    }
    
    @Override
    public boolean keepOnDeath(IStandPower power) {
        return JojoModConfig.getCommonConfigInstance().keepStandOnDeath.get();
    }
    
    public T getStats() {
        return StandStatsManager.getInstance().getStats(this);
    }
    
    public T getDefaultStats() {
        return defaultStats;
    }
    
    public Class<T> getStatsClass() {
        return statsClass;
    }
    
    @Override
    public int getColor() {
        return color;
    }

    @Override
    public boolean isReplaceableWith(StandType<?> newType) {
        return false;
    }

    @Override
    public void tickUser(LivingEntity user, IStandPower power) {
        if (!power.canUsePower()) {
            forceUnsummon(user, power);
        }
    }
    
    @Override
    public StandAction[] getAttacks() {
        return attacks;
    }

    @Override
    public StandAction[] getAbilities() {
        return abilities;
    }
    
    public boolean usesStamina() {
        return false;
    }
    
    public float getMaxStamina(IStandPower power) {
        return 1000;
    }
    
    public float getStaminaRegen(IStandPower power) {
        return 2.5F;
    }
    
    public boolean usesResolve() {
        return false;
    }
    
    public int getMaxResolveLevel() {
        return 4;
    }
    
    public void onNewResolveLevel(IStandPower power) {
        LivingEntity user = power.getUser();
        if (!user.level.isClientSide()) {
            unlockNewActions(power);
            
            if (user instanceof PlayerEntity) {
                List<ItemStack> giveItems = resolveLevelItems.get(power.getResolveLevel());
                if (giveItems != null) {
                    PlayerEntity player = (PlayerEntity) user;
                    giveItems.forEach(item -> player.addItem(item.copy()));
                }
            }
        }
    }
    
    public void unlockNewActions(IStandPower power) {
        Stream.concat(Arrays.stream(attacks), Arrays.stream(abilities))
        .forEach(action -> {
            tryUnlock(action, power);
            if (action.hasShiftVariation()) {
                tryUnlock((StandAction) action.getShiftVariationIfPresent(), power);
            }
        });
    }
    
    private void tryUnlock(StandAction action, IStandPower power) {
        if (action.canBeUnlocked(power)) {
            power.unlockAction(action);
        }
    }
    
    public boolean usesStandComboMechanic() {
        return false;
    }
    
    @Override
    public int getExpRewardMultiplier() {
        return tier + 1;
    }
    
    @Override
    public String getTranslationKey() {
        if (translationKey == null) {
            translationKey = Util.makeDescriptionId("stand", ModStandTypes.Registry.getRegistry().getKey(this));
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
        return "stand";
    }
    
    public ITextComponent getPartName() {
        return partName;
    }

    public int getTier() {
        return tier;
    }
    
    public void toggleSummon(IStandPower standPower) {
        if (!standPower.isActive()) {
            summon(standPower.getUser(), standPower, false);
        }
        else {
            unsummon(standPower.getUser(), standPower);
        }
    }
    
    protected void triggerAdvancement(IStandPower standPower, IStandManifestation stand) {
        if (standPower.getUser() instanceof ServerPlayerEntity) {
            ModCriteriaTriggers.SUMMON_STAND.get().trigger((ServerPlayerEntity) standPower.getUser(), standPower);
        }
    }

    public boolean summon(LivingEntity user, IStandPower standPower, boolean withoutNameVoiceLine) {
        if (!standPower.canUsePower()) {
            return false;
        }
        if (!withoutNameVoiceLine && !user.isShiftKeyDown()) {
            SoundEvent shout = summonShoutSupplier.get();
            if (shout != null) {
                JojoModUtil.sayVoiceLine(user, shout);
            }
        }
        triggerAdvancement(standPower, standPower.getStandManifestation());
        return true;
    }
    
    @Nullable
    public SoundEvent getOst(int level) {
        if (ostSupplier == null) {
            return null;
        }
        return ostSupplier.get(level);
    }
    
    public abstract void unsummon(LivingEntity user, IStandPower standPower);
    
    public abstract void forceUnsummon(LivingEntity user, IStandPower standPower);
    
    public boolean isStandSummoned(LivingEntity user, IStandPower standPower) {
        return standPower.getStandManifestation() != null;
    }
    
    public static void onHurtByStand(DamageSource dmgSource, LivingEntity target) {
        if (dmgSource instanceof IStandDamageSource) {
            IStandPower attackerStand = ((IStandDamageSource) dmgSource).getStandPower();
            target.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.setLastHurtByStand(attackerStand);
            });
        }
    }

//    public static void giveStandExp(LivingDeathEvent event) {
//        LivingEntity dead = event.getEntityLiving();
//        dead.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
//            IStandPower stand = cap.getLastHurtByStand();
//            if (stand != null) {
//                int expToAdd = 0;
//                if (dead.getType().getCategory() == EntityClassification.MONSTER) {
//                    expToAdd = 4;
//                }
//                else if (dead instanceof PlayerEntity) {
//                    expToAdd = 20;
//                }
//                else {
//                    return;
//                }
//                
//                expToAdd *= IStandPower.getStandPowerOptional(dead).map(deadPower -> {
//                    StandType<?> type = deadPower.getType();
//                    return type != null ? type.getExpRewardMultiplier() : 1;
//                }).orElse(1);
//                expToAdd *= INonStandPower.getNonStandPowerOptional(dead).map(deadPower -> {
//                    NonStandPowerType<?> type = deadPower.getType();
//                    return type != null ? type.getExpRewardMultiplier() : 1;
//                }).orElse(1);
//                expToAdd = (int) ((float) expToAdd * dead.getMaxHealth() / 20F);
//                stand.setXp(stand.getXp() + expToAdd);
//            }
//        });
//    }
}
