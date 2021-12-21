package com.github.standobyte.jojo.power.nonstand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.PowerBaseImpl;
import com.github.standobyte.jojo.power.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.stand.IStandPower;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public class NonStandPower extends PowerBaseImpl<INonStandPower, NonStandPowerType<?>> implements INonStandPower {
    private NonStandPowerType<?> type;
    private TypeSpecificData typeSpecificData;
    
    public NonStandPower(LivingEntity user) {
        super(user);
    }

    @Override
    public PowerClassification getPowerClassification() {
        return PowerClassification.NON_STAND;
    }

    @Override
    public boolean givePower(NonStandPowerType<?> type) {
        NonStandPowerType<?> oldType = this.getType();
        if (super.givePower(type)) {
            manaRegenPoints = type.getStartingManaRegenPoints();
            manaLimitFactor = 1F;
            typeSpecificData.onPowerGiven(oldType);
            return true;
        }
        return false;
    }

    @Override
    protected void onTypeInit(NonStandPowerType<?> powerType) {
        attacks = new ArrayList<Action>(Arrays.asList(powerType.getAttacks()));
        abilities = new ArrayList<Action>(Arrays.asList(powerType.getAbilities()));
        TypeSpecificData data = powerType.newSpecificDataInstance();
        setTypeSpecificData(data);
        this.type = powerType;
    }

    @Override
    public NonStandPowerType<?> getType() {
        return type;
    }
    
    @Override
    public boolean clear() {
        if (super.clear()) {
            manaRegenPoints = 1F;
            manaLimitFactor = 1F;
            type.onClear(this);
            typeSpecificData = null;
            type = null;
            serverPlayerUser.ifPresent(player -> {
                IStandPower standPower = IStandPower.getPlayerStandPower(player);
                standPower.setManaRegenPoints(1);
                standPower.setManaLimitFactor(1);
            });
            return true;
        }
        return false;
    }
    
    @Override
    public boolean isActive() {
        return true;
    }
    
    @Override
    protected float reduceManaConsumed(float amount) {
        return getType() == null ? super.reduceManaConsumed(amount) : getType().reduceManaConsumed(amount, this, user);
    }
    
    @Override
    public boolean isActionUnlocked(Action action) {
        return typeSpecificData == null ? false : typeSpecificData.isActionUnlocked(action, this);
    }
    
    @Override
    public boolean isLeapUnlocked() {
        return type.isLeapUnlocked(this);
    }
    
    @Override
    public void onLeap() {
        super.onLeap();
        type.onLeap(this);
    }
    
    @Override
    public float leapStrength() {
        return type.getLeapStrength(this);
    }
    
    @Override
    public int getLeapCooldownPeriod() {
        return type.getLeapCooldownPeriod();
    }
    
    @Override
    protected float getLeapManaCost() {
        return type.getLeapManaCost();
    }
    
    @Override
    public <T extends NonStandPowerType<D>, D extends TypeSpecificData> Optional<D> getTypeSpecificData(@Nullable T requiredType) {
        if (typeSpecificData != null && (requiredType == null || requiredType == type)) {
            return Optional.of((D) typeSpecificData);
        }
        return Optional.empty();
    }
    
    private void setTypeSpecificData(TypeSpecificData data) {
        this.typeSpecificData = data;
        this.typeSpecificData.setPower(this);
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT cnbt = super.writeNBT();
        cnbt.putString("Type", ModNonStandPowers.Registry.getKeyAsString(getType()));
        getTypeSpecificData(null).ifPresent(data -> {
            cnbt.put("AdditionalData", data.writeNBT());
        });
        return cnbt;
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
        String powerName = nbt.getString("Type");
        if (powerName != IPowerType.NO_POWER_NAME) {
            NonStandPowerType<?> type = ModNonStandPowers.Registry.getRegistry().getValue(new ResourceLocation(powerName));
            if (type != null) {
                onTypeInit(type);
                TypeSpecificData data = type.newSpecificDataInstance();
                if (data != null) {
                    setTypeSpecificData(data);
                    data.readNBT(nbt.getCompound("AdditionalData"));
                }
            }
        }
        super.readNBT(nbt);
    }
    
    @Override
    public void onClone(IPower<NonStandPowerType<?>> oldPower, boolean wasDeath, boolean keep) {
        super.onClone(oldPower, wasDeath, keep);
        if (keep && oldPower.hasPower()) {
            ((INonStandPower) oldPower).getTypeSpecificData(null).ifPresent(data -> {
                setTypeSpecificData(data);
            });
        }
    }
    
    @Override
    public void syncWithUserOnly() {
        super.syncWithUserOnly();
        serverPlayerUser.ifPresent(player -> {
            if (hasPower()) {
                getTypeSpecificData(null).ifPresent(data -> {
                    data.syncWithUserOnly(player);
                });
            }
        });
    }
    
    @Override
    public void syncWithTrackingOrUser(ServerPlayerEntity player) {
        super.syncWithTrackingOrUser(player);
        if (hasPower()) {
            getTypeSpecificData(null).ifPresent(data -> {
                data.syncWithTrackingOrUser(getUser(), player);
            });
        }
    }
}