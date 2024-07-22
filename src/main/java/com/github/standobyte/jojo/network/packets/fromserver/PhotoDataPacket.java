package com.github.standobyte.jojo.network.packets.fromserver;

import java.util.UUID;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.polaroid.PhotosCache;
import com.github.standobyte.jojo.client.polaroid.PhotosCache.PhotoHolder;
import com.github.standobyte.jojo.network.BatchSender;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PhotoDataPacket {
    private final UUID serverId;
    private final long photoId;
    private final boolean hasPhoto;
    private final BatchSender.Batch dataBatch;
    
    public PhotoDataPacket(UUID serverId, long photoId, BatchSender.Batch data) {
        this(serverId, photoId, true, data);
    }
    
    private PhotoDataPacket(UUID serverId, long photoId, boolean hasPhoto, BatchSender.Batch data) {
        this.serverId = serverId;
        this.photoId = photoId;
        this.hasPhoto = hasPhoto;
        this.dataBatch = data;
    }
    
    public static PhotoDataPacket failed(UUID serverId, long photoId) {
        return new PhotoDataPacket(serverId, photoId, false, null);
    }
    
    
    
    public static class Handler implements IModPacketHandler<PhotoDataPacket> {

        @Override
        public void encode(PhotoDataPacket msg, PacketBuffer buf) {
            buf.writeUUID(msg.serverId);
            buf.writeLong(msg.photoId);
            buf.writeBoolean(msg.hasPhoto);
            if (msg.hasPhoto) {
                msg.dataBatch.toBuf(buf);
            }
        }

        @Override
        public PhotoDataPacket decode(PacketBuffer buf) {
            UUID serverId = buf.readUUID();
            long photoId = buf.readLong();
            boolean hasPhoto = buf.readBoolean();
            if (hasPhoto) {
                BatchSender.Batch dataBatch = BatchSender.Batch.fromBuf(buf);
                return new PhotoDataPacket(serverId, photoId, dataBatch);
            }
            else {
                return failed(serverId, photoId);
            }
        }

        @Override
        public void handle(PhotoDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
            PhotoHolder photoHolder = PhotosCache.getOrCreatePhotoHolder(msg.serverId, msg.photoId);
            if (photoHolder != null) {
                photoHolder.readBatchFromPacket(msg.hasPhoto ? msg.dataBatch : null);
            }
        }

        @Override
        public Class<PhotoDataPacket> getPacketClass() {
            return PhotoDataPacket.class;
        }
    }
}
