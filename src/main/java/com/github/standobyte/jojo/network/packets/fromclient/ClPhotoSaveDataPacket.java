package com.github.standobyte.jojo.network.packets.fromclient;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.function.Supplier;

import com.github.standobyte.jojo.capability.world.SaveFileUtilCapProvider;
import com.github.standobyte.jojo.item.polaroid.PhotosHandler;
import com.github.standobyte.jojo.network.BatchReceiver;
import com.github.standobyte.jojo.network.BatchSender;
import com.github.standobyte.jojo.network.packets.IModPacketHandler;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class ClPhotoSaveDataPacket {
    private final long photoId;
    private final BatchSender.Batch dataBatch;
    
    public ClPhotoSaveDataPacket(long photoId, BatchSender.Batch data) {
        this.photoId = photoId;
        this.dataBatch = data;
    }
    
    
    
    public static class Handler implements IModPacketHandler<ClPhotoSaveDataPacket> {
    
        @Override
        public void encode(ClPhotoSaveDataPacket msg, PacketBuffer buf) {
            buf.writeLong(msg.photoId);
            msg.dataBatch.toBuf(buf);
        }

        @Override
        public ClPhotoSaveDataPacket decode(PacketBuffer buf) {
            long photoId = buf.readLong();
            BatchSender.Batch dataBatch = BatchSender.Batch.fromBuf(buf);
            return new ClPhotoSaveDataPacket(photoId, dataBatch);
        }

        @Override
        public void handle(ClPhotoSaveDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity player = ctx.get().getSender();
            PhotosHandler serverPhotos = SaveFileUtilCapProvider.getSaveFileCap(player).getPolaroidPhotos();
            BatchReceiver receiver = serverPhotos.getOrCreateReceiver(msg.photoId);
            if (receiver != null) {
                ByteBuffer fullPhoto = receiver.receiveYou(msg.dataBatch);
                if (fullPhoto != null) {
                    UUID serverId = SaveFileUtilCapProvider.getSaveFileCap(player).getServerUUID();
                    serverPhotos.putPhoto(msg.photoId, BatchReceiver.byteBufferToArray(fullPhoto), player.getUUID(), serverId);
                }
            }
        }

        @Override
        public Class<ClPhotoSaveDataPacket> getPacketClass() {
            return ClPhotoSaveDataPacket.class;
        }
    }
}
