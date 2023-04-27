package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.JojoModConfig;
import com.github.standobyte.jojo.init.power.stand.ModStandActions;
import com.github.standobyte.jojo.power.stand.stats.StandStats;
import com.github.standobyte.jojo.power.stand.type.StandType;
import com.github.standobyte.jojo.util.mc.data.StandStatsManager;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

public class StandStatsDataPacket {
    private final List<StandStatsDataEntry> stats;
    
    public StandStatsDataPacket(Map<StandType<?>, StandStats> statsMap) {
        this(statsMap.entrySet()
                .stream()
                .map(entry -> new StandStatsDataEntry(entry.getKey().getRegistryName(), entry.getValue()))
                .collect(Collectors.toList()));
    }
    
    private StandStatsDataPacket(List<StandStatsDataEntry> stats) {
        this.stats = stats;
    }
    
    public static void encode(StandStatsDataPacket msg, PacketBuffer buf) {
        buf.writeVarInt(msg.stats.size());
        msg.stats.forEach(entry -> entry.write(buf));
    }
    
    public static StandStatsDataPacket decode(PacketBuffer buf) {
        int size = buf.readVarInt();
        List<StandStatsDataEntry> stats = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            stats.add(StandStatsDataEntry.read(buf));
        }
        return new StandStatsDataPacket(stats);
    }

    public static void handle(StandStatsDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            StandStatsManager.getInstance().clSetStats(msg.stats);
            JojoModConfig.getCommonConfigInstance(true).onStatsDataPackLoad();
        });
        ctx.get().setPacketHandled(true);
    }
    
    
    
    public static class StandStatsDataEntry {
        private final ResourceLocation location;
        private final StandStats stats;
        
        private StandStatsDataEntry(ResourceLocation location, StandStats stats) {
            this.location = location;
            this.stats = stats;
        }
        
        private static StandStatsDataEntry read(PacketBuffer buf) {
            ResourceLocation location = buf.readResourceLocation();
            StandType<?> stand = ModStandActions.STANDS.getRegistry().getValue(location);
            if (stand == null) {
                throw new IllegalStateException("Stand stats synchronization error: " + location + " not registered");
            }
            return new StandStatsDataEntry(location, StandStats.fromBuffer(stand.getStatsClass(), buf));
        }
        
        private void write(PacketBuffer buf) {
            buf.writeResourceLocation(location);
            stats.write(buf);
        }

        public ResourceLocation getStandTypeLocation() {
            return location;
        }

        public StandStats getStats() {
            return stats;
        }
    }
}
