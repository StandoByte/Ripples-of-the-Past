package com.github.standobyte.jojo.power.impl.stand;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.registries.IForgeRegistry;

public class ResolveLevelsMap {
    private final Map<ResourceLocation, ResolveLevel> wrappedMap = new HashMap<>();
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
        this.levelEntryCached = wrappedMap.computeIfAbsent(standType.getRegistryName(), ___ -> new ResolveLevel(standType));
    }
    
    
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        wrappedMap.forEach((standId, levelEntry) -> {
            nbt.put(standId.toString(), levelEntry.toNBT());
        });
        return nbt;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        nbt.getAllKeys().forEach(key -> {
            ResourceLocation standId = new ResourceLocation(key);
            IForgeRegistry<StandType<?>> registry = JojoCustomRegistries.STANDS.getRegistry();
            StandType<?> stand = registry.containsKey(standId) ? registry.getValue(standId) : null;
            ResolveLevel levelEntry = wrappedMap.computeIfAbsent(standId, ___ -> new ResolveLevel(stand));
            if (nbt.contains(key, MCUtil.getNbtId(CompoundNBT.class))) {
                levelEntry.fromNBT(nbt.getCompound(key));
            }
        });
    }

//    void syncWithUser(IStandPower standPower, ServerPlayerEntity player) {
//        PacketManager.sendToClient(new ResolveLevelPacket(getResolveLevel(standPower), false), player);
//    }
    
    
    
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
        
        private ResolveLevel(@Nullable StandType<?> standType) {
            this.maxLevel = standType != null ? standType.getMaxResolveLevel() : 0;
        }
        
        private void incExtraLevel() {
            extraLevel++;
        }
        
        private boolean setLevel(int level) {
            boolean changed = this.level != level;
            this.level = MathHelper.clamp(level, 0, maxLevel);
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
