package com.github.standobyte.jojo.item.polaroid;

import java.util.UUID;

import com.github.standobyte.jojo.network.BatchSender;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClPhotoSaveDataPacket;

public class ClPhotoSender extends BatchSender {
    private final long photoId;

    public ClPhotoSender(byte[] data, UUID serverId, long photoId) {
        super(data);
        this.photoId = photoId;
    }

    @Override
    protected void sendBatch(Batch dataBatch) {
        PacketManager.sendToServer(new ClPhotoSaveDataPacket(photoId, dataBatch));
    }

}
