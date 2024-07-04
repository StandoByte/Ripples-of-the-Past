package com.github.standobyte.jojo.util.mc;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class EntityOwnerResolver {
    private LivingEntity owner;
    private UUID ownerUUID;
    private int ownerNetworkId;
    
    public void setOwner(LivingEntity owner) {
        this.ownerUUID = owner != null ? owner.getUUID() : null;
        this.ownerNetworkId = owner != null ? owner.getId() : 0;
        _setNewOwnerEntity(owner);
    }
    
    public LivingEntity getEntity(World world) {
        if (owner == null || !owner.isAlive()) {
            if (ownerUUID != null && world instanceof ServerWorld) {
                _setNewOwnerEntity(((ServerWorld) world).getEntity(ownerUUID));
            } else if (ownerNetworkId != 0) {
                _setNewOwnerEntity(world.getEntity(ownerNetworkId));
            }
        }
        
        return owner;
    }
    
    private void _setNewOwnerEntity(Entity entity) {
        if (entity instanceof LivingEntity) {
            this.owner = (LivingEntity) entity;
        }
    }
    
    
    
    public void saveNbt(CompoundNBT nbt, String key) {
        if (ownerUUID != null) {
            nbt.putUUID(key, ownerUUID);
        }
    }
    
    public void loadNbt(CompoundNBT nbt, String key) {
        ownerUUID = nbt.hasUUID(key) ? nbt.getUUID(key) : null;
    }
    
    public void writeNetwork(PacketBuffer buf) {
        buf.writeInt(ownerNetworkId);
    }
    
    public void readNetwork(PacketBuffer buf) {
        buf.readInt();
    }
}
