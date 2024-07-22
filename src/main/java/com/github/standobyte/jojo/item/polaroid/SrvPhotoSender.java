package com.github.standobyte.jojo.item.polaroid;

import java.util.UUID;

import com.github.standobyte.jojo.network.BatchSender;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PhotoDataPacket;

import net.minecraft.entity.player.ServerPlayerEntity;

public class SrvPhotoSender extends BatchSender {
    private final UUID serverId;
    private final long photoId;
    private final ServerPlayerEntity player;

    public SrvPhotoSender(byte[] data, UUID serverId, long photoId, ServerPlayerEntity player) {
        super(data);
        this.serverId = serverId;
        this.photoId = photoId;
        this.player = player;
    }

    @Override
    protected void sendBatch(Batch dataBatch) {
        PacketManager.sendToClient(new PhotoDataPacket(serverId, photoId, dataBatch), player);
    }

}
