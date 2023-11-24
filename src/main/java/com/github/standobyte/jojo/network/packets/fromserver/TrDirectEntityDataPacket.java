package com.github.standobyte.jojo.network.packets.fromserver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraftforge.fml.network.NetworkEvent;

// a custom class, because SEntityMetadataPacket does not accept a list of data entries
// and can only read either all entries, or the ones marked as dirty
public class TrDirectEntityDataPacket {
    private int entityId;
    private List<EntityDataManager.DataEntry<?>> packedItems;
    
    public TrDirectEntityDataPacket(int entityId, List<EntityDataManager.DataEntry<?>> packedItems) {
        this.entityId = entityId;
        this.packedItems = packedItems;
    }
    
    
    
    public static class Handler implements IModPacketHandler<TrDirectEntityDataPacket> {

        @Override
        public void encode(TrDirectEntityDataPacket msg, PacketBuffer buf) {
            buf.writeVarInt(msg.entityId);
            try {
                EntityDataManager.pack(msg.packedItems, buf);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public TrDirectEntityDataPacket decode(PacketBuffer buf) {
            int entityId = buf.readVarInt();
            List<EntityDataManager.DataEntry<?>> packedItems;
            try {
                packedItems = EntityDataManager.unpack(buf);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return new TrDirectEntityDataPacket(entityId, packedItems);
        }

        @Override
        public void handle(TrDirectEntityDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
            if (msg.packedItems != null) {
                Entity entity = ClientUtil.getEntityById(msg.entityId);
                if (entity != null) {
                    entity.getEntityData().assignValues(msg.packedItems);
                }
            }
        }

        @Override
        public Class<TrDirectEntityDataPacket> getPacketClass() {
            return TrDirectEntityDataPacket.class;
        }
    }
}
