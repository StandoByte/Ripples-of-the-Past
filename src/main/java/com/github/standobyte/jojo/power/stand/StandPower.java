package com.github.standobyte.jojo.power.stand;

import java.util.Arrays;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.actions.StandAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.SyncManaLimitFactorPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncManaRegenPointsPacket;
import com.github.standobyte.jojo.network.packets.fromserver.SyncStandExpPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.IPowerType;
import com.github.standobyte.jojo.power.PowerBaseImpl;
import com.github.standobyte.jojo.power.stand.type.StandType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class StandPower extends PowerBaseImpl<StandType> implements IStandPower {
    private StandType standType = null;
    private int tier = 0;
    @Nullable
    private IStandManifestation standManifestation = null;
    private int exp = 0;
    
    public StandPower(LivingEntity user) {
        super(user);
    }
    
    @Override
    public PowerClassification getPowerClassification() {
        return PowerClassification.STAND;
    }
    
    @Override
    protected void tickMana() {
        serverPlayerUser.ifPresent(player -> {
            if (getMana() < getMaxMana()) {
                player.causeFoodExhaustion(0.005F);
            }
        });
        super.tickMana();
    }
    
    @Override
    protected void onTypeInit(StandType standType) {
        this.standType = standType;
        attacks = Arrays.asList(standType.getAttacks());
        abilities = Arrays.asList(standType.getAbilities());
        if (user instanceof PlayerEntity && ((PlayerEntity) user).abilities.instabuild) {
            exp = StandPower.MAX_EXP;
        }
        tier = Math.max(tier, standType.getTier());
    }

    @Override
    public StandType getType() {
        return standType;
    }
    
    @Override
    public boolean clear() {
        if (super.clear()) {
            if (isActive()) {
                standType.forceUnsummon(user, this);
            }
            exp = 0;
            standType = null;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean isActive() {
        return hasPower() && getType().isStandSummoned(user, this);
    }
    
    
    public int getExp() {
        return exp;
    }
    
    public void setExp(int exp) {
        exp = MathHelper.clamp(exp, 0, MAX_EXP);
        boolean send = this.exp != exp;
        this.exp = exp;
        if (send) {
            serverPlayerUser.ifPresent(player -> {
                PacketManager.sendToClient(new SyncStandExpPacket(getExp(), true), player);
            });
        }
    }
    
    @Override
    public void setStandManifestation(IStandManifestation standManifestation) {
        this.standManifestation = standManifestation;
        if (standManifestation != null) {
            standManifestation.setUser(getUser());
            standManifestation.setUserPower(this);
        }
    }
    
    @Override
    public IStandManifestation getStandManifestation() {
        return standManifestation;
    }
    
    @Override
    public void toggleSummon() {
        if (!isActive()) {
            getType().summon(user, this, false);
        }
        else {
            getType().unsummon(user, this);
        }
    }
    
    @Override
    public int getTier() {
        return tier;
    }
    
    @Override
    public boolean isActionUnlocked(Action action) {
        return getExp() >= ((StandAction) action).getExpRequirement();
    }
    
    @Override
    public ActionConditionResult checkRequirements(Action action, LivingEntity performer, ActionTarget target, boolean checkTargetType) {
        serverPlayerUser.ifPresent(player -> {
            PacketManager.sendToClient(new SyncStandExpPacket(getExp(), false), player);
        });
        return super.checkRequirements(action, performer, target, checkTargetType);
    }
    
    @Override
    public boolean isLeapUnlocked() {
        return standManifestation instanceof StandEntity && !((StandEntity) standManifestation).isArmsOnlyMode();
    }
    
    @Override
    public float leapStrength() {
        StandEntity standEntity = (StandEntity) standManifestation;
        if (standEntity.isFollowingUser()) {
            return (float) standEntity.getType().getStats().getDamage() / 3F;
        }
        return 0;
    }
    
    @Override
    public int getLeapCooldownPeriod() {
        return 0;
    }
    
    @Override
    protected float getLeapManaCost() {
        return 200;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT cnbt = super.writeNBT();
        cnbt.putString("StandType", ModStandTypes.Registry.getKeyAsString(getType()));
        cnbt.putInt("Exp", getExp());
        return cnbt;
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
        String standName = nbt.getString("StandType");
        if (standName != IPowerType.NO_POWER_NAME) {
            StandType stand = ModStandTypes.Registry.getRegistry().getValue(new ResourceLocation(standName));
            if (stand != null) {
                onTypeInit(stand);
                exp = nbt.getInt("Exp");
            }
        }
        super.readNBT(nbt);
    }
    
    @Override
    public void onClone(IPower<StandType> oldPower, boolean wasDeath) {
        super.onClone(oldPower, wasDeath);
        if (oldPower.hasPower()) {
            exp = ((IStandPower) oldPower).getExp();
        }
        else {
            manaRegenPoints = oldPower.getManaRegenPoints();
            manaLimitFactor = oldPower.getManaLimitFactor();
        }
    }
    
    @Override
    public void syncWithUserOnly() {
        super.syncWithUserOnly();
        serverPlayerUser.ifPresent(player -> {
            if (hasPower()) {
                PacketManager.sendToClient(new SyncStandExpPacket(getExp(), false), player);
            }
            else {
                PacketManager.sendToClient(new SyncManaRegenPointsPacket(getPowerClassification(), getManaRegenPoints()), player);
                PacketManager.sendToClient(new SyncManaLimitFactorPacket(getPowerClassification(), getManaLimitFactor()), player);
            }
        });
    }
    
    @Override
    public void syncWithTrackingOrUser(ServerPlayerEntity player) {
        super.syncWithTrackingOrUser(player);
        if (hasPower() && standManifestation != null) {
            standManifestation.syncWithTrackingOrUser(player);
        }
    }
}
