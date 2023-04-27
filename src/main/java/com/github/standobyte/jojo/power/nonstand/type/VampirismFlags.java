package com.github.standobyte.jojo.power.nonstand.type;

import java.util.List;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.non_stand.VampirismAction;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.init.power.non_stand.vampirism.ModVampirismActions;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrNonStandFlagPacket;
import com.github.standobyte.jojo.network.packets.fromserver.TrNonStandFlagPacket.Flag;
import com.github.standobyte.jojo.power.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.nonstand.TypeSpecificData;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class VampirismFlags extends TypeSpecificData {
    private boolean vampireHamonUser = false;
    private boolean vampireFullPower = false;
    private int lastBloodLevel = -999;

    @Override
    public void onPowerGiven(NonStandPowerType<?> oldType) {
        if (!power.getUser().level.isClientSide()) {
            if (oldType == ModPowers.HAMON.get()) {
                setVampireHamonUser(true);
            }
            power.addEnergy(300);
        }
    }

    @Override
    public boolean isActionUnlocked(Action<INonStandPower> action, INonStandPower power) {
        return vampireFullPower || 
                action == ModVampirismActions.VAMPIRISM_BLOOD_DRAIN.get() || 
                action == ModVampirismActions.VAMPIRISM_BLOOD_GIFT.get() || 
                vampireHamonUser && action == ModVampirismActions.VAMPIRISM_HAMON_SUICIDE.get();
    }

    public boolean isVampireHamonUser() {
        return vampireHamonUser;
    }

    public void setVampireHamonUser(boolean vampireHamonUser) {
        if (!this.vampireHamonUser == vampireHamonUser) {
            serverPlayer.ifPresent(player -> {
                PacketManager.sendToClientsTrackingAndSelf(new TrNonStandFlagPacket(
                        player.getId(), Flag.VAMPIRE_HAMON_USER, vampireHamonUser), player);
            });
        }
        this.vampireHamonUser = vampireHamonUser;
        if (vampireHamonUser) {
            addHamonSuicideAbility();
        }
    }
    
    private void addHamonSuicideAbility() {
        VampirismAction hamonAbility = ModVampirismActions.VAMPIRISM_HAMON_SUICIDE.get();
        List<Action<INonStandPower>> abilities = power.getAbilities();
        if (vampireHamonUser && !abilities.contains(hamonAbility)) {
            abilities.add(hamonAbility);
        }
    }

    public boolean isVampireAtFullPower() {
        return vampireFullPower;
    }
    
    public void setVampireFullPower(boolean vampireFullPower) {
        if (this.vampireFullPower != vampireFullPower) {
            serverPlayer.ifPresent(player -> {
                PacketManager.sendToClientsTrackingAndSelf(new TrNonStandFlagPacket(
                        player.getId(), Flag.VAMPIRE_FULL_POWER, vampireFullPower), player);
            });
        }
        this.vampireFullPower = vampireFullPower;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("VampireHamonUser", vampireHamonUser);
        nbt.putBoolean("VampireFullPower", vampireFullPower);
        return nbt;
    }

    @Override
    public void readNBT(CompoundNBT nbt) {
        this.vampireHamonUser = nbt.getBoolean("VampireHamonUser");
        this.vampireFullPower = nbt.getBoolean("VampireFullPower");
    }

    @Override
    public void syncWithUserOnly(ServerPlayerEntity user) {
        if (vampireHamonUser) {
            addHamonSuicideAbility();
        }
        lastBloodLevel = -999;
    }
    
    public boolean refreshBloodLevel(int bloodLevel) {
        boolean changed = this.lastBloodLevel != bloodLevel;
        this.lastBloodLevel = bloodLevel;
        return changed;
    }
    
    @Override
    public void syncWithTrackingOrUser(LivingEntity user, ServerPlayerEntity entity) {
        PacketManager.sendToClient(new TrNonStandFlagPacket(
                user.getId(), Flag.VAMPIRE_HAMON_USER, vampireHamonUser), entity);
        PacketManager.sendToClient(new TrNonStandFlagPacket(
                user.getId(), Flag.VAMPIRE_FULL_POWER, vampireFullPower), entity);
    }
}
