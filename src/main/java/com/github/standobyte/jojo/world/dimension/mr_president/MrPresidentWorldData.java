package com.github.standobyte.jojo.world.dimension.mr_president;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.capability.world.MrPresidentWorldDataProvider;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.world.dimension.ModDimensions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;

public class MrPresidentWorldData {
    private final BiMap<UUID, ChunkSectionPos> allocatedRooms = HashBiMap.create();
    private final Map<UUID, MrPresidentTurtlePos> trackedTurtlePos = new HashMap<>();

    public MrPresidentWorldData(ServerWorld world) {}
    
    public static LazyOptional<MrPresidentWorldData> get(MinecraftServer server) {
        ServerWorld world = server.getLevel(ModDimensions.MR_PRESIDENT);
        if (world != null) {
            return world.getCapability(MrPresidentWorldDataProvider.CAPABILITY);
        }
        return LazyOptional.empty();
    }

    public INBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        
        ListNBT roomsMapNbt = new ListNBT();
        for (Map.Entry<UUID, ChunkSectionPos> entry : allocatedRooms.entrySet()) {
            CompoundNBT roomNbt = new CompoundNBT();
            roomNbt.putUUID("Turtle", entry.getKey());
            roomNbt.put("Pos", entry.getValue().toNBT());
            
            MrPresidentTurtlePos turtleTracked = trackedTurtlePos.get(entry.getKey());
            if (turtleTracked != null) {
                turtleTracked.addToNbtEntry(roomNbt);
            }
            
            roomsMapNbt.add(roomNbt);
        }
        nbt.put("Rooms", roomsMapNbt);
        
        return nbt;
    }

    public void fromNBT(INBT inbt) {
        CompoundNBT nbt = (CompoundNBT) inbt;
        
        ListNBT roomsNbt = nbt.getList("Rooms", Constants.NBT.TAG_COMPOUND);
        for (INBT elem : roomsNbt) {
            CompoundNBT roomNbt = (CompoundNBT) elem;
            ChunkSectionPos pos = ChunkSectionPos.fromNBT(roomNbt.getList("Pos", Constants.NBT.TAG_INT));
            if (pos != null && roomNbt.hasUUID("Turtle")) {
                UUID id = roomNbt.getUUID("Turtle");
                allocatedRooms.put(id, pos);
                
                MrPresidentTurtlePos turtleTracked = MrPresidentTurtlePos.fromNbtEntry(roomNbt);
                if (turtleTracked != null) {
                    trackedTurtlePos.put(id, turtleTracked);
                }
            }
        }
    }
    
    @Nullable
    public ChunkSectionPos getAllocatedRoom(UUID turtleId) {
        return allocatedRooms.get(turtleId);
    }
    
    @Nullable
    public UUID getTurtleId(ChunkSectionPos roomPos) {
        return allocatedRooms.inverse().get(roomPos);
    }
    
    public void rememberTurtlePosition(Entity user) {
        MrPresidentTurtlePos pos = this.trackedTurtlePos.computeIfAbsent(user.getUUID(), id -> new MrPresidentTurtlePos());
        pos.turtleDimension = user.level.dimension();
        pos.turtlePos = MrPresidentBackTeleporter.posToTeleportTo(user);
    }
    
    @Nullable
    public MrPresidentTurtlePos getTurtlePosition(UUID turtleId) {
        return trackedTurtlePos.get(turtleId);
    }
    
    // TODO algorithm for generating rooms position
    public ChunkSectionPos posForNewRoom(UUID turtleId) {
        int x = 0;
        int y = 0;
        int z = 0;
        
        int ring = 0;
        ChunkSectionPos pos = new ChunkSectionPos(x, y, z);
        while (allocatedRooms.containsValue(pos)) {
            if (y < 15) {
                y++;
            }
            else {
                y = 0;
                
                if (x == ring) {
                    ring++;
                    x = ring - 1;
                    z = 1;
                }
                else if (z == ring)  { z--; x = -1; }
                else if (x == -ring) { x++; z = -1; }
                else if (z == -ring) { z++; x = 1;  }
                else if (x > 0 && z > 0) { x--; z++; }
                else if (x < 0 && z > 0) { x--; z--; }
                else if (x < 0 && z < 0) { x++; z--; }
                else if (x > 0 && z < 0) { x++; z++; }
                else {
                    throw new RuntimeException("I'm a dumbass");
                }
            }
            
            pos = new ChunkSectionPos(x, y, z);
        }
        
        allocatedRooms.put(turtleId, pos);
        return pos;
    }
    
    
    public static class MrPresidentTurtlePos {
        RegistryKey<World> turtleDimension;
        Vector3d turtlePos;
        
        private MrPresidentTurtlePos() {}
        
        MrPresidentTurtlePos(RegistryKey<World> userDimension, Vector3d userPos) {
            this.turtleDimension = userDimension;
            this.turtlePos = userPos;
        }
        
        void addToNbtEntry(CompoundNBT nbt) {
            if (turtleDimension != null && turtlePos != null) {
                nbt.putString("TurtleDim", turtleDimension.location().toString());
                MCUtil.nbtPutVec3d(nbt, "TurtlePos", turtlePos);
            }
        }
        
        @Nullable
        static MrPresidentTurtlePos fromNbtEntry(CompoundNBT nbt) {
            if (nbt.contains("TurtleDim", Constants.NBT.TAG_STRING)) {
                ResourceLocation dimensionId = new ResourceLocation(nbt.getString("TurtleDim"));
                RegistryKey<World> dimension = MCUtil.getRegistryKeyIfPresent(Registry.DIMENSION_REGISTRY, dimensionId);
                if (dimension == null) {
                    return null;
                }
                
                Vector3d pos = MCUtil.nbtGetVec3d(nbt, "TurtlePos");
                if (pos == null) {
                    return null;
                }
                
                return new MrPresidentTurtlePos(dimension, pos);
            }
            
            return null;
        }
    }
    
    public static class ChunkSectionPos {
        public final int x;
        public final int y;
        public final int z;
        
        public ChunkSectionPos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        public ChunkSectionPos(BlockPos blockPos) {
            this.x = blockPos.getX() >> 4;
            this.y = blockPos.getY() >> 4;
            this.z = blockPos.getZ() >> 4;
        }
        
        public static ChunkSectionPos fromNBT(ListNBT nbt) {
            if (nbt.size() == 3 && nbt.getElementType() == Constants.NBT.TAG_INT) {
                int x = nbt.getInt(0);
                int y = nbt.getInt(1);
                int z = nbt.getInt(2);
                return new ChunkSectionPos(x, y, z);
            }
            
            return null;
        }
        
        public ListNBT toNBT() {
            ListNBT nbt = new ListNBT();
            nbt.add(IntNBT.valueOf(x));
            nbt.add(IntNBT.valueOf(y));
            nbt.add(IntNBT.valueOf(z));
            return nbt;
        }
        
        @Override
        public String toString() {
            return "[" + x + ", " + y + ", " + z + "]";
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (!(obj instanceof ChunkSectionPos)) {
                return false;
            } else {
                ChunkSectionPos other = (ChunkSectionPos) obj;
                if (this.x != other.x) {
                    return false;
                } else if (this.y != other.y) {
                    return false;
                } else {
                    return this.z == other.z;
                }
            }
        }
        
        @Override
        public int hashCode() {
            return (y + z * 31) * 31 + x;
        }
    }
}
