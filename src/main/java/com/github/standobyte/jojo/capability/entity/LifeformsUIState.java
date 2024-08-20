package com.github.standobyte.jojo.capability.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClGEUiDataPacket;
import com.github.standobyte.jojo.network.packets.fromserver.ability_specific.GEUiDataPacket;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.entitysubtype.EntitySubtype;
import com.github.standobyte.jojo.util.mc.entitysubtype.SubtypeResourceLocation;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

public class LifeformsUIState {
    private final PlayerEntity player;
    
    EntitySubtype<?> chosenType = null;
    Set<ResourceLocation> favoritesMobs = new HashSet<>();
    Set<ResourceLocation> newUnseenMobs = new HashSet<>();
    
    public LifeformsUIState(PlayerEntity player) {
        this.player = player;
    }
    
    
    
    @Nullable
    public EntitySubtype<?> getGEChosenLifeformType() {
        return chosenType;
    }

    public void setGEChosenLifeformType(EntitySubtype<?> type, boolean syncToServer) {
        this.chosenType = type;
        if (syncToServer && player.level.isClientSide()) {
            PacketManager.sendToServer(ClGEUiDataPacket.chosenEntityType(Optional.ofNullable(type)));
        }
    }
    
    
    public boolean isGELifeformInFavorites(EntityType<?> type) {
        return favoritesMobs.contains(type.getRegistryName());
    }

    public boolean GELifeformAddFav(EntityType<?> type) {
        if (favoritesMobs.add(type.getRegistryName()) && player.level.isClientSide()) {
            PacketManager.sendToServer(ClGEUiDataPacket.favoriteAdded(type));
            return true;
        }
        
        return false;
    }

    public boolean GELifeformRemoveFav(EntityType<?> type) {
        if (favoritesMobs.remove(type.getRegistryName()) && player.level.isClientSide()) {
            PacketManager.sendToServer(ClGEUiDataPacket.favoriteRemoved(type));
            return true;
        }
        
        return false;
    }
    
    public void setGELifeformFavs(Collection<ResourceLocation> favorites) {
        favoritesMobs.clear();
        favoritesMobs.addAll(favorites);
    }
    
    
    public boolean isGELifeformNew(EntityType<?> type) {
        return newUnseenMobs.contains(type.getRegistryName());
    }
    
    public void clearGENewMobs() {
        newUnseenMobs.clear();
        if (player.level.isClientSide()) {
            PacketManager.sendToServer(ClGEUiDataPacket.clearUnseen());
        }
    }
    
    public void setGELifeformsNew(Collection<ResourceLocation> newUnseen) {
        newUnseenMobs.clear();
        newUnseenMobs.addAll(newUnseen);
    }
    
    
    public void onPlayerClone(LifeformsUIState prev) {
        this.chosenType = prev.chosenType;
        this.favoritesMobs = prev.favoritesMobs;
        this.newUnseenMobs = prev.newUnseenMobs;
    }
    
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();

        if (chosenType != null) {
            nbt.put("ChosenType", StringNBT.valueOf(chosenType.getId().toString()));
        }
        if (!favoritesMobs.isEmpty()) {
            ListNBT list = favoritesMobs.stream()
                    .map(Object::toString).map(StringNBT::valueOf)
                    .collect(ListNBT::new, ListNBT::add, ListNBT::addAll);
            nbt.put("FavoritesMobs", list);
        }
        if (!newUnseenMobs.isEmpty()) {
            ListNBT list = favoritesMobs.stream()
                    .map(Object::toString).map(StringNBT::valueOf)
                    .collect(ListNBT::new, ListNBT::add, ListNBT::addAll);
            nbt.put("NewMobs", list);
        }
        
        return nbt;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        if (nbt.contains("ChosenType", Constants.NBT.TAG_STRING)) {
            SubtypeResourceLocation id = new SubtypeResourceLocation(nbt.getString("ChosenType"));
            chosenType = EntitySubtype.getSubtype(id);
        }
        MCUtil.nbtGetList(nbt, "FavoritesMobs", StringNBT.class)
                .map(listNbt -> listNbt
                        .stream()
                        .map(elemNbt -> ((StringNBT) elemNbt).getAsString())
                        .map(ResourceLocation::new)
                        .collect(Collectors.toList()))
                .ifPresent(hidden -> favoritesMobs.addAll(hidden));
        MCUtil.nbtGetList(nbt, "NewMobs", StringNBT.class)
                .map(listNbt -> listNbt
                        .stream()
                        .map(elemNbt -> ((StringNBT) elemNbt).getAsString())
                        .map(ResourceLocation::new)
                        .collect(Collectors.toList()))
                .ifPresent(hidden -> newUnseenMobs.addAll(hidden));
    }
    
    
    public GEUiDataPacket makePacket() {
        return new GEUiDataPacket(this.favoritesMobs, this.newUnseenMobs, Optional.ofNullable(chosenType));
    }

}
