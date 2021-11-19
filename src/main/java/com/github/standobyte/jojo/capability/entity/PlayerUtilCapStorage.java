package com.github.standobyte.jojo.capability.entity;

import com.github.standobyte.jojo.capability.entity.PlayerUtilCap.OneTimeNotification;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class PlayerUtilCapStorage implements IStorage<PlayerUtilCap> {

    @Override
    public INBT writeNBT(Capability<PlayerUtilCap> capability, PlayerUtilCap instance, Direction side) {
        CompoundNBT cnbt = new CompoundNBT();
        CompoundNBT notificationsMap = new CompoundNBT();
        for (OneTimeNotification flag : OneTimeNotification.values()) {
            notificationsMap.putBoolean(flag.name(), instance.sentNotification(flag));
        }
        cnbt.put("NotificationsSent", notificationsMap);
        return cnbt;
    }

    @Override
    public void readNBT(Capability<PlayerUtilCap> capability, PlayerUtilCap instance, Direction side, INBT nbt) {
        CompoundNBT cnbt = (CompoundNBT) nbt;
        if (cnbt.contains("NotificationsSent", 10)) {
            CompoundNBT notificationsMap = cnbt.getCompound("NotificationsSent");
            for (OneTimeNotification flag : OneTimeNotification.values()) {
                instance.setSentNotification(flag, notificationsMap.getBoolean(flag.name()));
            }
        }
    }
}