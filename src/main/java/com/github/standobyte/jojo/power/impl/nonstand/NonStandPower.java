package com.github.standobyte.jojo.power.impl.nonstand;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PreviousPowerTypesPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrEnergyPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrTypeNonStandPowerPacket;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.impl.PowerBaseImpl;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.general.ObjectWrapper;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.registries.IForgeRegistry;

public class NonStandPower extends PowerBaseImpl<INonStandPower, NonStandPowerType<?>> implements INonStandPower {
    public static final float BASE_MAX_ENERGY = 1000;
    
    private float energy = 0;
    protected NonStandPowerType<?> type;
    private TypeSpecificData typeSpecificData;
    private Set<NonStandPowerType<?>> hadPowers = new HashSet<>();
    
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
        TypeSpecificData oldData = typeSpecificData;
        setType(type);
        onNewPowerGiven(type);
        typeSpecificData.onPowerGiven(oldType, oldData);
        clUpdateHud();
        addHadPowerBefore(type);
        return true;
    }
    
    private void setType(NonStandPowerType<?> powerType) {
        this.type = powerType;
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
        if (user != null && !user.level.isClientSide()) {
            PacketManager.sendToClientsTrackingAndSelf(new TrTypeNonStandPowerPacket(user.getId(), getType()), user);
        }
    }
    
    @Override
    public boolean clear() {
        if (super.clear()) {
            if (user != null && !user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(TrTypeNonStandPowerPacket.noPowerType(user.getId()), user);
            }
            type.onClear(this);
            NonStandPowerType<?> clearedType = this.type;
            setType(null);
            clearedType.afterClear(this);
            typeSpecificData = null;
            energy = 0;
            clUpdateHud();
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
    public ActionConditionResult checkRequirements(Action<INonStandPower> action, ObjectWrapper<ActionTarget> targetContainer, boolean checkTargetType) {
        ActionConditionResult result = super.checkRequirements(action, targetContainer, checkTargetType);
        if (!result.isPositive()) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClient(new TrEnergyPacket(player.getId(), getEnergy()), player);
            });
        }
        return result;
    }
    
    @Override
    public boolean isTargetUpdateTick() {
        return super.isTargetUpdateTick() || GeneralUtil.orElseFalse(getUser().getCapability(PlayerUtilCapProvider.CAPABILITY), 
                player -> player.getContinuousAction().map(action -> action.updateTarget()).orElse(false));
    }
    
    @Override
    public float getEnergy() {
        return energy;
    }

    @Override
    public float getMaxEnergy() {
        if (type != null) {
            return Math.max(type.getMaxEnergy(this), 1F);
        }
        return BASE_MAX_ENERGY;
    }
    
    @Override
    public boolean hasEnergy(float amount) {
        if (getType() == null) {
            return false;
        }
        return isUserCreative() || getType().hasEnergy(this, amount);
    }

    @Override
    public void addEnergy(float amount) {
        setEnergy(MathHelper.clamp(this.energy + amount, 0, getMaxEnergy()));
    }

    @Override
    public boolean consumeEnergy(float amount) {
        if (getType() == null) {
            return false;
        }
        return getType().consumeEnergy(this, amount);
    }

    @Override
    public void setEnergy(float amount) {
        amount = MathHelper.clamp(amount, 0, getMaxEnergy());
        if (this.energy != amount) {
            this.energy = amount;
            if (user != null && !user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrEnergyPacket(user.getId(), getEnergy()), user);
            }
        }
    }
    
    private void tickEnergy() {
        float energy = type.tickEnergy(this);
        this.energy = MathHelper.clamp(energy, 0, getMaxEnergy());
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
    public boolean hadPowerBefore(NonStandPowerType<?> type) {
        return hadPowers.contains(type);
    }
    
    @Override
    public void addHadPowerBefore(NonStandPowerType<?> type) {
        hadPowers.add(type);
        serverPlayerUser.ifPresent(player -> {
            PacketManager.sendToClient(new PreviousPowerTypesPacket(hadPowers), player);
        });
    }
    
    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT cnbt = super.writeNBT();
        cnbt.putFloat("Energy", energy);
        cnbt.putString("Type", JojoCustomRegistries.NON_STAND_POWERS.getKeyAsString(getType()));
        getTypeSpecificData(null).ifPresent(data -> {
            cnbt.put("AdditionalData", data.writeNBT());
        });
        cnbt.put("HadPowers", hadPowers.stream()
                .map(type -> StringNBT.valueOf(type.getRegistryName().toString()))
                .collect(Collectors.toCollection(ListNBT::new)));
        return cnbt;
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
        IForgeRegistry<NonStandPowerType<?>> powerTypeRegistry = JojoCustomRegistries.NON_STAND_POWERS.getRegistry();
        String powerName = nbt.getString("Type");
        if (powerName != IPowerType.NO_POWER_NAME) {
            NonStandPowerType<?> type = powerTypeRegistry.getValue(new ResourceLocation(powerName));
            if (type != null) {
                setType(type);
                TypeSpecificData data = type.newSpecificDataInstance();
                if (data != null) {
                    setTypeSpecificData(data);
                    data.readNBT(nbt.getCompound("AdditionalData"));
                }
            }
        }
        
        if (nbt.contains("HadPowers", MCUtil.getNbtId(ListNBT.class))) {
            ListNBT list = nbt.getList("HadPowers", MCUtil.getNbtId(StringNBT.class));
            for (int i = 0; i < list.size(); i++) {
                String name = list.getString(i);
                if (!name.isEmpty()) {
                    ResourceLocation id = new ResourceLocation(name);
                    if (powerTypeRegistry.containsKey(id)) {
                        hadPowers.add(powerTypeRegistry.getValue(id));
                    }
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
            PacketManager.sendToClient(new PreviousPowerTypesPacket(hadPowers), player);
        });
    }
    
    @Override
    public void syncWithTrackingOrUser(ServerPlayerEntity player) {
        super.syncWithTrackingOrUser(player);
        if (hasPower() && user != null) {
            PacketManager.sendToClient(new TrTypeNonStandPowerPacket(user.getId(), getType()), player);
            getTypeSpecificData(null).ifPresent(data -> {
                data.syncWithTrackingOrUser(user, player);
            });
            PacketManager.sendToClient(new TrEnergyPacket(user.getId(), energy), player);
        }
    }
    
}