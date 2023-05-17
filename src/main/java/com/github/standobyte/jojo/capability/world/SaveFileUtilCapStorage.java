package com.github.standobyte.jojo.capability.world;

import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class SaveFileUtilCapStorage implements IStorage<SaveFileUtilCap> {

    @Override
    public INBT writeNBT(Capability<SaveFileUtilCap> capability, SaveFileUtilCap instance, Direction side) {
        CompoundNBT cnbt = new CompoundNBT();
        CompoundNBT timesStandsTakenMap = new CompoundNBT();
        for (Map.Entry<StandType<?>, Integer> entry : instance.timesStandsTaken.entrySet()) {
            timesStandsTakenMap.putInt(JojoCustomRegistries.STANDS.getKeyAsString(entry.getKey()), entry.getValue());
        }
        cnbt.put("StandsTaken", timesStandsTakenMap);
        
        cnbt.put("TimeStopGameRules", instance.save());
        
        return cnbt;
    }

    @Override
    public void readNBT(Capability<SaveFileUtilCap> capability, SaveFileUtilCap instance, Direction side, INBT nbt) {
        CompoundNBT cnbt = (CompoundNBT) nbt;
        if (cnbt.contains("StandsTaken", 10)) {
            Map<StandType<?>, Integer> stands = new HashMap<>();
            CompoundNBT timesStandsTakenNBT = cnbt.getCompound("StandsTaken");
            JojoCustomRegistries.STANDS.getRegistry().forEach(stand -> {
                int timesTaken = timesStandsTakenNBT.getInt(JojoCustomRegistries.STANDS.getKeyAsString(stand));
                if (timesTaken > 0) {
                    stands.put(stand, timesTaken);
                }
            });
            instance.timesStandsTaken = stands;
        }
        
        instance.load(cnbt.getCompound("TimeStopGameRules"));
    }
}