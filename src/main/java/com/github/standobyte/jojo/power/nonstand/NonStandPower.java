package com.github.standobyte.jojo.power.nonstand;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.init.ModNonStandPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrEnergyPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrTypeNonStandPowerPacket;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.PowerBaseImpl;
import com.github.standobyte.jojo.power.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.util.Container;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class NonStandPower extends PowerBaseImpl<INonStandPower, NonStandPowerType<?>> implements INonStandPower {
    public static final float BASE_MAX_ENERGY = 1000;
    
    private float energy = 0;
    protected NonStandPowerType<?> type;
    private TypeSpecificData typeSpecificData;
    
    public NonStandPower(LivingEntity user) {
        super(user);
    }

    @Override
    public PowerClassification getPowerClassification() {
        return PowerClassification.NON_STAND;
    }

    @Override
    public boolean hasPower() {
        return getType() != null;
    }

    @Override
    public boolean givePower(NonStandPowerType<?> type) {
        if (!canGetPower(type)) {
            return false;
        }
        NonStandPowerType<?> oldType = this.getType();
        setType(type);
        onNewPowerGiven(type);
        typeSpecificData.onPowerGiven(oldType);
        return true;
    }
    
    private void setType(NonStandPowerType<?> powerType) {
        this.type = powerType;
        onPowerSet(powerType);
    }

    @Override
    protected void onNewPowerGiven(NonStandPowerType<?> powerType) {
        super.onNewPowerGiven(powerType);
        TypeSpecificData data = powerType.newSpecificDataInstance();
        energy = 0;
        setTypeSpecificData(data);
        if (isUserCreative()) {
            energy = getMaxEnergy();
        }
        serverPlayerUser.ifPresent(player -> {
            PacketManager.sendToClientsTrackingAndSelf(new TrTypeNonStandPowerPacket(player.getId(), getType()), player);
        });
    }
    
    @Override
    public boolean clear() {
        if (super.clear()) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClientsTrackingAndSelf(TrTypeNonStandPowerPacket.noPowerType(player.getId()), player);
            });
            type.onClear(this);
            NonStandPowerType<?> clearedType = this.type;
            setType(null);
            clearedType.afterClear(this);
            typeSpecificData = null;
            energy = 0;
            return true;
        }
        return false;
    }

    @Override
    public NonStandPowerType<?> getType() {
        return type;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (hasPower()) {
            tickEnergy();
        }
    }
    
    @Override
    public boolean isActive() {
        return true;
    }
    
    @Override
    public ActionConditionResult checkRequirements(Action<INonStandPower> action, Container<ActionTarget> targetContainer, boolean checkTargetType) {
        ActionConditionResult result = super.checkRequirements(action, targetContainer, checkTargetType);
        if (!result.isPositive()) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClient(new TrEnergyPacket(player.getId(), getEnergy()), player);
            });
        }
        return result;
    }

    @Override
    public float getEnergy() {
        return energy;
    }

    @Override
    public float getMaxEnergy() {
        float maxAmount = BASE_MAX_ENERGY;
        if (type != null) {
            maxAmount *= Math.max(type.getMaxEnergyFactor(this), 0.001F);
        }
        return maxAmount;
    }
    
    @Override
    public boolean hasEnergy(float amount) {
        return getEnergy() >= reduceEnergyConsumed(amount) || isUserCreative();
    }

    @Override
    public void addEnergy(float amount) {
        setEnergy(MathHelper.clamp(this.energy + amount, 0, getMaxEnergy()));
    }

    @Override
    public boolean consumeEnergy(float amount) {
        if (isUserCreative()) {
            return true;
        }
        if (hasEnergy(amount)) {
            setEnergy(this.energy - reduceEnergyConsumed(amount));
            return true;
        }
        return false;
    }
    
    protected float reduceEnergyConsumed(float amount) {
        return getType() == null ? amount : getType().reduceEnergyConsumed(amount, this, user);
    }

    @Override
    public void setEnergy(float amount) {
        amount = MathHelper.clamp(amount, 0, getMaxEnergy());
        boolean send = this.energy != amount;
        this.energy = amount;
        if (send) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClientsTrackingAndSelf(new TrEnergyPacket(player.getId(), getEnergy()), player);
            });
        }
    }
    
    private void tickEnergy() {
        float inc = type.getEnergyTickInc(this);
        if (isUserCreative()) {
            inc = Math.max(inc, 0);
        }
        energy = MathHelper.clamp(energy + inc, 0, getMaxEnergy());
    }
    
    @Override
    public boolean isLeapUnlocked() {
        return type.isLeapUnlocked(this);
    }
    
    @Override
    public boolean canLeap() {
        return super.canLeap() && hasEnergy(type.getLeapEnergyCost());
    }
    
    @Override
    public void onLeap() {
        super.onLeap();
        consumeEnergy(type.getLeapEnergyCost());
        type.onLeap(this);
    }
    
    @Override
    public float leapStrength() {
    	float strength = type.getLeapStrength(this);
    	if (user != null) {
    		ModifiableAttributeInstance speedAttribute = user.getAttribute(Attributes.MOVEMENT_SPEED);
    		if (speedAttribute != null) {
    			strength *= speedAttribute.getValue() / speedAttribute.getBaseValue();
    		}
    	}
    	return strength;
    }
    
    @Override
    public int getLeapCooldownPeriod() {
        return type.getLeapCooldownPeriod();
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
        cnbt.putFloat("Energy", energy);
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
                setType(type);
                TypeSpecificData data = type.newSpecificDataInstance();
                if (data != null) {
                    setTypeSpecificData(data);
                    data.readNBT(nbt.getCompound("AdditionalData"));
                }
            }
        }
        energy = nbt.contains("Mana", 5) ? // TODO remove in a later version
                nbt.getFloat("Mana")
                : nbt.getFloat("Energy");
        super.readNBT(nbt);
    }
    
    @Override
    protected void keepPower(INonStandPower oldPower, boolean wasDeath) {
        super.keepPower(oldPower, wasDeath);
        setType(oldPower.getType());
        this.energy = oldPower.getEnergy();
        oldPower.getTypeSpecificData(null).ifPresent(data -> {
            this.setTypeSpecificData(data);
        });
    }
    
    @Override
    public void syncWithUserOnly() {
        super.syncWithUserOnly();
        serverPlayerUser.ifPresent(player -> {
            if (hasPower()) {
                getTypeSpecificData(null).ifPresent(data -> {
                    data.syncWithUserOnly(player);
                });
                PacketManager.sendToClient(new TrEnergyPacket(player.getId(), energy), player);
            }
        });
    }
    
    @Override
    public void syncWithTrackingOrUser(ServerPlayerEntity player) {
        super.syncWithTrackingOrUser(player);
        if (hasPower() && user != null) {
            PacketManager.sendToClient(new TrTypeNonStandPowerPacket(user.getId(), getType()), player);
            PacketManager.sendToClient(new TrEnergyPacket(user.getId(), energy), player);
            getTypeSpecificData(null).ifPresent(data -> {
                data.syncWithTrackingOrUser(user, player);
            });
        }
    }
    
}