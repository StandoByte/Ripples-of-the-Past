package com.github.standobyte.jojo.capability.entity;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class ClientPlayerUtilCapProvider implements ICapabilityProvider {
    @CapabilityInject(ClientPlayerUtilCap.class)
    public static Capability<ClientPlayerUtilCap> CAPABILITY = null;
    private LazyOptional<ClientPlayerUtilCap> instance;
    
    public ClientPlayerUtilCapProvider(PlayerEntity player) {
        this.instance = LazyOptional.of(() -> new ClientPlayerUtilCap((AbstractClientPlayerEntity) player));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CAPABILITY.orEmpty(cap, instance);
    }

}