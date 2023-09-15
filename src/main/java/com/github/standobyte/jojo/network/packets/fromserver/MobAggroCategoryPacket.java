package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.network.NetworkUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;
import com.github.standobyte.jojo.util.mc.MobAggroCategory;

import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class MobAggroCategoryPacket {
    private final Collection<Entry> entries;
    
    public static MobAggroCategoryPacket createFrom(Collection<EntityType<?>> entityTypes, World world) {
        Collection<Entry> entries = entityTypes.stream()
                .map(type -> new Entry(type, MobAggroCategory.getCategoryOnServer(type, world)))
                .collect(Collectors.toList());
        return new MobAggroCategoryPacket(entries);
    }
    
    private MobAggroCategoryPacket(Collection<Entry> entries) {
        this.entries = entries;
    }
    
    
    private static class Entry {
        private final EntityType<?> entityType;
        @Nullable
        private final MobAggroCategory category;
        
        private Entry(EntityType<?> entityType, MobAggroCategory category) {
            this.entityType = entityType;
            this.category = category;
        }
        
        private void toBuf(PacketBuffer buf) {
            buf.writeRegistryId(entityType);
            NetworkUtil.writeOptionally(buf, category, e -> buf.writeEnum(e));
        }
        
        private static Entry fromBuf(PacketBuffer buf) {
            return new Entry(buf.readRegistryIdSafe(EntityType.class), 
                    NetworkUtil.readOptional(buf, () -> buf.readEnum(MobAggroCategory.class)).orElse(null));
        }
    }
    
    
    public static class Handler implements IModPacketHandler<MobAggroCategoryPacket> {

        @Override
        public void encode(MobAggroCategoryPacket msg, PacketBuffer buf) {
            NetworkUtil.writeCollection(buf, msg.entries, entry -> entry.toBuf(buf), false);
        }

        @Override
        public MobAggroCategoryPacket decode(PacketBuffer buf) {
            Collection<Entry> entries = NetworkUtil.readCollection(buf, () -> Entry.fromBuf(buf));
            return new MobAggroCategoryPacket(entries);
        }

        @Override
        public void handle(MobAggroCategoryPacket msg, Supplier<NetworkEvent.Context> ctx) {
            for (Entry entry : msg.entries) {
                MobAggroCategory.setCategoryManually(entry.entityType, entry.category);
            }
        }

        @Override
        public Class<MobAggroCategoryPacket> getPacketClass() {
            return MobAggroCategoryPacket.class;
        }
    }
}
