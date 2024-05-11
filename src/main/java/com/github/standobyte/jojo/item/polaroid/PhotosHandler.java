package com.github.standobyte.jojo.item.polaroid;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.github.standobyte.jojo.network.BatchReceiver;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.PhotoDataPacket;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.DimensionSavedDataManager;

public class PhotosHandler {
    private final MinecraftServer server;
    private long serverPhotoId = 0;
    private final Long2ObjectMap<BatchReceiver> photosReceiver = new Long2ObjectArrayMap<>();
    private final Long2ObjectMap<Set<ServerPlayerEntity>> playersRequestedEarly = new Long2ObjectArrayMap<>();
    
    
    public PhotosHandler(MinecraftServer server) {
        this.server = server;
    }
    
    public long incPolaroidPhotoId() {
        return ++serverPhotoId;
    }
    
    public BatchReceiver getOrCreateReceiver(long photoId) {
        return photosReceiver.computeIfAbsent(photoId, n -> new BatchReceiver());
    }
    
    public void putPhoto(long id, byte[] photoData, UUID photoSender, UUID serverId) {
        DimensionSavedDataManager serverData = server.overworld().getDataStorage();
        PolaroidPhotoData photo = new PolaroidPhotoData(makePhotoId(id), photoData, photoSender);
        photo.setDirty();
        serverData.set(photo);
        
        Set<ServerPlayerEntity> earlyRequested = playersRequestedEarly.get(id);
        if (earlyRequested != null) {
            earlyRequested.forEach(player -> {
                if (!player.hasDisconnected()) {
                    photo.sendTo(player, serverId, id); 
                }
            });
        }
        playersRequestedEarly.remove(id);
    }
    
    public void requestPhoto(long photoId, ServerPlayerEntity player, UUID serverId) {
        DimensionSavedDataManager serverData = server.overworld().getDataStorage();
        String stringId = makePhotoId(photoId);
        PolaroidPhotoData photo = serverData.get(() -> new PolaroidPhotoData(stringId), stringId);
        if (photo != null) {
            photo.sendTo(player, serverId, photoId);
        }
        else {
            playersRequestedEarly.computeIfAbsent(photoId, num -> new HashSet<>()).add(player);
            PacketManager.sendToClient(PhotoDataPacket.failed(serverId, photoId), player);
        }
    }
    
    private static String makePhotoId(long id) {
        return "jojo_photo" + id;
    }
    
    
    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putLong("PhotoId", serverPhotoId);
        return nbt;
    }
    
    public void fromNBT(CompoundNBT nbt) {
        serverPhotoId = nbt.getLong("PhotoId");
    }
}
