package com.github.standobyte.jojo.power.impl.nonstand.type.zombie;

import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrZombieFlagsPacket;
import com.github.standobyte.jojo.power.impl.nonstand.TypeSpecificData;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class ZombieData extends TypeSpecificData {
    private int lastBloodLevel = -999;
    private boolean disguised = false;

    @Override
    public void onPowerGiven(NonStandPowerType<?> oldType, TypeSpecificData oldData) {
        LivingEntity user = power.getUser();
        
        if (!user.level.isClientSide()) {
            power.addEnergy(1000);
        }
        
        super.onPowerGiven(oldType, oldData);
    }

//    @Override
//    public boolean isActionUnlocked(Action<INonStandPower> action, INonStandPower power) {
//        return  action == ModZombieActions.ZOMBIE_CLAW_LACERATE.get() || 
//                action == ModZombieActions.ZOMBIE_DEVOUR.get() ||
//                action == ModZombieActions.ZOMBIE_DISGUISE.get();
//    }
    
    public void tick() {
        LivingEntity user = power.getUser();
        if (!user.isAlive()) {
            disguised = false;
        }
    }
    
    public boolean toggleDisguise() {
        setDisguiseEnabled(!disguised);
        return disguised;
    }
    
    public void setDisguiseEnabled(boolean isEnabled) {
        if (this.disguised != isEnabled) {
            this.disguised = isEnabled;
            LivingEntity user = power.getUser();
            if (!user.level.isClientSide()) {
                PacketManager.sendToClientsTrackingAndSelf(new TrZombieFlagsPacket(user.getId(), this), user);
                power.getType().updatePassiveEffects(user, power);
            }
        }
    }
    
    public boolean isDisguiseEnabled() {
        return disguised;
    }

    @Override
    public CompoundNBT writeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("DisguiseEnabled", disguised);
        return nbt;
    }
    
    @Override
    public void readNBT(CompoundNBT nbt) {
        disguised = nbt.getBoolean("DisguiseEnabled");
    }
    
    @Override
    public void syncWithUserOnly(ServerPlayerEntity user) {
        lastBloodLevel = -999;
    }
    
    public boolean refreshBloodLevel(int bloodLevel) {
        boolean bloodLevelChanged = this.lastBloodLevel != bloodLevel;
        this.lastBloodLevel = bloodLevel;
        return bloodLevelChanged;
    }
    
    @Override
    public void syncWithTrackingOrUser(LivingEntity user, ServerPlayerEntity entity) {
        PacketManager.sendToClient(new TrZombieFlagsPacket(user.getId(), this), entity);
    }
}
