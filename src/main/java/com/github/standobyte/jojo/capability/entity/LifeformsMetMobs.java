package com.github.standobyte.jojo.capability.entity;

import java.util.HashSet;
import java.util.Set;

import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.ability_specific.MetEntityTypesPacket;
import com.github.standobyte.jojo.util.mc.SubtypeResourceLocation;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;

public class LifeformsMetMobs {
    private Set<SubtypeResourceLocation> metEntityTypesId = new HashSet<>();
    
    
    public boolean add(ResourceLocation entityTypeId) {
        return metEntityTypesId.add(new SubtypeResourceLocation(entityTypeId, null));
    }
    
    public boolean add(ResourceLocation entityTypeId, String variant) {
        return metEntityTypesId.add(new SubtypeResourceLocation(entityTypeId, variant));
    }
    
    public boolean contains(ResourceLocation entityTypeId) {
        return metEntityTypesId.contains(entityTypeId);
    }
    
    public boolean contains(ResourceLocation entityTypeId, String variant) {
        return metEntityTypesId.contains(new SubtypeResourceLocation(entityTypeId, variant));
    }
    
    public boolean isEmpty() {
        return metEntityTypesId.isEmpty();
    }
    
    
    public ListNBT toNBT() {
        ListNBT metEntities = new ListNBT();
        metEntityTypesId.forEach(entityTypeId -> metEntities.add(StringNBT.valueOf(entityTypeId.toString())));
        return metEntities;
    }
    
    public void fromNBT(ListNBT metEntitiesId) {
        metEntitiesId.forEach(idNBT -> {
            String idString = ((StringNBT) idNBT).getAsString(); 
            if (!idString.isEmpty()) {
                SubtypeResourceLocation registryName = new SubtypeResourceLocation(idString);
                metEntityTypesId.add(registryName);
            }
        });
    }
    
    public void syncToClient(ServerPlayerEntity player) {
        PacketManager.sendToClient(new MetEntityTypesPacket(metEntityTypesId), player);
    }
    
    
//    public static <T extends Entity> void makeSubtype(EntityType<T> entityType, String subtypeId, Predicate<T> subtype) {
//        
//    }
}
