package com.github.standobyte.jojo.power.impl.stand;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.StandActionLearningPacket;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mod.LegacyUtil;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class StandActionLearningProgress {
    private final Map<StandAction, StandActionLearningEntry> wrappedMap = new HashMap<>();
    
    @Nullable
    private StandActionLearningEntry getEntry(StandAction action, StandType<?> currentlyUsedStand) {
        StandActionLearningEntry entry = wrappedMap.get(action);
        if (entry != null && entry.standType != currentlyUsedStand) {
            entry = null;
        }
        return entry;
    }
    
    public float getLearningProgressPoints(StandAction action, StandType<?> currentlyUsedStand) {
        StandActionLearningEntry entry = getEntry(action, currentlyUsedStand);
        return entry != null ? Math.max(entry.getPoints(), 0) : -1;
    }
    
    boolean addEntry(StandAction action, StandType<?> standType) {
        if (wrappedMap.containsKey(action)) {
            return false;
        }
        
        wrappedMap.put(action, new StandActionLearningEntry(action, standType, 0));
        return true;
    }
    
    boolean setLearningProgressPoints(StandAction action, float points, IStandPower power) {
        StandActionLearningEntry entry = wrappedMap.computeIfAbsent(action, a -> new StandActionLearningEntry(a, power.getType(), 0));
        entry.setPoints(points);
        return true;
    }
    
    void setEntryDirectly(StandAction action, StandActionLearningEntry entry) {
        wrappedMap.put(action, entry);
    }
    
    void syncEntryWithUser(StandAction action, ServerPlayerEntity user) {
        PacketManager.sendToClient(new StandActionLearningPacket(action, wrappedMap.get(action), false), user);
    }
    
    void syncFullWithUser(ServerPlayerEntity user) {
        wrappedMap.forEach((action, progress) -> {
            PacketManager.sendToClient(new StandActionLearningPacket(action, progress, false), user);
        });
    }
    
    
    
    public void fromNBT(CompoundNBT nbt) {
        nbt.getAllKeys().forEach(actionName -> {
            Action<?> action = JojoCustomRegistries.ACTIONS.getRegistry().getValue(new ResourceLocation(actionName));
            if (action instanceof StandAction) {
                StandAction standAction = (StandAction) action;
                
                Optional<CompoundNBT> entryNBT = MCUtil.nbtGetCompoundOptional(nbt, action.getRegistryName().toString());
                Optional<StandActionLearningEntry> entryRead = entryNBT.flatMap(tag -> StandActionLearningEntry.fromNBT(standAction, tag));
                if (!entryRead.isPresent()) {
                    entryRead = LegacyUtil.readOldStandActionLearning(nbt, standAction);
                }
                
                entryRead.ifPresent(entry -> {
                    wrappedMap.put(standAction, entry);
                });
            }
        });
    }
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        wrappedMap.forEach((action, entry) -> {
            nbt.put(action.getRegistryName().toString(), entry.toNBT());
        });
        return nbt;
    }
    
    
    
    public static class StandActionLearningEntry {
        private final StandAction action;
        private final StandType<?> standType;
        private float points;
        
        public StandActionLearningEntry(StandAction action, StandType<?> standType, float points) {
            this.action = action;
            this.standType = standType;
            this.points = points;
        }
        
        private void setPoints(float points) {
            this.points = points;
        }
        
        private float getPoints() {
            return points;
        }
        
        
        public CompoundNBT toNBT() {
            CompoundNBT nbt = new CompoundNBT();
            MCUtil.nbtPutRegistryEntry(nbt, "Stand", standType);
            nbt.putFloat("Points", points);
            return nbt;
        }
        
        public static Optional<StandActionLearningEntry> fromNBT(StandAction action, CompoundNBT nbt) {
            Optional<StandType<?>> standFromNbt = MCUtil.nbtGetRegistryEntry(nbt, "Stand", JojoCustomRegistries.STANDS.getRegistry());
            return standFromNbt.flatMap(stand -> {
                if (nbt.contains("Points", MCUtil.getNbtId(FloatNBT.class))) {
                    return Optional.of(new StandActionLearningEntry(action, stand, nbt.getFloat("Points")));
                }
                return Optional.empty();
            });
        }
        
        
        public void toBuf(PacketBuffer buffer) {
            buffer.writeFloat(points);
            buffer.writeRegistryId(standType);
        }
        
        public static StandActionLearningEntry fromBuf(PacketBuffer buffer, StandAction action) {
            float points = buffer.readFloat();
            StandType<?> standType = buffer.readRegistryIdSafe(StandType.class);
            return new StandActionLearningEntry(action, standType, points);
        }
    }
}
