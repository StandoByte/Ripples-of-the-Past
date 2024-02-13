package com.github.standobyte.jojo.power;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.controls.HudControlSettings;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.bowcharge.BowChargeEffectInstance;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.general.ObjectWrapper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.IForgeRegistry;

public interface IPower<P extends IPower<P, T>, T extends IPowerType<P, T>> {
    PowerClassification getPowerClassification();
    boolean hasPower();
    boolean canGetPower(T type);
    boolean givePower(T type);
    boolean clear();
    T getType();
    LivingEntity getUser();
    boolean isUserCreative();
    void tick();
    boolean isActive();
    
    default ITextComponent getName() {
        return hasPower() ? getType().getName() : StringTextComponent.EMPTY;
    }
    
    boolean isActionOnCooldown(Action<?> action);
    float getCooldownRatio(Action<?> action, float partialTick);
    int getCooldownTimer(Action<?> action);
    void setCooldownTimer(Action<?> action, int value);
    void updateCooldownTimer(Action<?> action, int value, int totalCooldown);
    void resetCooldowns();
    ActionCooldownTracker getCooldowns();
    
    boolean clickAction(Action<P> action, boolean sneak, ActionTarget target, @Nullable PacketBuffer extraInput);
    ActionConditionResult checkRequirements(Action<P> action, ObjectWrapper<ActionTarget> targetContainer, boolean checkTargetType);
    ActionConditionResult checkTarget(Action<P> action, ObjectWrapper<ActionTarget> targetContainer);
    boolean canUsePower();
    
    default RayTraceResult clientHitResult(Entity cameraEntity, RayTraceResult mcHitResult) {
        return getType() != null ? getType().clientHitResult((P) this, cameraEntity, mcHitResult) : mcHitResult;
    }
    
    float getLearningProgressRatio(Action<P> action);

    void setHeldAction(Action<P> action);
    @Nullable default Action<P> getHeldAction() {
        return getHeldAction(false);
    }
    @Nullable Action<P> getHeldAction(boolean checkRequirements);
    void refreshHeldActionTickState(boolean requirementsFulfilled);
    int getHeldActionTicks();
    void stopHeldAction(boolean shouldFire);

    void setMouseTarget(ActionTarget target);
    @Nullable ActionTarget getMouseTarget();
    boolean isTargetUpdateTick();
    
    void onUserGettingAttacked(DamageSource dmgSource, float dmgAmount);
    float getTargetResolveMultiplier(IStandPower attackingStand);
    
    boolean canLeap();
    boolean isLeapUnlocked();
    float leapStrength();
    void onLeap();
    int getLeapCooldown();
    void setLeapCooldown(int cooldown);
    int getLeapCooldownPeriod();
    
    void onItemUseStart(ItemStack item, int duration);
    void onItemUseStop(ItemStack item, int duration);
    @Nullable BowChargeEffectInstance<P, T> getBowChargeEffect();
    
    ResourceLocation clGetPowerTypeIcon();
    default void clUpdateHud() {
        LivingEntity user = getUser();
        if (user != null && user.level.isClientSide() && user == ClientUtil.getCameraEntity()) {
            HudControlSettings.getInstance().refreshControls(this);
        }
    }
    
    INBT writeNBT();
    void readNBT(CompoundNBT nbt);
    void onClone(P oldPower, boolean wasDeath);
    void syncWithUserOnly();
    void syncWithTrackingOrUser(ServerPlayerEntity player);

    public static LazyOptional<? extends IPower<?, ?>> getPowerOptional(LivingEntity entity, PowerClassification classification) {
        return classification == PowerClassification.STAND ? IStandPower.getStandPowerOptional(entity) : INonStandPower.getNonStandPowerOptional(entity);
    }

    public static IPower<?, ?> getPlayerPower(PlayerEntity player, PowerClassification classification) {
        return classification == PowerClassification.STAND ? IStandPower.getPlayerStandPower(player) : INonStandPower.getPlayerNonStandPower(player);
    }
    
    public static enum PowerClassification {
        STAND(IStandPower.class) {
            @Override
            public void writePowerType(IPowerType<?, ?> powerType, PacketBuffer buf) {
                buf.writeRegistryId((StandType<?>) powerType);
            }

            @SuppressWarnings("unchecked")
            @Override
            public IPowerType<?, ?> readPowerType(PacketBuffer buf) {
                return buf.readRegistryIdSafe(StandType.class);
            }

            @Override
            public StandType<?> getFromRegistryId(ResourceLocation id) {
                IForgeRegistry<StandType<?>> registry = JojoCustomRegistries.STANDS.getRegistry();
                if (registry.containsKey(id)) {
                    return registry.getValue(id);
                }
                return null;
            }
        },
        NON_STAND(INonStandPower.class) {
            @Override
            public void writePowerType(IPowerType<?, ?> powerType, PacketBuffer buf) {
                buf.writeRegistryId((NonStandPowerType<?>) powerType);
            }

            @SuppressWarnings("unchecked")
            @Override
            public IPowerType<?, ?> readPowerType(PacketBuffer buf) {
                return buf.readRegistryIdSafe(NonStandPowerType.class);
            }

            @Override
            public NonStandPowerType<?> getFromRegistryId(ResourceLocation id) {
                IForgeRegistry<NonStandPowerType<?>> registry = JojoCustomRegistries.NON_STAND_POWERS.getRegistry();
                if (registry.containsKey(id)) {
                    return registry.getValue(id);
                }
                return null;
            }
        };
        
        private final Class<? extends IPower<?, ?>> powerClass;
        
        private PowerClassification(Class<? extends IPower<?, ?>> powerClass) {
            this.powerClass = powerClass;
        }
        
        public Class<? extends IPower<?, ?>> getPowerClass() {
            return powerClass;
        }
        
        public abstract void writePowerType(IPowerType<?, ?> powerType, PacketBuffer buf);
        public abstract IPowerType<?, ?> readPowerType(PacketBuffer buf);
        @Nullable public abstract IPowerType<?, ?> getFromRegistryId(ResourceLocation id);
    }
    
    // TODO change PowerClassification from Enum to the custom one
//    public static class PowerClassification<P extends IPower<P, T>, T extends IPowerType<P, T>> {
//        public static final PowerClassification<IStandPower, StandType<?>> STAND = new PowerClassification<>("stand", 0);
//        public static final PowerClassification<INonStandPower, NonStandPowerType<?>> NON_STAND = new PowerClassification<>("non_stand", 1);
//        
//        private static final List<PowerClassification<?, ?>> VALUES = ImmutableList.of(STAND, NON_STAND);
//        private final String name;
//        private final int id;
//        
//        private PowerClassification(String name, int id) {
//            this.name = name;
//            this.id = id;
//        }
//        
//        public String name() {
//            return name;
//        }
//        
//        @Nullable
//        public static PowerClassification<?, ?> valueOf(String name) {
//            if (STAND.name.equals(name)) {
//                return STAND;
//            }
//            if (NON_STAND.name.equals(name)) {
//                return NON_STAND;
//            }
//            return null;
//        }
//        
//        public static PowerClassification<?, ?> read(PacketBuffer buf) {
//            return VALUES.get(buf.readVarInt());
//        }
//        
//        public void write(PacketBuffer buf) {
//            buf.writeVarInt(id);
//        }
//        
//        public static List<PowerClassification<?, ?>> values() {
//            return VALUES;
//        }
//    }
}
