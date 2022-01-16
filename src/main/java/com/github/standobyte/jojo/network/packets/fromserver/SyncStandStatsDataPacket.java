package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.init.ModStandTypes;
import com.github.standobyte.jojo.power.stand.stats.StandStatsV2;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.github.standobyte.jojo.util.data.StandStatsManager;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncStandStatsDataPacket {
    private final List<StandStatsDataEntry> stats;
    
    public SyncStandStatsDataPacket(Map<StandType<?>, StandStatsV2> statsMap) {
        this(statsMap.entrySet()
                .stream()
                .map(entry -> new StandStatsDataEntry(entry.getKey().getRegistryName(), entry.getValue()))
                .collect(Collectors.toList()));
    }
    
    private SyncStandStatsDataPacket(List<StandStatsDataEntry> stats) {
        this.stats = stats;
    }
    
    public static void encode(SyncStandStatsDataPacket msg, PacketBuffer buf) {
        buf.writeVarInt(msg.stats.size());
        msg.stats.forEach(entry -> entry.write(buf));
    }
    
    public static SyncStandStatsDataPacket decode(PacketBuffer buf) {
        int size = buf.readVarInt();
        List<StandStatsDataEntry> stats = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            stats.add(StandStatsDataEntry.read(buf));
        }
        return new SyncStandStatsDataPacket(stats);
    }

    public static void handle(SyncStandStatsDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            StandStatsManager.getInstance().clSetStats(msg.stats);
        });
        ctx.get().setPacketHandled(true);
    }
    
    
    
    public static class StandStatsDataEntry {
        private final ResourceLocation location;
        private final StandStatsV2 stats;
        
        private StandStatsDataEntry(ResourceLocation location, StandStatsV2 stats) {
            this.location = location;
            this.stats = stats;
        }
        
        private static StandStatsDataEntry read(PacketBuffer buf) {
            ResourceLocation location = buf.readResourceLocation();
            StandType<?> stand = ModStandTypes.Registry.getRegistry().getValue(location);
            if (stand == null) {
                throw new IllegalStateException("Stand stats synchronization error: " + location + " not registered");
            }
            return new StandStatsDataEntry(location, StandStatsV2.fromBuffer(stand.getStatsClass(), buf));
        }
        
        private void write(PacketBuffer buf) {
            buf.writeResourceLocation(location);
            stats.write(buf);
        }

        public ResourceLocation getStandTypeLocation() {
            return location;
        }

        public StandStatsV2 getStats() {
            return stats;
        }
    }
}
