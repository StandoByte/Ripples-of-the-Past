package com.github.standobyte.jojo.item.polaroid;

import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.network.BatchSender;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldSavedData;

public class PolaroidPhotoData extends WorldSavedData {
    private byte[] photoBytes = new byte[0];
    private UUID senderPlayer;
    
    public PolaroidPhotoData(String id) {
        super(id);
    }
    
    public PolaroidPhotoData(String id, byte[] photoBytes, @Nullable UUID senderPlayer) {
        super(id);
        this.photoBytes = photoBytes;
        this.senderPlayer = senderPlayer;
    }
    
    public void sendTo(ServerPlayerEntity player, UUID serverId, long photoId) {
        BatchSender sender = new SrvPhotoSender(photoBytes, serverId, photoId, player);
        sender.sendAll();
    }

    @Override
    public void load(CompoundNBT nbt) {
        this.photoBytes = nbt.getByteArray("Photo");
        this.senderPlayer = nbt.hasUUID("Sender") ? nbt.getUUID("Sender") : null;
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.putByteArray("Photo", photoBytes);
        if (senderPlayer != null) {
            nbt.putUUID("Sender", senderPlayer);
        }
        return nbt;
    }

}
