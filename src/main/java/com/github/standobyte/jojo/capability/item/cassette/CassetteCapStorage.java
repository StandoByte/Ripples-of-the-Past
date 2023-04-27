package com.github.standobyte.jojo.capability.item.cassette;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class CassetteCapStorage implements IStorage<CassetteCap> {

    @Override
    public INBT writeNBT(Capability<CassetteCap> capability, CassetteCap instance, Direction side) {
        return instance.toNBT();
    }

    @Override
    public void readNBT(Capability<CassetteCap> capability, CassetteCap instance, Direction side, INBT inbt) {
        instance.fromNBT((CompoundNBT) inbt);
    }

}
