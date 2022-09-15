package com.github.standobyte.jojo.power.stand;

import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.ResolveLevelPacket;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.github.standobyte.jojo.util.utils.JojoModUtil;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public class ResolveLevelsMap {
    private final Map<StandType<?>, ResolveLevel> wrappedMap = new HashMap<>();
    private ResolveLevel levelEntryCached;
    
    public void onStandSet(StandType<?> standType) {
        if (standType != null) {
            setLevelEntry(standType);
        }
        else {
            levelEntryCached = null;
        }
    }
    
    public int getResolveLevel(IStandPower standPower) {
        return levelEntryRequested(standPower) ? levelEntryCached.level : 0;
    }
    
    public boolean setResolveLevel(IStandPower standPower, int level) {
        return levelEntryRequested(standPower) ? levelEntryCached.setLevel(level) : false;
    }
    
    public void incExtraLevel(IStandPower standPower) {
        if (levelEntryRequested(standPower)) levelEntryCached.incExtraLevel();
    }
    
    public void clear() {
        levelEntryCached = null;
        wrappedMap.clear();
    }
    
    
    
    private boolean levelEntryRequested(IStandPower standPower) {
        if (!standPower.hasPower()) return false;
        
        if (levelEntryCached == null) {
            setLevelEntry(standPower.getType());
        }
        return true;
    }
    
    private void setLevelEntry(StandType<?> standType) {
        this.levelEntryCached = wrappedMap.computeIfAbsent(standType, ResolveLevel::new);
    }
    
    
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        wrappedMap.forEach((stand, levelEntry) -> {
            nbt.put(stand.getRegistryName().toString(), levelEntry.toNBT());
        });
        return nbt;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        nbt.getAllKeys().forEach(key -> {
            ResourceLocation location = new ResourceLocation(key);
            IForgeRegistry<StandType<?>> registry = ModStandTypes.Registry.getRegistry();
            if (registry.containsKey(location)) {
                StandType<?> stand = registry.getValue(location);
                if (stand != null) {
                    ResolveLevel levelEntry = wrappedMap.computeIfAbsent(stand, ResolveLevel::new);
                    if (nbt.contains(key, JojoModUtil.getNbtId(CompoundNBT.class))) {
                        levelEntry.fromNBT(nbt.getCompound(key));
                    }
                }
            }
        });
    }

    void syncWithUser(IStandPower standPower, ServerPlayerEntity player) {
        PacketManager.sendToClient(new ResolveLevelPacket(getResolveLevel(standPower)), player);
    }
    
    
    
    public void readOldValues(IStandPower standPower, int level, int extraLevel) {
        if (levelEntryRequested(standPower)) {
            levelEntryCached.level = level;
            levelEntryCached.extraLevel = extraLevel;
        }
    }
    
    
    private class ResolveLevel {
        private int level = 0;
        private int extraLevel = 0;
        private final int maxLevel;
        
        private ResolveLevel(StandType<?> standType) {
            this.maxLevel = standType.getMaxResolveLevel();
        }
        
        private void incExtraLevel() {
            extraLevel++;
        }
        
        private boolean setLevel(int level) {
            boolean changed = this.level != level;
            this.level = Math.min(level, maxLevel);
            return changed;
        }
        
        

        public CompoundNBT toNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("Level", level);
            nbt.putInt("ExtraLevel", extraLevel);
            return nbt;
        }
        
        public void fromNBT(CompoundNBT nbt) {
            this.level = nbt.getInt("Level");
            this.extraLevel = nbt.getInt("ExtraLevel");
        }
    }
}
