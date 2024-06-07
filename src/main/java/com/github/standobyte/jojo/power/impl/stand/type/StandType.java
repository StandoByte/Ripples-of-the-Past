package com.github.standobyte.jojo.power.impl.stand.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.advancements.ModCriteriaTriggers;
import com.github.standobyte.jojo.capability.entity.LivingUtilCapProvider;
import com.github.standobyte.jojo.client.controls.ControlScheme;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.command.configpack.StandStatsConfig;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.impl.stand.IStandManifestation;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.util.mc.OstSoundList;
import com.github.standobyte.jojo.util.mc.damage.IStandDamageSource;
import com.github.standobyte.jojo.util.mod.JojoModUtil;
import com.google.common.collect.Iterables;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class StandType<T extends StandStats> extends ForgeRegistryEntry<StandType<?>> implements IPowerType<IStandPower, StandType<?>> {
    @Deprecated
    private final int color;
    
    private final StandAction[] leftClickHotbar;
    private final StandAction[] rightClickHotbar;
    private final StandAction defaultMMBAction;
    private List<Pair<String, StandAction>> defaultKeys = null;
    
    private String translationKey;
    private ResourceLocation iconTexture;
    
    private final ITextComponent partName;
    private final T defaultStats;
    private final Class<T> statsClass;
    private final StandSurvivalGameplayPool survivalGameplayPool;
    private final Supplier<SoundEvent> summonShoutSupplier;
    private final OstSoundList ostSupplier;
    private final Map<Integer, List<ItemStack>> resolveLevelItems;
    private final int resolveMultiplierTier;
    
    @Deprecated
    public StandType(int color, ITextComponent partName, 
            StandAction[] leftClickHotbar, StandAction[] rightClickHotbar, StandAction defaultQuickAccess, 
            Class<T> statsClass, T defaultStats, @Nullable StandTypeOptionals additions) {
        this.color = color;
        this.partName = partName;
        this.leftClickHotbar = leftClickHotbar;
        this.rightClickHotbar = rightClickHotbar;
        this.defaultMMBAction = defaultQuickAccess;
        this.statsClass = statsClass;
        this.defaultStats = defaultStats;
        
        if (additions == null) additions = new StandTypeOptionals();
        this.ostSupplier = additions.ostSupplier;
        this.summonShoutSupplier = additions.summonShoutSupplier;
        this.survivalGameplayPool = additions.survivalGameplayPool;
        this.resolveLevelItems = additions.resolveLevelItems;
        this.resolveMultiplierTier = additions.resolveMultiplierTier;
    }
    
    protected StandType(AbstractBuilder<?, T> builder) {
        this(builder.color, builder.storyPartName, builder.leftClickHotbar, builder.rightClickHotbar, 
                builder.mmbAction, builder.statsClass, builder.defaultStats, 
                builder.additions);
        if (!builder.defaultKeys.isEmpty()) {
            this.defaultKeys = builder.defaultKeys;
        }
    }
    

    
    public static abstract class AbstractBuilder<B extends AbstractBuilder<B, T>, T extends StandStats> {
        private int color = 0x000000;
        private ITextComponent storyPartName = StringTextComponent.EMPTY;
        private StandAction[] leftClickHotbar = {};
        private StandAction[] rightClickHotbar = {};
        private StandAction mmbAction = null;
        private List<Pair<String, StandAction>> defaultKeys = new ArrayList<>();
        private T defaultStats;
        private Class<T> statsClass;
        private StandTypeOptionals additions = null;
        
        public B color(int color) {
            this.color = color;
            return getThis();
        }
        
        public B storyPartName(ITextComponent actions) {
            this.storyPartName = actions;
            return getThis();
        }
        
        public B leftClickHotbar(StandAction... actions) {
            this.leftClickHotbar = actions;
            return getThis();
        }
        
        public B rightClickHotbar(StandAction... actions) {
            this.rightClickHotbar = actions;
            if (mmbAction == null) {
                mmbAction = actions.length > 0 ? actions[0] : null;
            }
            return getThis();
        }
        
        /**
         * @deprecated use {@link AbstractBuilder#defaultMMB(StandAction)}
         */
        @Deprecated
        public B defaultQuickAccess(StandAction mmbAction) {
            return defaultMMB(mmbAction);
        }
        
        public B defaultMMB(StandAction quickAccess) {
            this.mmbAction = quickAccess;
            return getThis();
        }
        
        public B defaultKey(StandAction action, String keyName) {
            defaultKeys.add(Pair.of(keyName, action));
            return getThis();
        }
        
        public B defaultStats(Class<T> statsClass, T stats) {
            this.statsClass = statsClass;
            this.defaultStats = stats;
            return getThis();
        }
        
        public B defaultStats(Class<T> statsClass, StandStats.AbstractBuilder<?, T> statsBuilder) {
            return defaultStats(statsClass, statsBuilder.build());
        }
        
        public B addSummonShout(Supplier<SoundEvent> summonShoutSupplier) {
            if (summonShoutSupplier != null) {
                getOptionals().summonShoutSupplier = summonShoutSupplier;
            }
            return getThis();
        }
        
        public B addOst(OstSoundList ostSupplier) {
            if (ostSupplier != null) {
                getOptionals().ostSupplier = ostSupplier;
            }
            return getThis();
        }
        
        public B addItemOnResolveLevel(int resolveLevel, ItemStack item) {
            if (item != null && !item.isEmpty()) {
                getOptionals().resolveLevelItems.computeIfAbsent(resolveLevel, lvl -> new ArrayList<>()).add(item);
            }
            return getThis();
        }
        
        public B setSurvivalGameplayPool(StandSurvivalGameplayPool survivalGameplayPool) {
            getOptionals().survivalGameplayPool = survivalGameplayPool;
            return getThis();
        }
        
        public B addAttackerResolveMultTier(int tierAdd) {
            getOptionals().resolveMultiplierTier += tierAdd;
            return getThis();
        }
        
        private StandTypeOptionals getOptionals() {
            if (additions == null) {
                additions = new StandTypeOptionals();
            }
            return additions;
        }
        
        protected abstract B getThis();
        public abstract StandType<T> build();
    }
    
    

    private Set<StandAction> allUnlockableActions;
    public void onCommonSetup() {
        Set<StandAction> unlockables = new HashSet<>();
        Collections.addAll(unlockables, leftClickHotbar);
        Collections.addAll(unlockables, rightClickHotbar);
        
        unlockables.addAll(
                unlockables.stream().filter(Action::hasShiftVariation)
                .map(action -> (StandAction) action.getShiftVariationIfPresent())
                .collect(Collectors.toSet())
                );
        
        addAllExtraUnlockables(unlockables);
        
        this.allUnlockableActions = Collections.unmodifiableSet(unlockables);
    }
    
    private static void addAllExtraUnlockables(Set<StandAction> actions) {
        Set<StandAction> newSet = new HashSet<>();
        for (StandAction action : actions) {
            newSet.addAll(action.getExtraUnlockables());
            if (!newSet.isEmpty()) {
                addAllExtraUnlockables(newSet);
            }
        }
        actions.addAll(newSet);
    }
    
    @Override
    public boolean keepOnDeath(IStandPower power) {
        return JojoModConfig.getCommonConfigInstance(false).keepStandOnDeath.get();
    }
    
    public T getStats() {
        StandStatsConfig statsManager = StandStatsConfig.getInstance();
        return statsManager != null ? statsManager.getStats(this) : getDefaultStats();
    }
    
    public T getDefaultStats() {
        return defaultStats;
    }
    
    public Class<T> getStatsClass() {
        return statsClass;
    }
    
    @Deprecated
    public int getColor() {
        return color;
    }

    @Override
    public boolean isReplaceableWith(StandType<?> newType) {
        return false;
    }
    
    public StandSurvivalGameplayPool getSurvivalGameplayPool() {
        return survivalGameplayPool;
    }

    @Override
    public void tickUser(LivingEntity user, IStandPower power) {
        if (!power.canUsePower()) {
            forceUnsummon(user, power);
        }
    }
    
    @Override
    public void onNewDay(LivingEntity user, IStandPower power, long prevDay, long day) {
        getStats().onNewDay(user, power);
    }
    
    @Deprecated
    public StandAction[] getDefaultHotbar(ControlScheme.Hotbar hotbar) {
        switch (hotbar) {
        case LEFT_CLICK: return leftClickHotbar;
        case RIGHT_CLICK: return rightClickHotbar;
        default: throw new IllegalArgumentException();
        }
    }
    
    @Override
    public ControlScheme.DefaultControls clCreateDefaultLayout() {
        ControlScheme.DefaultControls controls = new ControlScheme.DefaultControls(
                leftClickHotbar, 
                rightClickHotbar, 
                ControlScheme.DefaultControls.DefaultKey.mmb(defaultMMBAction));
        if (defaultKeys != null) {
            for (Pair<String, StandAction> actionKey : defaultKeys) {
                controls.addKey(ControlScheme.DefaultControls.DefaultKey.of(actionKey.getValue(), actionKey.getKey()));
            }
        }
        return controls;
    }
    
    @Override
    public void clAddMissingActions(ControlScheme controlScheme, IStandPower power) {
        for (Action<?> attack : leftClickHotbar) {
            controlScheme.addIfMissing(ControlScheme.Hotbar.LEFT_CLICK, attack);
        }
        for (Action<?> ability : rightClickHotbar) {
            controlScheme.addIfMissing(ControlScheme.Hotbar.RIGHT_CLICK, ability);
        }
    }
    
    @Override
    public boolean isActionLegalInHud(Action<IStandPower> action, IStandPower power) {
        return Iterables.contains(getAllUnlockableActions(), action) && action.isLegalInHud(power);
    }
    
    public Iterable<StandAction> getAllUnlockableActions() {
        return allUnlockableActions;
    }
    
    public boolean usesStamina() {
        return false;
    }
    
    public float getMaxStamina(IStandPower power) {
        return 1000;
    }
    
    public float getStaminaRegen(IStandPower power) {
        return 3F;
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
        getAllUnlockableActions().forEach(action -> {
            tryUnlock(action, power);
        });
    }
    
    private void tryUnlock(StandAction action, IStandPower power) {
        if (action.canBeUnlocked(power)) {
            power.unlockAction(action);
        }
    }
    
    private static final Optional<StandAction> NOPE = Optional.empty();
    public Optional<StandAction> getStandFinisherPunch() {
        return NOPE;
    }
    
    @Override
    public float getTargetResolveMultiplier(IStandPower power, IStandPower attackingStand) {
        float multiplier = resolveMultiplierTier + 1;
        if (attackingStand.hasPower()) {
            multiplier = Math.max(multiplier - attackingStand.getType().resolveMultiplierTier, 1);
        }
        return multiplier;
    }
    
    public ITextComponent getPartName() {
        return partName;
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
    
    public boolean canBeManuallyControlled() {
        return false;
    }
    
    public boolean canLeap() {
        return false;
    }
    
    @Nullable
    public OstSoundList getOst() {
        return ostSupplier;
    }
    
    public abstract void unsummon(LivingEntity user, IStandPower standPower);
    
    public abstract void forceUnsummon(LivingEntity user, IStandPower standPower);
    
    public boolean isStandSummoned(LivingEntity user, IStandPower standPower) {
        return standPower.getStandManifestation() != null;
    }
    
    public static void onHurtByStand(DamageSource dmgSource, float dmgAmount, LivingEntity target) {
        if (dmgSource instanceof IStandDamageSource) {
            IStandDamageSource standDmgSource = (IStandDamageSource) dmgSource;
            IStandPower attackerStand = standDmgSource.getStandPower();
            target.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.setLastHurtByStand(attackerStand, dmgAmount, standDmgSource.getStandInvulTicks());
            });
        }
    }
    
    @Override
    public String getTranslationKey() {
        if (translationKey == null) {
            translationKey = Util.makeDescriptionId("stand", JojoCustomRegistries.STANDS.getRegistry().getKey(this));
        }
        return this.translationKey;
    }

    @Override
    public ResourceLocation getIconTexture(@Nullable IStandPower power) {
        if (iconTexture == null) {
            iconTexture = JojoModUtil.makeTextureLocation("power", getRegistryName().getNamespace(), getRegistryName().getPath());
        }
        
        if (power != null && power.getType() == this) {
            return StandSkinsManager.getInstance().getRemappedResPath(
                    manager -> manager.getStandSkin(power.getStandInstance().get()), iconTexture);
        }
        
        return this.iconTexture;
    }
    
    public void onStandSkinSet(IStandPower power, Optional<ResourceLocation> skin) {}
    
    
    @Deprecated
    public static class StandTypeOptionals {
        private StandSurvivalGameplayPool survivalGameplayPool = StandSurvivalGameplayPool.PLAYER_ARROW; 
        private Supplier<SoundEvent> summonShoutSupplier = () -> null;
        private OstSoundList ostSupplier = null;
        private Map<Integer, List<ItemStack>> resolveLevelItems = new HashMap<>();
        private int resolveMultiplierTier = 5;
        
        public StandTypeOptionals addSummonShout(Supplier<SoundEvent> summonShoutSupplier) {
            if (summonShoutSupplier != null) {
                this.summonShoutSupplier = summonShoutSupplier;
            }
            return this;
        }
        
        public StandTypeOptionals addOst(OstSoundList ostSupplier) {
            if (ostSupplier != null) {
                this.ostSupplier = ostSupplier;
            }
            return this;
        }
        
        public StandTypeOptionals addItemOnResolveLevel(int resolveLevel, ItemStack item) {
            if (item != null && !item.isEmpty()) {
                resolveLevelItems.computeIfAbsent(resolveLevel, lvl -> new ArrayList<>()).add(item);
            }
            return this;
        }
    }
    
    
    public static enum StandSurvivalGameplayPool {
        PLAYER_ARROW,
        NON_ARROW, // Requiems, C-Moon, Made in Heaven, Acts depending on their implementation, etc.
        NPC_ENCOUNTER,
        ANIMAL,
        OTHER
    }
}
