package com.github.standobyte.jojo.capability.world;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class SaveFileUtilCapProvider implements ICapabilitySerializable<INBT>{
    @CapabilityInject(SaveFileUtilCap.class)
    public static Capability<SaveFileUtilCap> CAPABILITY = null;
    private LazyOptional<SaveFileUtilCap> instance;
    
    public SaveFileUtilCapProvider(ServerWorld overworld) {
        this.instance = LazyOptional.of(() -> new SaveFileUtilCap(overworld));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

    @Override
    public INBT serializeNBT() {
        return CAPABILITY.getStorage().writeNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Save file capability LazyOptional is not attached.")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        CAPABILITY.getStorage().readNBT(CAPABILITY, instance.orElseThrow(
                () -> new IllegalArgumentException("Save file capability LazyOptional is not attached.")), null, nbt);
    }
    
    public static SaveFileUtilCap getSaveFileCap(MinecraftServer server) {
        return server.overworld().getCapability(SaveFileUtilCapProvider.CAPABILITY).orElseThrow(
                () -> new IllegalArgumentException("Save file capability LazyOptional is not attached."));
    }
    
    public static SaveFileUtilCap getSaveFileCap(ServerPlayerEntity serverPlayer) {
        return getSaveFileCap(serverPlayer.server);
    }
}
